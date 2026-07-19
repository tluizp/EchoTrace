package io.echotrace.collector.journey.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.journey.response.JourneyEventResponse;
import io.echotrace.collector.journey.response.JourneyResponse;
import io.echotrace.collector.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JourneyService {

    private final EventRepository repository;

    public JourneyService(EventRepository repository) {
        this.repository = repository;
    }

    public Optional<JourneyResponse> findById(String journeyId) {
        if (journeyId == null || journeyId.trim().isEmpty()) {
            return Optional.empty();
        }

        List<EventEntity> entities = repository.findByJourneyIdOrderByCreatedAtAsc(journeyId.trim());
        if (entities.isEmpty()) {
            return Optional.empty();
        }

        EventEntity first = entities.get(0);
        boolean failed = entities.stream().anyMatch(this::isError);
        EventEntity valueSource = latestValue(entities);
        BigDecimal affectedValue = failed && valueSource != null ? valueSource.getBusinessValue() : null;
        String currency = affectedValue != null ? valueSource.getCurrency() : null;
        Instant finishedAt = finishedAt(entities);
        long durationMs = durationBetween(first.getCreatedAt(), finishedAt);

        List<JourneyEventResponse> events = entities.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());

        return Optional.of(new JourneyResponse(
                first.getJourneyId(), firstNonBlank(entities, EventEntity::getJourneyType),
                failed ? "FAILED" : "SUCCESS", first.getCreatedAt(), finishedAt,
                durationMs, affectedValue, currency, events));
    }

    private boolean isError(EventEntity event) {
        return event.getStatus() != null
                && "ERROR".equals(event.getStatus().toUpperCase(Locale.ROOT));
    }

    private EventEntity latestValue(List<EventEntity> entities) {
        for (int i = entities.size() - 1; i >= 0; i--) {
            if (entities.get(i).getBusinessValue() != null) {
                return entities.get(i);
            }
        }
        return null;
    }

    private Instant finishedAt(List<EventEntity> entities) {
        return entities.stream()
                .filter(event -> event.getCreatedAt() != null)
                .map(event -> event.getCreatedAt().plusMillis(Math.max(0, event.getDurationMs())))
                .max(Instant::compareTo)
                .orElse(null);
    }

    private long durationBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return Math.max(0, Duration.between(start, end).toMillis());
    }

    private JourneyEventResponse toEventResponse(EventEntity entity) {
        return new JourneyEventResponse(
                entity.getEventId(), entity.getEventName(), entity.getOutcomeName(),
                entity.getJourneyStage(), entity.getStatus(), entity.getOutcomeReason(),
                entity.getServiceName(), entity.getServiceVersion(), entity.getDeploymentId(),
                entity.getCommitSha(), entity.getTraceId(), entity.getSpanId(),
                entity.getCreatedAt(), entity.getDurationMs(), entity.getBusinessValue(),
                entity.getCurrency(), entity.getPayload());
    }

    private String firstNonBlank(List<EventEntity> entities,
                                 java.util.function.Function<EventEntity, String> extractor) {
        return entities.stream().map(extractor).filter(value -> value != null && !value.trim().isEmpty())
                .findFirst().orElse(null);
    }
}
