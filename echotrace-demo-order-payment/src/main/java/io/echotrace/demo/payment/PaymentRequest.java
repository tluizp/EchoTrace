package io.echotrace.demo.payment;

import java.math.BigDecimal;

public final class PaymentRequest {

    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final boolean simulatedFailure;

    public PaymentRequest(String orderId, String customerId, BigDecimal amount,
                          boolean simulatedFailure) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.simulatedFailure = simulatedFailure;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public boolean isSimulatedFailure() { return simulatedFailure; }
}
