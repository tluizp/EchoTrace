package io.echotrace.collector.journey.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.journey.response.JourneyFunnelResponse;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JourneyFunnelServiceTest {

    private static final Instant START = Instant.parse("2026-07-19T10:00:00Z");
    private static final Instant END = Instant.parse("2026-07-19T11:00:00Z");

    private final AtomicReference<List<EventEntity>> result = new AtomicReference<>(List.of());
    private final JourneyFunnelService service = new JourneyFunnelService(repositoryStub(result));

    @Test
    void buildsSequentialFunnelWithConversionsFailuresAndDropOffs() {
        result.set(List.of(
                event("a", "checkout", "SUCCESS", 1),
                event("b", "checkout", "SUCCESS", 2),
                event("c", "checkout", "SUCCESS", 3),
                event("a", "order", "SUCCESS", 4),
                event("b", "order", "ERROR", 5),
                event("c", "order", "SUCCESS", 6),
                event("a", "payment", "SUCCESS", 7),
                event("c", "payment", "ERROR", 8),
                event("a", "confirmed", "SUCCESS", 9)
        ));

        JourneyFunnelResponse funnel = service.build("order.checkout", START, END);

        assertEquals(3, funnel.getTotalJourneys());
        assertEquals(1, funnel.getCompletedJourneys());
        assertEquals(2, funnel.getFailedJourneys());
        assertEquals(new BigDecimal("33.33"), funnel.getConversionRate());
        assertEquals(4, funnel.getSteps().size());
        assertEquals(2, funnel.getSteps().get(2).getReachedJourneys());
        assertEquals(1, funnel.getSteps().get(2).getFailedJourneys());
        assertEquals(1, funnel.getSteps().get(2).getDropOffJourneys());
        assertEquals(new BigDecimal("66.67"), funnel.getSteps().get(2).getConversionFromPrevious());
        assertEquals(new BigDecimal("50.00"), funnel.getSteps().get(3).getConversionFromPrevious());
    }

    @Test
    void returnsEmptyFunnelWhenNoStagesWereObserved() {
        JourneyFunnelResponse funnel = service.build("order.checkout", START, END);

        assertEquals(0, funnel.getTotalJourneys());
        assertEquals(new BigDecimal("0.00"), funnel.getConversionRate());
        assertEquals(List.of(), funnel.getSteps());
    }

    @Test
    void rejectsInvalidTimeWindow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.build("order.checkout", END, START));
    }

    private EventEntity event(String journeyId, String stage, String status, long second) {
        EventEntity event = new EventEntity();
        event.setJourneyId(journeyId);
        event.setJourneyType("order.checkout");
        event.setJourneyStage(stage);
        event.setStatus(status);
        event.setCreatedAt(START.plusSeconds(second));
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
