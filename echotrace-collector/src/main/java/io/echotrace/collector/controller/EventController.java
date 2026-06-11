package io.echotrace.collector.controller;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.mapper.EventMapper;
import io.echotrace.collector.metrics.response.EventResponse;
import io.echotrace.collector.metrics.response.service.MetricsService;
import io.echotrace.collector.repository.EventRepository;
import io.echotrace.model.EventPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository repository;
    private final MetricsService service;
    private final EventMapper mapper = new EventMapper();

    public EventController(EventRepository repository, MetricsService service) {
        this.repository = repository;
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody EventPayload payload) {
        EventEntity entity = mapper.toEntity(payload);
        repository.save(entity);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("names")
    public List<EventResponse> list() {
        return service.findAll();
    }
}