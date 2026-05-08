package io.echotrace.collector.metrics.response.service;

import io.echotrace.collector.metrics.response.MetricResponse;
import io.echotrace.collector.metrics.response.projection.EventCountProjection;
import io.echotrace.collector.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
                .toList();

        List<Long> values = result.stream()
                .map(EventCountProjection::getTotal)
                .toList();

        return new MetricResponse(labels, values);
    }
}
