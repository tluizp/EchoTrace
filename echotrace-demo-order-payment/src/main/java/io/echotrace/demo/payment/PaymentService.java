package io.echotrace.demo.payment;

import io.echotrace.annotation.EchoTrace;
import io.echotrace.telemetry.Telemetry;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    @EchoTrace(
            name = "payment.processed",
            outcome = "payment.approval",
            journey = "order.checkout",
            stage = "payment",
            correlationId = "request.orderId",
            value = "request.amount",
            currency = "BRL",
            capture = {"request.customerId"}
    )
    public PaymentReceipt process(PaymentRequest request) {
        if (request.isSimulatedFailure()) {
            Telemetry.reason("SIMULATED_ACQUIRER_TIMEOUT");
            throw new PaymentDeclinedException("Simulated acquirer timeout");
        }
        return new PaymentReceipt(UUID.randomUUID().toString(), "APPROVED");
    }
}
