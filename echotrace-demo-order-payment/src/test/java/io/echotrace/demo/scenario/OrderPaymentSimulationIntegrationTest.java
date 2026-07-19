package io.echotrace.demo.scenario;

import io.echotrace.core.EventPublisher;
import io.echotrace.demo.OrderPaymentDemoApplication;
import io.echotrace.model.EventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = {OrderPaymentDemoApplication.class,
                OrderPaymentSimulationIntegrationTest.PublisherConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "echotrace.service-version=2.0.0-test",
                "echotrace.deployment-id=demo-test"
        }
)
class OrderPaymentSimulationIntegrationTest {

    @Autowired
    private OrderPaymentSimulationService service;

    @Autowired
    private CapturingPublisher publisher;

    @BeforeEach
    void clearEvents() {
        publisher.events.clear();
    }

    @Test
    void emitsCompleteAndFailedJourneysThroughTheRealStarter() {
        SimulationResponse response = service.run(4, 50);

        assertEquals(2, response.getSuccessfulOrders());
        assertEquals(2, response.getFailedOrders());
        assertEquals("2.0.0-test", response.getServiceVersion());
        assertEquals(14, publisher.events.size());
        assertEquals(4, count("checkout.started", null));
        assertEquals(4, count("order.created", null));
        assertEquals(2, count("payment.processed", "ERROR"));
        assertEquals(2, count("payment.processed", "SUCCESS"));
        assertEquals(2, count("order.confirmed", null));
        assertEquals("SIMULATED_ACQUIRER_TIMEOUT", publisher.events.stream()
                .filter(event -> "ERROR".equals(event.getStatus()))
                .findFirst().orElseThrow().getBusinessOutcome().getReason());
    }

    @Test
    void rejectsUnsafeScenarioSizes() {
        assertThrows(IllegalArgumentException.class, () -> service.run(1001, 5));
        assertThrows(IllegalArgumentException.class, () -> service.run(10, 101));
    }

    private long count(String eventName, String status) {
        return publisher.events.stream()
                .filter(event -> eventName.equals(event.getEventName()))
                .filter(event -> status == null || status.equals(event.getStatus()))
                .count();
    }

    @TestConfiguration
    static class PublisherConfiguration {
        @Bean
        CapturingPublisher capturingPublisher() {
            return new CapturingPublisher();
        }
    }

    static class CapturingPublisher implements EventPublisher {
        private final List<EventPayload> events = new CopyOnWriteArrayList<>();

        @Override
        public void publish(EventPayload payload) {
            events.add(payload);
        }
    }
}
