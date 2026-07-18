package io.echotrace.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

        try (Telemetry.Scope ignored = Telemetry.startScope()) {
            assertEquals("trace-parent", Telemetry.getTraceId());
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
}
