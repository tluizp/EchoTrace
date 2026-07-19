package io.echotrace.collector.controller;

import io.echotrace.collector.journey.response.JourneyResponse;
import io.echotrace.collector.journey.service.JourneyService;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JourneyControllerTest {

    @Test
    void returnsJourneyWithHttp200() {
        io.echotrace.collector.entity.EventEntity event = new io.echotrace.collector.entity.EventEntity();
        event.setEventId("event-1");
        event.setEventName("order.created");
        event.setJourneyId("order-42");
        event.setJourneyType("order.checkout");
        event.setStatus("SUCCESS");
        event.setCreatedAt(Instant.EPOCH);
        JourneyController controller = controllerReturning(List.of(event));

        ResponseEntity<JourneyResponse> response = controller.findById("order-42");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("order-42", response.getBody().getJourneyId());
    }

    @Test
    void returnsHttp404WhenJourneyDoesNotExist() {
        JourneyController controller = controllerReturning(List.of());

        ResponseEntity<JourneyResponse> response = controller.findById("missing");

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    private JourneyController controllerReturning(List<io.echotrace.collector.entity.EventEntity> events) {
        EventRepository repository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findByJourneyIdOrderByCreatedAtAsc")) return events;
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
        return new JourneyController(new JourneyService(repository));
    }
}
