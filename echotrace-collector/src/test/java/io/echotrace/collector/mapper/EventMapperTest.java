package io.echotrace.collector.mapper;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.model.BusinessOutcome;
import io.echotrace.model.EventPayload;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventMapperTest {

    @Test
    void mapsBusinessOutcomeToQueryableColumns() {
        Instant now = Instant.parse("2026-07-19T12:00:00Z");
        EventPayload payload = new EventPayload(
                "2.0", "event-1", 1, "payment.processed", "payment-service", "production",
                "ERROR", 125, "trace", "span", now, now, Map.of(),
                new BusinessOutcome("payment.approval", "order-42", "order.checkout",
                        "payment", "ACQUIRER_TIMEOUT", new BigDecimal("349.90"), "BRL"),
                "2.14.3", "deploy-7", "abc123");

        EventEntity entity = new EventMapper().toEntity(payload);

        assertEquals("payment.approval", entity.getOutcomeName());
        assertEquals("order-42", entity.getJourneyId());
        assertEquals(new BigDecimal("349.90"), entity.getBusinessValue());
        assertEquals("deploy-7", entity.getDeploymentId());
    }
}
