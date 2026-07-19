package io.echotrace.core;

import io.echotrace.model.EventPayload;
import io.echotrace.telemetry.Telemetry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventEmitterTest {

    @AfterEach
    void cleanUp() {
        Telemetry.clear();
    }

    @Test
    void emitsVersionedAndCorrelatedEvent() {
        List<EventPayload> published = new ArrayList<>();
        Instant now = Instant.parse("2026-07-16T12:00:00Z");
        EventEmitter emitter = new EventEmitter(published::add, "checkout", "test",
                Clock.fixed(now, ZoneOffset.UTC), new AttributeSanitizer());
        Telemetry.setTraceId("trace-id");
        Telemetry.setSpanId("span-id");

        EventPayload event = emitter.event("order.created")
                .version(2)
                .outcome("order.creation")
                .journey("order.checkout", "123")
                .stage("order")
                .value(new BigDecimal("49.90"), "BRL")
                .attribute("order.id", "123")
                .emit();

        assertEquals(1, published.size());
        assertNotNull(event.getEventId());
        assertEquals("2.0", event.getSpecVersion());
        assertEquals(2, event.getEventVersion());
        assertEquals("trace-id", event.getTraceId());
        assertEquals(now, event.getCreatedAt());
        assertEquals("123", event.getPayload().get("order.id"));
        assertEquals("order.creation", event.getBusinessOutcome().getName());
        assertEquals("123", event.getBusinessOutcome().getJourneyId());
    }
}
