package io.echotrace.collector.controller;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.metrics.response.MetricResponse;
import io.echotrace.collector.metrics.response.MetricsOverviewResponse;
import io.echotrace.collector.metrics.response.enums.MetricInterval;
import io.echotrace.collector.metrics.response.service.MetricsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricsService service;

    public MetricController(MetricsService service) {
        this.service = service;
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public MetricResponse count(
            @RequestParam String eventName,
            @RequestParam(defaultValue = "MINUTE") MetricInterval interval,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return service.countEvents(eventName, interval.getPostgresValue(), start, end);
    }

    @GetMapping(value = "/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public MetricsOverviewResponse getOverview(
            @RequestParam String eventName,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {

        return service.getOverview(eventName, start, end);
    }

    @GetMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EventEntity> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam Instant start,
            @RequestParam Instant end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.getLogs(search, start, end, page, size);
    }
}
