package io.echotrace.collector.slo.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.repository.EventRepository;
import io.echotrace.collector.slo.config.BusinessSloProperties;
import io.echotrace.collector.slo.response.SloEvaluationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BusinessSloService {

    private static final BigDecimal CRITICAL_GAP = new BigDecimal("5.00");

    private final EventRepository repository;
    private final BusinessSloProperties properties;

    public BusinessSloService(EventRepository repository, BusinessSloProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public List<SloEvaluationResponse> evaluateAll(Instant end) {
        if (end == null) {
            throw new IllegalArgumentException("Evaluation end must not be null");
        }
        return properties.getBusinessSlos().stream()
                .map(definition -> evaluate(definition, end))
                .collect(Collectors.toList());
    }

    private SloEvaluationResponse evaluate(BusinessSloProperties.Definition definition, Instant end) {
        Instant start = end.minus(definition.getWindowMinutes(), ChronoUnit.MINUTES);
        List<EventEntity> events = repository
                .findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc(
                        definition.getJourneyType(), start, end);
        Set<String> journeys = events.stream()
                .map(EventEntity::getJourneyId)
                .filter(this::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> completed = events.stream()
                .filter(event -> definition.getCompletionStage().equals(event.getJourneyStage()))
                .filter(event -> event.getStatus() != null
                        && "SUCCESS".equals(event.getStatus().toUpperCase(Locale.ROOT)))
                .map(EventEntity::getJourneyId)
                .filter(journeys::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        BigDecimal measured = percentage(completed.size(), journeys.size());
        BigDecimal objective = definition.getObjectivePercentage().setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = journeys.isEmpty() ? null : measured.subtract(objective);
        EvaluationState state = state(journeys.size(), definition.getMinimumJourneys(), measured, objective);

        return new SloEvaluationResponse(
                definition.getName(), definition.getJourneyType(), definition.getCompletionStage(),
                objective, start, end, journeys.size(), completed.size(), measured, remaining,
                state.status, state.alertTriggered, state.severity,
                message(state.status, measured, objective, journeys.size(), definition.getMinimumJourneys()));
    }

    private EvaluationState state(long total, int minimum, BigDecimal measured, BigDecimal objective) {
        if (total == 0) return new EvaluationState("NO_DATA", false, "NONE");
        if (total < minimum) return new EvaluationState("INSUFFICIENT_DATA", false, "NONE");
        if (measured.compareTo(objective) >= 0) {
            return new EvaluationState("HEALTHY", false, "NONE");
        }
        BigDecimal gap = objective.subtract(measured);
        String severity = gap.compareTo(CRITICAL_GAP) >= 0 ? "CRITICAL" : "WARNING";
        return new EvaluationState("BREACHED", true, severity);
    }

    private String message(String status, BigDecimal measured, BigDecimal objective,
                           long total, int minimum) {
        if ("NO_DATA".equals(status)) return "No journeys were observed in the evaluation window";
        if ("INSUFFICIENT_DATA".equals(status)) {
            return "Observed " + total + " journeys; at least " + minimum + " are required";
        }
        if ("HEALTHY".equals(status)) return "Business SLO objective is being met";
        return "Conversion is " + objective.subtract(measured).setScale(2, RoundingMode.HALF_UP)
                + " percentage points below objective";
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class EvaluationState {
        private final String status;
        private final boolean alertTriggered;
        private final String severity;

        private EvaluationState(String status, boolean alertTriggered, String severity) {
            this.status = status;
            this.alertTriggered = alertTriggered;
            this.severity = severity;
        }
    }
}
