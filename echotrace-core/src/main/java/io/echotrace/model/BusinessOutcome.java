package io.echotrace.model;

import java.math.BigDecimal;

/**
 * Describes the business meaning of an event. All fields are optional so that
 * technical events and payloads produced with specification 1.x remain valid.
 */
public final class BusinessOutcome {

    private String name;
    private String journeyId;
    private String journeyType;
    private String stage;
    private String reason;
    private BigDecimal value;
    private String currency;

    public BusinessOutcome() {
    }

    public BusinessOutcome(String name, String journeyId, String journeyType,
                           String stage, String reason, BigDecimal value, String currency) {
        this.name = normalize(name);
        this.journeyId = normalize(journeyId);
        this.journeyType = normalize(journeyType);
        this.stage = normalize(stage);
        this.reason = normalize(reason);
        this.value = value;
        this.currency = normalize(currency);
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException("Business outcome value must not be negative");
        }
        if (currency != null && value == null) {
            throw new IllegalArgumentException("Business outcome currency requires a value");
        }
    }

    public boolean isEmpty() {
        return name == null && journeyId == null && journeyType == null && stage == null
                && reason == null && value == null && currency == null;
    }

    public String getName() {
        return name;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public String getJourneyType() {
        return journeyType;
    }

    public String getStage() {
        return stage;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
