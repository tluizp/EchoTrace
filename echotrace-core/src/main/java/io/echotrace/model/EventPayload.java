package io.echotrace.model;

import java.time.Instant;
import java.util.Map;

public class EventPayload {

    public static final String CURRENT_SPEC_VERSION = "1.0";

    String specVersion;
    String eventId;
    int eventVersion;
    String eventName;
    String serviceName;
    String environment;
    String status;
    long durationMs;
    String traceId;
    String spanId;
    Instant createdAt;
    Instant observedAt;
    Map<String, Object> payload;

    public EventPayload(){
        this.specVersion = CURRENT_SPEC_VERSION;
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventVersion = 1;
        this.observedAt = Instant.now();
        this.payload = Map.of();
    }

    public EventPayload(String eventName,
                        String serviceName,
                        String environment,
                        String status, long durationMs, String traceId, String spanId,
                        Instant createdAt, Map<String, Object> payload) {
        this(CURRENT_SPEC_VERSION, java.util.UUID.randomUUID().toString(), 1, eventName,
                serviceName, environment, status, durationMs, traceId, spanId,
                createdAt, Instant.now(), payload);
    }

    public EventPayload(String specVersion, String eventId, int eventVersion,
                        String eventName, String serviceName, String environment,
                        String status, long durationMs, String traceId, String spanId,
                        Instant createdAt, Instant observedAt, Map<String, Object> payload) {
        this.specVersion = requireText(specVersion, "specVersion");
        this.eventId = requireText(eventId, "eventId");
        if (eventVersion < 1) {
            throw new IllegalArgumentException("eventVersion must be greater than zero");
        }
        this.eventVersion = eventVersion;
        this.eventName = requireText(eventName, "eventName");
        this.serviceName = serviceName;
        this.environment = environment;
        this.status = status;
        this.durationMs = durationMs;
        this.traceId = traceId;
        this.spanId = spanId;
        this.createdAt = java.util.Objects.requireNonNull(createdAt, "createdAt");
        this.observedAt = java.util.Objects.requireNonNull(observedAt, "observedAt");
        this.payload = payload == null
                ? Map.of()
                : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(payload));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public String getEventId() {
        return eventId;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getStatus() {
        return status;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    @Override
    public String toString() {
        return "EventPayload{" +
                "specVersion='" + specVersion + '\'' +
                ", eventId='" + eventId + '\'' +
                ", eventVersion=" + eventVersion +
                ", eventName='" + eventName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", environment='" + environment + '\'' +
                ", status='" + status + '\'' +
                ", durationMs=" + durationMs +
                ", traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", createdAt=" + createdAt +
                ", observedAt=" + observedAt +
                ", payload=" + payload +
                '}';
    }
}
