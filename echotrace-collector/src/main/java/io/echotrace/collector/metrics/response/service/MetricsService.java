package io.echotrace.collector.metrics.response.service;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.metrics.response.EventResponse;
import io.echotrace.collector.metrics.response.MetricResponse;
import io.echotrace.collector.metrics.response.MetricsOverviewResponse;
import io.echotrace.collector.metrics.response.projection.EventCountProjection;
import io.echotrace.collector.metrics.response.projection.MetricsOverviewProjection;
import io.echotrace.collector.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final EventRepository repository;

    public MetricsService(EventRepository repository) {
        this.repository = repository;
    }

    public MetricResponse countEvents(String eventName, String interval, Instant start, Instant end) {
        List<EventCountProjection> result =
                repository.countEventsGrouped(eventName, interval, start, end);

        List<String> labels = result.stream()
                .map(EventCountProjection::getBucketLabel)
                .collect(Collectors.toList());

        List<Long> values = result.stream()
                .map(EventCountProjection::getTotal)
                .collect(Collectors.toList());

        return new MetricResponse(labels, values);
    }

    public MetricsOverviewResponse getOverview(
            String eventName,
            Instant start,
            Instant end
    ) {

        MetricsOverviewProjection projection =
                repository.getOverviewMetrics(
                        eventName,
                        start,
                        end
                );

        double successRate =
                projection.getSuccessRate() != null
                        ? projection.getSuccessRate()
                        : 0.0;

        return new MetricsOverviewResponse(
                projection.getTotalEvents(),
                successRate,
                projection.getErrorCount()
        );
    }

    public List<EventResponse> findAll() {

        return repository.findDistinctEventNames()
                .stream()
                .map(eventName -> new EventResponse(
                        eventName,
                        formatDisplayName(eventName)
                ))
                .collect(Collectors.toList());
    }

    private String formatDisplayName(String eventName) {

        return Arrays.stream(eventName.split("\\."))
                .map(word ->
                        Character.toUpperCase(word.charAt(0))
                                + word.substring(1)
                )
                .collect(Collectors.joining(" "));
    }

    public List<EventEntity> getLogs(String search, Instant start, Instant end, int page, int size) {
        int offset = page * size;
        // Se a busca estiver vazia, passa null para limpar a query
        String searchParam = (search == null || search.trim().isEmpty()) ? null : search;

        return repository.findLogs(searchParam, start, end, size, offset);
    }
}
