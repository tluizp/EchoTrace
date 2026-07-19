package io.echotrace.collector.controller;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.journey.response.JourneyFunnelResponse;
import io.echotrace.collector.journey.service.JourneyFunnelService;
import io.echotrace.collector.journey.service.JourneyService;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JourneyFunnelControllerTest {

    private static final Instant START = Instant.parse("2026-07-19T10:00:00Z");
    private static final Instant END = Instant.parse("2026-07-19T11:00:00Z");

    @Test
    void returnsFunnelWithHttp200() {
        EventEntity event = new EventEntity();
        event.setJourneyId("order-42");
        event.setJourneyType("order.checkout");
        event.setJourneyStage("checkout");
        event.setStatus("SUCCESS");
        event.setCreatedAt(START.plusSeconds(1));
        JourneyController controller = controllerReturning(List.of(event));

        ResponseEntity<JourneyFunnelResponse> response =
                controller.funnel("order.checkout", START, END);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalJourneys());
    }

    @Test
    void returnsHttp400ForInvalidWindow() {
        JourneyController controller = controllerReturning(List.of());

        ResponseEntity<JourneyFunnelResponse> response =
                controller.funnel("order.checkout", END, START);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    private JourneyController controllerReturning(List<EventEntity> events) {
        EventRepository repository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals(
                            "findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc")) return events;
                    if (method.getName().equals("findByJourneyIdOrderByCreatedAtAsc")) return List.of();
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
        return new JourneyController(new JourneyService(repository), new JourneyFunnelService(repository));
    }
}
