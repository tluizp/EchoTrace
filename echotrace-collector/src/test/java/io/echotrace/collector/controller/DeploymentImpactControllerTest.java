package io.echotrace.collector.controller;

import io.echotrace.collector.deployment.response.DeploymentImpactResponse;
import io.echotrace.collector.deployment.service.DeploymentImpactService;
import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeploymentImpactControllerTest {

    private static final Instant START = Instant.parse("2026-07-19T10:00:00Z");
    private static final Instant END = Instant.parse("2026-07-19T11:00:00Z");

    @Test
    void returnsHttp404WhenDeploymentWasNotObserved() {
        DeploymentImpactController controller = controllerReturning(List.of());

        ResponseEntity<DeploymentImpactResponse> response = controller.impact(
                "order.checkout", "missing", "payment-service", "confirmed", START, END);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void returnsHttp400ForInvalidWindow() {
        DeploymentImpactController controller = controllerReturning(List.of());

        ResponseEntity<DeploymentImpactResponse> response = controller.impact(
                "order.checkout", "deploy-7", "payment-service", "confirmed", END, START);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    private DeploymentImpactController controllerReturning(List<EventEntity> events) {
        EventRepository repository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals(
                            "findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc")) return events;
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
        return new DeploymentImpactController(new DeploymentImpactService(repository));
    }
}
