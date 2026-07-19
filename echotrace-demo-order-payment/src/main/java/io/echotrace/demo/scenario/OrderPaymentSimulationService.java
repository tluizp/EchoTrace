package io.echotrace.demo.scenario;

import io.echotrace.core.EventEmitter;
import io.echotrace.demo.payment.PaymentDeclinedException;
import io.echotrace.demo.payment.PaymentRequest;
import io.echotrace.demo.payment.PaymentService;
import io.echotrace.telemetry.Telemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderPaymentSimulationService {

    private static final String JOURNEY_TYPE = "order.checkout";

    private final EventEmitter emitter;
    private final PaymentService paymentService;
    private final String serviceVersion;
    private final String deploymentId;

    public OrderPaymentSimulationService(
            EventEmitter emitter,
            PaymentService paymentService,
            @Value("${echotrace.service-version:unknown}") String serviceVersion,
            @Value("${echotrace.deployment-id:unknown}") String deploymentId) {
        this.emitter = emitter;
        this.paymentService = paymentService;
        this.serviceVersion = serviceVersion;
        this.deploymentId = deploymentId;
    }

    public SimulationResponse run(int orders, int failurePercentage) {
        validate(orders, failurePercentage);
        String scenarioId = UUID.randomUUID().toString();
        int failuresToGenerate = (int) Math.round(orders * (failurePercentage / 100.0));
        int succeeded = 0;
        int failed = 0;
        List<String> journeyIds = new ArrayList<>();

        for (int index = 0; index < orders; index++) {
            String orderId = scenarioId + "-order-" + String.format("%04d", index + 1);
            String customerId = "customer-" + String.format("%03d", (index % 25) + 1);
            BigDecimal amount = BigDecimal.valueOf(100L + index).setScale(2, RoundingMode.HALF_UP);
            boolean shouldFail = index < failuresToGenerate;
            journeyIds.add(orderId);

            try (Telemetry.Scope ignored = Telemetry.startScope()) {
                Telemetry.setTraceId(UUID.randomUUID().toString());
                Telemetry.setSpanId(UUID.randomUUID().toString());
                Telemetry.journey(JOURNEY_TYPE, orderId);

                emit("checkout.started", "checkout.start", "checkout", orderId, amount);
                emit("order.created", "order.creation", "order", orderId, amount);

                try {
                    paymentService.process(new PaymentRequest(orderId, customerId, amount, shouldFail));
                    emit("order.confirmed", "order.confirmation", "confirmed", orderId, amount);
                    succeeded++;
                } catch (PaymentDeclinedException exception) {
                    failed++;
                }
            }
        }

        return new SimulationResponse(
                scenarioId, orders, succeeded, failed, failurePercentage, Instant.now(),
                journeyIds, serviceVersion, deploymentId);
    }

    private void emit(String eventName, String outcome, String stage,
                      String orderId, BigDecimal amount) {
        emitter.event(eventName)
                .outcome(outcome)
                .journey(JOURNEY_TYPE, orderId)
                .stage(stage)
                .value(amount, "BRL")
                .attribute("order.id", orderId)
                .emit();
    }

    private void validate(int orders, int failurePercentage) {
        if (orders < 1 || orders > 1000) {
            throw new IllegalArgumentException("orders must be between 1 and 1000");
        }
        if (failurePercentage < 0 || failurePercentage > 100) {
            throw new IllegalArgumentException("failurePercentage must be between 0 and 100");
        }
    }
}
