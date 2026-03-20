package com.event.trace.model;

import java.time.Instant;
import java.util.Map;

public class EventPayload {

    private final String event;
    private final Instant timestamp;
    private final long durationMs;
    private final Map<String, Object> metadata;

    private final String traceId;
    private final String spanId;

    public EventPayload(
            String event,
            long durationMs,
            String traceId,
            String spanId,
            Map<String, Object> metadata
    ) {
        this.event = event;
        this.traceId = traceId;
        this.spanId = spanId;
        this.timestamp = Instant.now();
        this.durationMs = durationMs;
        this.metadata = Map.copyOf(metadata);
    }

    public String getEvent() { return event; }
    public Instant getTimestamp() { return timestamp; }
    public long getDurationMs() {return durationMs;}
    public Map<String, Object> getMetadata() { return metadata; }
    public String getTraceId() {return traceId;}
    public String getSpanId() {return spanId;}

    @Override
    public String toString() {
        return "EventPayload{" +
                "event='" + event + '\'' +
                ", timestamp=" + timestamp +
                ", durationMs=" + durationMs +
                ", metadata=" + metadata +
                ", traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                '}';
    }
}
