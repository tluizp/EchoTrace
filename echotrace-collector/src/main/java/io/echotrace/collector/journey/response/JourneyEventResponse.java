package io.echotrace.collector.journey.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public final class JourneyEventResponse {

    private final String eventId;
    private final String eventName;
    private final String outcome;
    private final String stage;
    private final String status;
    private final String reason;
    private final String serviceName;
    private final String serviceVersion;
    private final String deploymentId;
    private final String commitSha;
    private final String traceId;
    private final String spanId;
    private final Instant createdAt;
    private final long durationMs;
    private final BigDecimal value;
    private final String currency;
    private final Map<String, Object> attributes;

    public JourneyEventResponse(String eventId, String eventName, String outcome, String stage,
                                String status, String reason, String serviceName,
                                String serviceVersion, String deploymentId, String commitSha,
                                String traceId, String spanId, Instant createdAt, long durationMs,
                                BigDecimal value, String currency, Map<String, Object> attributes) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.outcome = outcome;
        this.stage = stage;
        this.status = status;
        this.reason = reason;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.deploymentId = deploymentId;
        this.commitSha = commitSha;
        this.traceId = traceId;
        this.spanId = spanId;
        this.createdAt = createdAt;
        this.durationMs = durationMs;
        this.value = value;
        this.currency = currency;
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getOutcome() { return outcome; }
    public String getStage() { return stage; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getServiceName() { return serviceName; }
    public String getServiceVersion() { return serviceVersion; }
    public String getDeploymentId() { return deploymentId; }
    public String getCommitSha() { return commitSha; }
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }
    public Instant getCreatedAt() { return createdAt; }
    public long getDurationMs() { return durationMs; }
    public BigDecimal getValue() { return value; }
    public String getCurrency() { return currency; }
    public Map<String, Object> getAttributes() { return attributes; }
}
