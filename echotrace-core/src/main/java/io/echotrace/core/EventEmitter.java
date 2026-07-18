package io.echotrace.core;

import io.echotrace.model.EventPayload;
import io.echotrace.telemetry.Telemetry;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Programmatic API for business events that are not tied to a method boundary. */
public final class EventEmitter {

    private final EventPublisher publisher;
    private final String serviceName;
    private final String environment;
    private final Clock clock;
    private final AttributeSanitizer sanitizer;

    public EventEmitter(EventPublisher publisher, String serviceName, String environment) {
        this(publisher, serviceName, environment, Clock.systemUTC(), new AttributeSanitizer());
    }

    EventEmitter(EventPublisher publisher, String serviceName, String environment, Clock clock,
                 AttributeSanitizer sanitizer) {
        this.publisher = java.util.Objects.requireNonNull(publisher, "publisher");
        this.serviceName = serviceName;
        this.environment = environment;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        this.sanitizer = java.util.Objects.requireNonNull(sanitizer, "sanitizer");
    }

    public EventBuilder event(String eventName) {
        return new EventBuilder(eventName);
    }

    public final class EventBuilder {
        private final String eventName;
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private int eventVersion = 1;
        private String status = "SUCCESS";
        private long durationMs;

        private EventBuilder(String eventName) {
            if (eventName == null || eventName.trim().isEmpty()) {
                throw new IllegalArgumentException("eventName must not be blank");
            }
            this.eventName = eventName;
        }

        public EventBuilder version(int eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public EventBuilder attribute(String key, Object value) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("attribute key must not be blank");
            }
            attributes.put(key, value);
            return this;
        }

        public EventBuilder status(String status) {
            this.status = status;
            return this;
        }

        public EventBuilder durationMs(long durationMs) {
            if (durationMs < 0) {
                throw new IllegalArgumentException("durationMs must not be negative");
            }
            this.durationMs = durationMs;
            return this;
        }

        public EventPayload emit() {
            Instant now = clock.instant();
            String traceId = Telemetry.getTraceId();
            String spanId = Telemetry.getSpanId();
            EventPayload event = new EventPayload(
                    EventPayload.CURRENT_SPEC_VERSION,
                    UUID.randomUUID().toString(),
                    eventVersion,
                    eventName,
                    serviceName,
                    environment,
                    status,
                    durationMs,
                    traceId,
                    spanId,
                    now,
                    now,
                    sanitizer.sanitize(attributes)
            );
            publisher.publish(event);
            return event;
        }
    }
}
