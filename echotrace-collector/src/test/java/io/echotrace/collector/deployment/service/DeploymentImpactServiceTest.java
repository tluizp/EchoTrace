package io.echotrace.collector.deployment.service;

import io.echotrace.collector.deployment.response.DeploymentImpactResponse;
import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentImpactServiceTest {

    private static final Instant START = Instant.parse("2026-07-19T10:00:00Z");
    private static final Instant END = Instant.parse("2026-07-19T12:00:00Z");

    private final AtomicReference<List<EventEntity>> result = new AtomicReference<>(List.of());
    private final DeploymentImpactService service = new DeploymentImpactService(repositoryStub(result));

    @Test
    void correlatesConversionAndFailuresBeforeAndAfterDeployment() {
        List<EventEntity> events = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            addJourney(events, "before-" + i, 10 + i * 2L, i < 18, i >= 18, false);
            addJourney(events, "after-" + i, 3700 + i * 2L, i < 10, i >= 12, true);
        }
        events.sort(Comparator.comparing(EventEntity::getCreatedAt));
        result.set(events);

        DeploymentImpactResponse impact = service.analyze(
                "order.checkout", "payment-service", "deploy-7", "confirmed", START, END)
                .orElseThrow();

        assertEquals(20, impact.getBefore().getTotalJourneys());
        assertEquals(new BigDecimal("90.00"), impact.getBefore().getConversionRate());
        assertEquals(new BigDecimal("50.00"), impact.getAfter().getConversionRate());
        assertEquals(new BigDecimal("-40.00"), impact.getConversionDeltaPercentagePoints());
        assertEquals(new BigDecimal("30.00"), impact.getFailureDeltaPercentagePoints());
        assertEquals("DEGRADED", impact.getSignal());
        assertTrue(impact.isSampleSufficient());
        assertEquals("2.14.3", impact.getServiceVersion());
        assertEquals(8, impact.getDeploymentEvents().getFailedEvents());
        assertEquals(8L, impact.getDeploymentEvents().getFailureReasons().get("ACQUIRER_TIMEOUT"));
        assertEquals(new BigDecimal("800.00"),
                impact.getAfter().getAffectedValueByCurrency().get("BRL"));
    }

    @Test
    void reportsInsufficientDataForSmallSamples() {
        List<EventEntity> events = new ArrayList<>();
        addJourney(events, "before", 10, true, false, false);
        addJourney(events, "after", 3700, false, true, true);
        events.sort(Comparator.comparing(EventEntity::getCreatedAt));
        result.set(events);

        DeploymentImpactResponse impact = service.analyze(
                "order.checkout", "payment-service", "deploy-7", "confirmed", START, END)
                .orElseThrow();

        assertEquals("INSUFFICIENT_DATA", impact.getSignal());
        assertFalse(impact.isSampleSufficient());
    }

    @Test
    void returnsEmptyWhenDeploymentWasNotObserved() {
        assertFalse(service.analyze(
                "order.checkout", "payment-service", "missing", "confirmed", START, END)
                .isPresent());
    }

    private void addJourney(List<EventEntity> events, String journeyId, long offsetSeconds,
                            boolean completed, boolean failed, boolean targetDeployment) {
        events.add(event(journeyId, "checkout", "SUCCESS", offsetSeconds, false, null));
        events.add(event(journeyId, "payment", failed ? "ERROR" : "SUCCESS",
                offsetSeconds + 1, targetDeployment, failed ? "ACQUIRER_TIMEOUT" : null));
        if (completed) {
            events.add(event(journeyId, "confirmed", "SUCCESS", offsetSeconds + 2, false, null));
        }
    }

    private EventEntity event(String journeyId, String stage, String status, long offsetSeconds,
                              boolean targetDeployment, String reason) {
        EventEntity event = new EventEntity();
        event.setJourneyId(journeyId);
        event.setJourneyType("order.checkout");
        event.setJourneyStage(stage);
        event.setStatus(status);
        event.setCreatedAt(START.plusSeconds(offsetSeconds));
        event.setOutcomeReason(reason);
        if ("ERROR".equals(status)) {
            event.setBusinessValue(new BigDecimal("100.00"));
            event.setCurrency("BRL");
        }
        if (targetDeployment) {
            event.setServiceName("payment-service");
            event.setDeploymentId("deploy-7");
            event.setServiceVersion("2.14.3");
        }
        return event;
    }

    private EventRepository repositoryStub(AtomicReference<List<EventEntity>> result) {
        return (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals(
                            "findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc")) {
                        return result.get();
                    }
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
    }
}
