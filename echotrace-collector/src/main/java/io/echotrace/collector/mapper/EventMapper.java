package io.echotrace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.echotrace.collector.entity.EventEntity;
import io.echotrace.model.EventPayload;

public class EventMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public EventEntity toEntity(EventPayload payload) {
        EventEntity entity = new EventEntity();
        entity.setEventName(payload.getEvent());
        entity.setTimestamp(payload.getTimestamp());

        try {
            entity.setMetadata(payload.getMetadata());
        } catch (Exception e) {
            throw new RuntimeException("Error serializing payload", e);
        }

        return entity;
    }
}
