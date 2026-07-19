package io.echotrace.collector.journey.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.journey.response.FunnelStepResponse;
import io.echotrace.collector.journey.response.JourneyFunnelResponse;
import io.echotrace.collector.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JourneyFunnelService {

    private final EventRepository repository;

    public JourneyFunnelService(EventRepository repository) {
        this.repository = repository;
    }

    public JourneyFunnelResponse build(String journeyType, Instant start, Instant end) {
        validate(journeyType, start, end);
        String normalizedType = journeyType.trim();
        List<EventEntity> events = repository
                .findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc(normalizedType, start, end);

        LinkedHashMap<String, List<EventEntity>> byStage = events.stream()
                .filter(this::hasJourneyAndStage)
                .collect(Collectors.groupingBy(
                        event -> event.getJourneyStage().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        if (byStage.isEmpty()) {
            return new JourneyFunnelResponse(normalizedType, start, end, 0, 0, 0,
                    percentage(0, 0), Collections.emptyList());
        }

        Set<String> cohort = journeyIds(byStage.values().iterator().next());
        Set<String> previousReached = new LinkedHashSet<>(cohort);
        List<FunnelStepResponse> steps = new ArrayList<>();
        int position = 1;

        for (Map.Entry<String, List<EventEntity>> entry : byStage.entrySet()) {
            Set<String> stageJourneys = journeyIds(entry.getValue());
            Set<String> reached = intersection(previousReached, stageJourneys);
            Set<String> failed = failedJourneyIds(entry.getValue(), reached);
            long successful = reached.size() - failed.size();
            long dropOff = previousReached.size() - reached.size();

            steps.add(new FunnelStepResponse(
                    position++, entry.getKey(), reached.size(), successful, failed.size(), dropOff,
                    percentage(reached.size(), previousReached.size()),
                    percentage(reached.size(), cohort.size())));
            previousReached = reached;
        }

        Set<String> failedJourneys = failedJourneyIds(events, cohort);
        long completed = previousReached.size();
        return new JourneyFunnelResponse(
                normalizedType, start, end, cohort.size(), completed, failedJourneys.size(),
                percentage(completed, cohort.size()), steps);
    }

    private void validate(String journeyType, Instant start, Instant end) {
        if (journeyType == null || journeyType.trim().isEmpty()) {
            throw new IllegalArgumentException("journeyType must not be blank");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end are required");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    private boolean hasJourneyAndStage(EventEntity event) {
        return event.getJourneyId() != null && !event.getJourneyId().trim().isEmpty()
                && event.getJourneyStage() != null && !event.getJourneyStage().trim().isEmpty();
    }

    private Set<String> journeyIds(List<EventEntity> events) {
        return events.stream().map(EventEntity::getJourneyId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> failedJourneyIds(List<EventEntity> events, Set<String> allowed) {
        return events.stream()
                .filter(event -> allowed.contains(event.getJourneyId()))
                .filter(event -> event.getStatus() != null
                        && "ERROR".equals(event.getStatus().toUpperCase(Locale.ROOT)))
                .map(EventEntity::getJourneyId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> intersection(Set<String> left, Set<String> right) {
        Set<String> result = new LinkedHashSet<>(left);
        result.retainAll(right);
        return result;
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
