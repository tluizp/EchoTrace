package io.echotrace.collector.journey.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.journey.response.JourneyResponse;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class JourneyServiceTest {

    private final AtomicReference<List<EventEntity>> result = new AtomicReference<>(List.of());
    private final AtomicReference<String> queriedJourneyId = new AtomicReference<>();
    private final EventRepository repository = repositoryStub(result, queriedJourneyId);
    private final JourneyService service = new JourneyService(repository);

    @Test
    void reconstructsFailedJourneyInChronologicalOrder() {
        EventEntity started = event("event-1", "checkout.started", "checkout", "SUCCESS",
                "2026-07-19T10:31:02Z", null, null);
        EventEntity failed = event("event-2", "payment.processed", "payment", "ERROR",
                "2026-07-19T10:31:08Z", new BigDecimal("349.90"), "ACQUIRER_TIMEOUT");
        failed.setServiceName("payment-service");
        failed.setServiceVersion("2.14.3");
        failed.setDeploymentId("deploy-7");
        result.set(List.of(started, failed));

        JourneyResponse journey = service.findById("order-42").orElseThrow();

        assertEquals("FAILED", journey.getStatus());
        assertEquals(6000, journey.getDurationMs());
        assertEquals(new BigDecimal("349.90"), journey.getAffectedValue());
        assertEquals("BRL", journey.getCurrency());
        assertEquals("checkout.started", journey.getEvents().get(0).getEventName());
        assertEquals("ACQUIRER_TIMEOUT", journey.getEvents().get(1).getReason());
        assertEquals("2.14.3", journey.getEvents().get(1).getServiceVersion());
    }

    @Test
    void doesNotReportAffectedValueForSuccessfulJourney() {
        EventEntity completed = event("event-1", "payment.approved", "payment", "SUCCESS",
                "2026-07-19T10:31:02Z", new BigDecimal("349.90"), null);
        completed.setDurationMs(125);
        result.set(List.of(completed));

        JourneyResponse journey = service.findById("order-42").orElseThrow();

        assertEquals("SUCCESS", journey.getStatus());
        assertEquals(125, journey.getDurationMs());
        assertNull(journey.getAffectedValue());
    }

    @Test
    void returnsEmptyWhenJourneyDoesNotExist() {
        assertFalse(service.findById("missing").isPresent());
        assertEquals("missing", queriedJourneyId.get());
    }

    private EventEntity event(String eventId, String eventName, String stage, String status,
                              String createdAt, BigDecimal value, String reason) {
        EventEntity entity = new EventEntity();
        entity.setEventId(eventId);
        entity.setEventName(eventName);
        entity.setJourneyId("order-42");
        entity.setJourneyType("order.checkout");
        entity.setJourneyStage(stage);
        entity.setStatus(status);
        entity.setCreatedAt(Instant.parse(createdAt));
        entity.setBusinessValue(value);
        entity.setCurrency(value == null ? null : "BRL");
        entity.setOutcomeReason(reason);
        return entity;
    }

    private static EventRepository repositoryStub(AtomicReference<List<EventEntity>> result,
                                                   AtomicReference<String> queriedJourneyId) {
        return (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findByJourneyIdOrderByCreatedAtAsc")) {
                        queriedJourneyId.set((String) args[0]);
                        return result.get();
                    }
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
    }
}
