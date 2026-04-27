package io.echotrace.model;

import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.Map;

public class EventPayload {

    private final String eventName;
    private final String environment;
    private final Instant timestamp;
    private final long durationMs;
    private final Map<String, Object> metadata;

    private final String traceId;
    private final String spanId;

    public EventPayload(
            String event,
            String environment,
            long durationMs,
            String traceId,
            String spanId,
            Map<String, Object> metadata
    ) {
        this.eventName = event;
        this.environment = environment;
        this.traceId = traceId;
        this.spanId = spanId;
        this.timestamp = Instant.now();
        this.durationMs = durationMs;
        this.metadata = Map.copyOf(metadata);
    }

    public String getEventName() { return eventName; }
    public String getEnvironment() { return environment; }
    public Instant getTimestamp() { return timestamp; }
    public long getDurationMs() {return durationMs;}
    public Map<String, Object> getMetadata() { return metadata; }
    public String getTraceId() {return traceId;}
    public String getSpanId() {return spanId;}

    @Override
    public String toString() {
        return "EventPayload{" +
                "eventName='" + eventName + '\'' +
                ", environment='" + environment + '\'' +
                ", timestamp=" + timestamp +
                ", durationMs=" + durationMs +
                ", metadata=" + metadata +
                ", traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                '}';
    }
}
