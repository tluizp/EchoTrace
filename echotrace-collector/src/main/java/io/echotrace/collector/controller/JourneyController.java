package io.echotrace.collector.controller;

import io.echotrace.collector.journey.response.JourneyResponse;
import io.echotrace.collector.journey.response.JourneyFunnelResponse;
import io.echotrace.collector.journey.service.JourneyFunnelService;
import io.echotrace.collector.journey.service.JourneyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/journeys")
public class JourneyController {

    private final JourneyService service;
    private final JourneyFunnelService funnelService;

    public JourneyController(JourneyService service, JourneyFunnelService funnelService) {
        this.service = service;
        this.funnelService = funnelService;
    }

    @GetMapping("/{journeyId}")
    public ResponseEntity<JourneyResponse> findById(@PathVariable String journeyId) {
        return service.findById(journeyId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/types/{journeyType}/funnel")
    public ResponseEntity<JourneyFunnelResponse> funnel(
            @PathVariable String journeyType,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        try {
            return ResponseEntity.ok(funnelService.build(journeyType, start, end));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
