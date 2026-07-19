package io.echotrace.collector.mapper;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.model.EventPayload;

public class EventMapper {

    public EventEntity toEntity(EventPayload payload) {

        EventEntity entity = new EventEntity();

        entity.setSpecVersion(payload.getSpecVersion());
        entity.setEventId(payload.getEventId());
        entity.setEventVersion(payload.getEventVersion());
        entity.setEventName(payload.getEventName());
        entity.setServiceName(payload.getServiceName());
        entity.setEnvironment(payload.getEnvironment());
        entity.setStatus(payload.getStatus());
        entity.setDurationMs(payload.getDurationMs());
        entity.setTraceId(payload.getTraceId());
        entity.setSpanId(payload.getSpanId());
        entity.setCreatedAt(payload.getCreatedAt());
        entity.setObservedAt(payload.getObservedAt());
        entity.setPayload(payload.getPayload());

        return entity;
    }
}
