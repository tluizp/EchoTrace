package io.echotrace.collector.mapper;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.model.EventPayload;
import io.echotrace.model.BusinessOutcome;

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
        entity.setServiceVersion(payload.getServiceVersion());
        entity.setDeploymentId(payload.getDeploymentId());
        entity.setCommitSha(payload.getCommitSha());

        BusinessOutcome outcome = payload.getBusinessOutcome();
        if (outcome != null) {
            entity.setOutcomeName(outcome.getName());
            entity.setJourneyId(outcome.getJourneyId());
            entity.setJourneyType(outcome.getJourneyType());
            entity.setJourneyStage(outcome.getStage());
            entity.setOutcomeReason(outcome.getReason());
            entity.setBusinessValue(outcome.getValue());
            entity.setCurrency(outcome.getCurrency());
        }

        return entity;
    }
}
