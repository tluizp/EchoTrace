package io.echotrace.collector.controller;

import io.echotrace.collector.slo.response.SloEvaluationResponse;
import io.echotrace.collector.slo.service.BusinessSloService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/slos")
public class BusinessSloController {

    private final BusinessSloService service;

    public BusinessSloController(BusinessSloService service) {
        this.service = service;
    }

    @GetMapping("/evaluations")
    public List<SloEvaluationResponse> evaluate(
            @RequestParam(required = false) Instant end) {
        return service.evaluateAll(end == null ? Instant.now() : end);
    }
}
