package io.echotrace.demo.scenario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class OrderPaymentDemoController {

    private final OrderPaymentSimulationService service;

    public OrderPaymentDemoController(OrderPaymentSimulationService service) {
        this.service = service;
    }

    @org.springframework.web.bind.annotation.GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "order-payment-demo");
    }

    @PostMapping("/order-to-payment")
    public ResponseEntity<SimulationResponse> simulate(
            @RequestParam(defaultValue = "100") int orders,
            @RequestParam(defaultValue = "5") int failurePercentage) {
        try {
            return ResponseEntity.ok(service.run(orders, failurePercentage));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
