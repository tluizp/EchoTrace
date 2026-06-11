package io.echotrace.model;

import java.time.Instant;
import java.util.Map;

public class EventPayload {

    String eventName;
    String serviceName;
    String environment;
    String status;
    long durationMs;
    String traceId;
    String spanId;
    Instant createdAt;
    Map<String, Object> payload;

    public EventPayload(){}

    public EventPayload(String eventName,
                        String serviceName,
                        String environment,
                        String status, long durationMs, String traceId, String spanId,
                        Instant createdAt, Map<String, Object> payload) {
        this.eventName = eventName;
        this.serviceName = serviceName;
        this.environment = environment;
        this.status = status;
        this.durationMs = durationMs;
        this.traceId = traceId;
        this.spanId = spanId;
        this.createdAt = createdAt;
        this.payload = (payload != null) ? Map.copyOf(payload) : Map.of();
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
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
                "eventName='" + eventName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", environment='" + environment + '\'' +
                ", status='" + status + '\'' +
                ", durationMs=" + durationMs +
                ", traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", createdAt=" + createdAt +
                ", payload=" + payload +
                '}';
    }
}
