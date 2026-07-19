package io.echotrace.demo.payment;

public final class PaymentReceipt {

    private final String paymentId;
    private final String status;

    public PaymentReceipt(String paymentId, String status) {
        this.paymentId = paymentId;
        this.status = status;
    }

    public String getPaymentId() { return paymentId; }
    public String getStatus() { return status; }
}
