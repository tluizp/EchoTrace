package io.echotrace.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessOutcomeTest {

    @Test
    void representsJourneyAndFinancialImpact() {
        BusinessOutcome outcome = new BusinessOutcome(
                "payment.approval", "order-42", "order.checkout", "payment",
                "ACQUIRER_TIMEOUT", new BigDecimal("349.90"), "BRL");

        assertEquals("payment.approval", outcome.getName());
        assertEquals("order-42", outcome.getJourneyId());
        assertEquals(new BigDecimal("349.90"), outcome.getValue());
    }

    @Test
    void rejectsCurrencyWithoutValue() {
        assertThrows(IllegalArgumentException.class, () -> new BusinessOutcome(
                "payment.approval", null, null, null, null, null, "BRL"));
    }
}
