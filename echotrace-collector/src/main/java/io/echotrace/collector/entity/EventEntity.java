package io.echotrace.collector.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class EventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String specVersion;

    @Column(unique = true)
    private String eventId;

    private int eventVersion;

    private String eventName;

    private String serviceName;

    private String environment;

    private String status;

    private long durationMs;

    private String traceId;

    private String spanId;

    private String outcomeName;

    private String journeyId;

    private String journeyType;

    private String journeyStage;

    private String outcomeReason;

    @Column(precision = 19, scale = 4)
    private BigDecimal businessValue;

    private String currency;

    private String serviceVersion;

    private String deploymentId;

    private String commitSha;

    private Instant createdAt;

    private Instant observedAt;

    @Type(type = "jsonb")
    private Map<String, Object> payload;

    public EventEntity() {
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    public EventEntity(UUID id, String eventName, String serviceName, String environment,
                       String status, long durationMs, String traceId, String spanId,
                       Instant createdAt, Map<String, Object> payload) {
        this.id = id;
        this.eventName = eventName;
        this.serviceName = serviceName;
        this.environment = environment;
        this.status = status;
        this.durationMs = durationMs;
        this.traceId = traceId;
        this.spanId = spanId;
        this.createdAt = createdAt;
        this.payload = payload;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOutcomeName() { return outcomeName; }
    public void setOutcomeName(String outcomeName) { this.outcomeName = outcomeName; }
    public String getJourneyId() { return journeyId; }
    public void setJourneyId(String journeyId) { this.journeyId = journeyId; }
    public String getJourneyType() { return journeyType; }
    public void setJourneyType(String journeyType) { this.journeyType = journeyType; }
    public String getJourneyStage() { return journeyStage; }
    public void setJourneyStage(String journeyStage) { this.journeyStage = journeyStage; }
    public String getOutcomeReason() { return outcomeReason; }
    public void setOutcomeReason(String outcomeReason) { this.outcomeReason = outcomeReason; }
    public BigDecimal getBusinessValue() { return businessValue; }
    public void setBusinessValue(BigDecimal businessValue) { this.businessValue = businessValue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }
    public String getDeploymentId() { return deploymentId; }
    public void setDeploymentId(String deploymentId) { this.deploymentId = deploymentId; }
    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
}
