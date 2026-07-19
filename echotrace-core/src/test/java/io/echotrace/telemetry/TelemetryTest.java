package io.echotrace.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TelemetryTest {

    @AfterEach
    void cleanUp() {
        Telemetry.clear();
    }

    @Test
    void nestedScopeInheritsCorrelationButIsolatesAttributes() {
        Telemetry.setTraceId("trace-parent");
        Telemetry.capture("parent", true);
        Telemetry.journey("order.checkout", "order-42");

        try (Telemetry.Scope ignored = Telemetry.startScope()) {
            assertEquals("trace-parent", Telemetry.getTraceId());
            assertEquals("order-42", Telemetry.readBusinessOutcome().getJourneyId());
            assertNull(Telemetry.read().get("parent"));
            Telemetry.capture("child", true);
        }

        assertEquals(true, Telemetry.read().get("parent"));
        assertNull(Telemetry.read().get("child"));
    }

    @Test
    void closingLastScopeRemovesThreadContext() {
        try (Telemetry.Scope ignored = Telemetry.startScope()) {
            Telemetry.setTraceId("trace");
        }

        assertNull(Telemetry.getTraceId());
    }

    @Test
    void capturesBusinessOutcomeContext() {
        Telemetry.outcome("payment.approval");
        Telemetry.journey("order.checkout", "order-42");
        Telemetry.stage("payment");
        Telemetry.reason("ACQUIRER_TIMEOUT");
        Telemetry.value(new BigDecimal("349.90"), "BRL");

        assertEquals("payment.approval", Telemetry.readBusinessOutcome().getName());
        assertEquals("order-42", Telemetry.readBusinessOutcome().getJourneyId());
        assertEquals(new BigDecimal("349.90"), Telemetry.readBusinessOutcome().getValue());
    }
}
