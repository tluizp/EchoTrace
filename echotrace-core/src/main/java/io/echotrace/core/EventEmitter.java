package io.echotrace.core;

import io.echotrace.model.EventPayload;
import io.echotrace.model.BusinessOutcome;
import io.echotrace.telemetry.Telemetry;

import java.time.Clock;
import java.time.Instant;
import java.math.BigDecimal;
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
    private final String serviceVersion;
    private final String deploymentId;
    private final String commitSha;

    public EventEmitter(EventPublisher publisher, String serviceName, String environment) {
        this(publisher, serviceName, environment, null, null, null);
    }

    public EventEmitter(EventPublisher publisher, String serviceName, String environment,
                        String serviceVersion, String deploymentId, String commitSha) {
        this(publisher, serviceName, environment, serviceVersion, deploymentId, commitSha,
                Clock.systemUTC(), new AttributeSanitizer());
    }

    EventEmitter(EventPublisher publisher, String serviceName, String environment, Clock clock,
                 AttributeSanitizer sanitizer) {
        this(publisher, serviceName, environment, null, null, null, clock, sanitizer);
    }

    EventEmitter(EventPublisher publisher, String serviceName, String environment,
                 String serviceVersion, String deploymentId, String commitSha, Clock clock,
                 AttributeSanitizer sanitizer) {
        this.publisher = java.util.Objects.requireNonNull(publisher, "publisher");
        this.serviceName = serviceName;
        this.environment = environment;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        this.sanitizer = java.util.Objects.requireNonNull(sanitizer, "sanitizer");
        this.serviceVersion = serviceVersion;
        this.deploymentId = deploymentId;
        this.commitSha = commitSha;
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
        private String outcomeName;
        private String journeyId;
        private String journeyType;
        private String stage;
        private String reason;
        private BigDecimal value;
        private String currency;

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

        public EventBuilder outcome(String outcomeName) {
            this.outcomeName = outcomeName;
            return this;
        }

        public EventBuilder journey(String journeyType, String journeyId) {
            this.journeyType = journeyType;
            this.journeyId = journeyId;
            return this;
        }

        public EventBuilder stage(String stage) {
            this.stage = stage;
            return this;
        }

        public EventBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public EventBuilder value(BigDecimal value, String currency) {
            this.value = value;
            this.currency = currency;
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
                    sanitizer.sanitize(attributes),
                    new BusinessOutcome(outcomeName, journeyId, journeyType, stage, reason, value, currency),
                    serviceVersion,
                    deploymentId,
                    commitSha
            );
            publisher.publish(event);
            return event;
        }
    }
}
