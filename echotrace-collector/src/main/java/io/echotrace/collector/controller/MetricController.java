package io.echotrace.collector.controller;

import io.echotrace.collector.metrics.response.MetricResponse;
import io.echotrace.collector.metrics.response.enums.MetricInterval;
import io.echotrace.collector.metrics.response.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricsService service;

    public MetricController(MetricsService service) {
        this.service = service;
    }

    @GetMapping("/count")
    public MetricResponse count(
            @RequestParam String eventName,
            @RequestParam(defaultValue = "minute") MetricInterval interval,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return service.countEvents(eventName, interval.getPostgresValue(), start, end);
    }
}
