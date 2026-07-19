package io.echotrace.collector.slo.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.repository.EventRepository;
import io.echotrace.collector.slo.config.BusinessSloProperties;
import io.echotrace.collector.slo.response.SloEvaluationResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessSloServiceTest {

    private static final Instant END = Instant.parse("2026-07-19T12:00:00Z");

    @Test
    void triggersCriticalAlertWhenObjectiveIsBreachedByFivePoints() {
        SloEvaluationResponse evaluation = evaluate(events(20, 18), "95", 20);

        assertEquals("BREACHED", evaluation.getStatus());
        assertTrue(evaluation.isAlertTriggered());
        assertEquals("CRITICAL", evaluation.getAlertSeverity());
        assertEquals(new BigDecimal("90.00"), evaluation.getMeasuredPercentage());
        assertEquals(new BigDecimal("-5.00"),
                evaluation.getErrorBudgetRemainingPercentagePoints());
    }

    @Test
    void triggersWarningForSmallerBreach() {
        SloEvaluationResponse evaluation = evaluate(events(20, 18), "92", 20);

        assertEquals("WARNING", evaluation.getAlertSeverity());
        assertTrue(evaluation.isAlertTriggered());
    }

    @Test
    void reportsHealthyWhenObjectiveIsMet() {
        SloEvaluationResponse evaluation = evaluate(events(20, 18), "90", 20);

        assertEquals("HEALTHY", evaluation.getStatus());
        assertFalse(evaluation.isAlertTriggered());
        assertEquals("NONE", evaluation.getAlertSeverity());
    }

    @Test
    void suppressesAlertWhenSampleIsInsufficient() {
        SloEvaluationResponse evaluation = evaluate(events(5, 1), "90", 20);

        assertEquals("INSUFFICIENT_DATA", evaluation.getStatus());
        assertFalse(evaluation.isAlertTriggered());
    }

    @Test
    void reportsNoDataWithoutConsumingErrorBudget() {
        SloEvaluationResponse evaluation = evaluate(List.of(), "90", 20);

        assertEquals("NO_DATA", evaluation.getStatus());
        assertNull(evaluation.getErrorBudgetRemainingPercentagePoints());
    }

    private SloEvaluationResponse evaluate(List<EventEntity> events, String objective, int minimum) {
        BusinessSloProperties.Definition definition = new BusinessSloProperties.Definition();
        definition.setName("checkout-conversion");
        definition.setJourneyType("order.checkout");
        definition.setCompletionStage("confirmed");
        definition.setObjectivePercentage(new BigDecimal(objective));
        definition.setWindowMinutes(60);
        definition.setMinimumJourneys(minimum);
        BusinessSloProperties properties = new BusinessSloProperties();
        properties.setBusinessSlos(List.of(definition));
        properties.afterPropertiesSet();

        BusinessSloService service = new BusinessSloService(repositoryReturning(events), properties);
        return service.evaluateAll(END).get(0);
    }

    private List<EventEntity> events(int total, int completed) {
        List<EventEntity> events = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            EventEntity started = new EventEntity();
            started.setJourneyId("order-" + i);
            started.setJourneyType("order.checkout");
            started.setJourneyStage("checkout");
            started.setStatus("SUCCESS");
            events.add(started);
            if (i < completed) {
                EventEntity finished = new EventEntity();
                finished.setJourneyId("order-" + i);
                finished.setJourneyType("order.checkout");
                finished.setJourneyStage("confirmed");
                finished.setStatus("SUCCESS");
                events.add(finished);
            }
        }
        return events;
    }

    private EventRepository repositoryReturning(List<EventEntity> events) {
        return (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals(
                            "findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc")) return events;
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
    }
}
