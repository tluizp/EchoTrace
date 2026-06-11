package io.echotrace.collector.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class EventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String eventName;

    private String serviceName;

    private String environment;

    private String status;

    private long durationMs;

    private String traceId;

    private String spanId;

    private Instant createdAt;

    @Type(type = "jsonb")
    private Map<String, Object> payload;

    public EventEntity() {
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
}
