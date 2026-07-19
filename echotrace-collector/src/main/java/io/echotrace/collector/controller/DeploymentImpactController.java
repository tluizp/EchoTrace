package io.echotrace.collector.controller;

import io.echotrace.collector.deployment.response.DeploymentImpactResponse;
import io.echotrace.collector.deployment.service.DeploymentImpactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/journeys/types/{journeyType}/deployments")
public class DeploymentImpactController {

    private final DeploymentImpactService service;

    public DeploymentImpactController(DeploymentImpactService service) {
        this.service = service;
    }

    @GetMapping("/{deploymentId}/impact")
    public ResponseEntity<DeploymentImpactResponse> impact(
            @PathVariable String journeyType,
            @PathVariable String deploymentId,
            @RequestParam String serviceName,
            @RequestParam String completionStage,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        try {
            return service.analyze(
                            journeyType, serviceName, deploymentId, completionStage, start, end)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
