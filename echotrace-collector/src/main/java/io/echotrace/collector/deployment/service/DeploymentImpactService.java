package io.echotrace.collector.deployment.service;

import io.echotrace.collector.deployment.response.DeploymentEventMetrics;
import io.echotrace.collector.deployment.response.DeploymentImpactResponse;
import io.echotrace.collector.deployment.response.JourneyPeriodMetrics;
import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DeploymentImpactService {

    static final int MINIMUM_SAMPLE_SIZE = 20;
    private static final BigDecimal SIGNAL_THRESHOLD = new BigDecimal("1.00");

    private final EventRepository repository;

    public DeploymentImpactService(EventRepository repository) {
        this.repository = repository;
    }

    public Optional<DeploymentImpactResponse> analyze(
            String journeyType, String serviceName, String deploymentId,
            String completionStage, Instant start, Instant end) {
        validate(journeyType, serviceName, deploymentId, completionStage, start, end);
        String type = journeyType.trim();
        String service = serviceName.trim();
        String deployment = deploymentId.trim();
        String completedStage = completionStage.trim();

        List<EventEntity> events = repository
                .findByJourneyTypeAndCreatedAtBetweenOrderByCreatedAtAsc(type, start, end);
        List<EventEntity> deploymentEvents = events.stream()
                .filter(event -> service.equals(event.getServiceName()))
                .filter(event -> deployment.equals(event.getDeploymentId()))
                .collect(Collectors.toList());
        if (deploymentEvents.isEmpty()) {
            return Optional.empty();
        }

        Instant cutover = deploymentEvents.stream().map(EventEntity::getCreatedAt)
                .filter(java.util.Objects::nonNull).min(Instant::compareTo).orElse(null);
        if (cutover == null) {
            return Optional.empty();
        }

        List<JourneyAggregate> journeys = aggregateJourneys(events);
        JourneyPeriodMetrics before = periodMetrics(
                journeys, journey -> journey.startedAt.isBefore(cutover)
                        && !journey.usesDeployment(service, deployment),
                completedStage, start, cutover);
        JourneyPeriodMetrics after = periodMetrics(
                journeys, journey -> !journey.startedAt.isBefore(cutover)
                        || journey.usesDeployment(service, deployment),
                completedStage, cutover, end);
        DeploymentEventMetrics targetMetrics = deploymentMetrics(deploymentEvents);
        BigDecimal conversionDelta = after.getConversionRate().subtract(before.getConversionRate());
        BigDecimal failureDelta = after.getFailureRate().subtract(before.getFailureRate());
        boolean sufficient = before.getTotalJourneys() >= MINIMUM_SAMPLE_SIZE
                && after.getTotalJourneys() >= MINIMUM_SAMPLE_SIZE;
        String signal = signal(conversionDelta, failureDelta, sufficient);

        return Optional.of(new DeploymentImpactResponse(
                type, completedStage, service, deployment, serviceVersion(deploymentEvents), cutover,
                before, after, targetMetrics, conversionDelta, failureDelta, signal,
                MINIMUM_SAMPLE_SIZE, sufficient));
    }

    private List<JourneyAggregate> aggregateJourneys(List<EventEntity> events) {
        Map<String, List<EventEntity>> grouped = events.stream()
                .filter(event -> event.getJourneyId() != null && !event.getJourneyId().trim().isEmpty())
                .collect(Collectors.groupingBy(EventEntity::getJourneyId,
                        LinkedHashMap::new, Collectors.toList()));
        List<JourneyAggregate> journeys = new ArrayList<>();
        grouped.forEach((journeyId, journeyEvents) -> journeyEvents.stream()
                .map(EventEntity::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .min(Instant::compareTo)
                .ifPresent(startedAt -> journeys.add(
                        new JourneyAggregate(startedAt, journeyEvents))));
        return journeys;
    }

    private JourneyPeriodMetrics periodMetrics(
            List<JourneyAggregate> journeys, Predicate<JourneyAggregate> period,
            String completionStage, Instant start, Instant end) {
        List<JourneyAggregate> selected = journeys.stream().filter(period).collect(Collectors.toList());
        long completed = selected.stream().filter(journey -> journey.completed(completionStage)).count();
        long failed = selected.stream().filter(JourneyAggregate::failed).count();
        Map<String, BigDecimal> affectedValues = new LinkedHashMap<>();
        selected.stream().filter(JourneyAggregate::failed).forEach(journey ->
                journey.latestValue().ifPresent(event -> affectedValues.merge(
                        normalizedCurrency(event.getCurrency()), event.getBusinessValue(), BigDecimal::add)));
        return new JourneyPeriodMetrics(
                start, end, selected.size(), completed, failed,
                percentage(completed, selected.size()), percentage(failed, selected.size()),
                affectedValues);
    }

    private DeploymentEventMetrics deploymentMetrics(List<EventEntity> events) {
        List<EventEntity> failed = events.stream().filter(this::isError).collect(Collectors.toList());
        Map<String, Long> reasons = failed.stream()
                .collect(Collectors.groupingBy(
                        event -> normalizedReason(event.getOutcomeReason()), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new));
        return new DeploymentEventMetrics(
                events.size(), failed.size(), percentage(failed.size(), events.size()), reasons);
    }

    private String signal(BigDecimal conversionDelta, BigDecimal failureDelta, boolean sufficient) {
        if (!sufficient) return "INSUFFICIENT_DATA";
        if (conversionDelta.compareTo(SIGNAL_THRESHOLD.negate()) <= 0
                || failureDelta.compareTo(SIGNAL_THRESHOLD) >= 0) return "DEGRADED";
        if (conversionDelta.compareTo(SIGNAL_THRESHOLD) >= 0
                || failureDelta.compareTo(SIGNAL_THRESHOLD.negate()) <= 0) return "IMPROVED";
        return "STABLE";
    }

    private String serviceVersion(List<EventEntity> events) {
        return events.stream().map(EventEntity::getServiceVersion)
                .filter(value -> value != null && !value.trim().isEmpty())
                .reduce((first, second) -> second).orElse(null);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private boolean isError(EventEntity event) {
        return event.getStatus() != null
                && "ERROR".equals(event.getStatus().toUpperCase(Locale.ROOT));
    }

    private String normalizedCurrency(String currency) {
        return currency == null || currency.trim().isEmpty() ? "UNSPECIFIED" : currency.trim();
    }

    private String normalizedReason(String reason) {
        return reason == null || reason.trim().isEmpty() ? "UNSPECIFIED" : reason.trim();
    }

    private void validate(String journeyType, String serviceName, String deploymentId,
                          String completionStage, Instant start, Instant end) {
        requireText(journeyType, "journeyType");
        requireText(serviceName, "serviceName");
        requireText(deploymentId, "deploymentId");
        requireText(completionStage, "completionStage");
        if (start == null || end == null) throw new IllegalArgumentException("start and end are required");
        if (!start.isBefore(end)) throw new IllegalArgumentException("start must be before end");
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private final class JourneyAggregate {
        private final Instant startedAt;
        private final List<EventEntity> events;

        private JourneyAggregate(Instant startedAt, List<EventEntity> events) {
            this.startedAt = startedAt;
            this.events = events;
        }

        private boolean completed(String stage) {
            return events.stream().anyMatch(event -> stage.equals(event.getJourneyStage())
                    && event.getStatus() != null
                    && "SUCCESS".equals(event.getStatus().toUpperCase(Locale.ROOT)));
        }

        private boolean failed() {
            return events.stream().anyMatch(DeploymentImpactService.this::isError);
        }

        private Optional<EventEntity> latestValue() {
            List<EventEntity> reversed = new ArrayList<>(events);
            Collections.reverse(reversed);
            return reversed.stream().filter(event -> event.getBusinessValue() != null).findFirst();
        }

        private boolean usesDeployment(String serviceName, String deploymentId) {
            return events.stream().anyMatch(event -> serviceName.equals(event.getServiceName())
                    && deploymentId.equals(event.getDeploymentId()));
        }
    }
}
