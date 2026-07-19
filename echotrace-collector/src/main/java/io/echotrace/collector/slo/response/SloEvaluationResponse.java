package io.echotrace.collector.slo.response;

import java.math.BigDecimal;
import java.time.Instant;

public final class SloEvaluationResponse {

    private final String name;
    private final String journeyType;
    private final String completionStage;
    private final BigDecimal objectivePercentage;
    private final Instant windowStart;
    private final Instant windowEnd;
    private final long totalJourneys;
    private final long completedJourneys;
    private final BigDecimal measuredPercentage;
    private final BigDecimal errorBudgetRemainingPercentagePoints;
    private final String status;
    private final boolean alertTriggered;
    private final String alertSeverity;
    private final String alertMessage;

    public SloEvaluationResponse(String name, String journeyType, String completionStage,
                                 BigDecimal objectivePercentage, Instant windowStart,
                                 Instant windowEnd, long totalJourneys, long completedJourneys,
                                 BigDecimal measuredPercentage,
                                 BigDecimal errorBudgetRemainingPercentagePoints,
                                 String status, boolean alertTriggered, String alertSeverity,
                                 String alertMessage) {
        this.name = name;
        this.journeyType = journeyType;
        this.completionStage = completionStage;
        this.objectivePercentage = objectivePercentage;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.totalJourneys = totalJourneys;
        this.completedJourneys = completedJourneys;
        this.measuredPercentage = measuredPercentage;
        this.errorBudgetRemainingPercentagePoints = errorBudgetRemainingPercentagePoints;
        this.status = status;
        this.alertTriggered = alertTriggered;
        this.alertSeverity = alertSeverity;
        this.alertMessage = alertMessage;
    }

    public String getName() { return name; }
    public String getJourneyType() { return journeyType; }
    public String getCompletionStage() { return completionStage; }
    public BigDecimal getObjectivePercentage() { return objectivePercentage; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public long getTotalJourneys() { return totalJourneys; }
    public long getCompletedJourneys() { return completedJourneys; }
    public BigDecimal getMeasuredPercentage() { return measuredPercentage; }
    public BigDecimal getErrorBudgetRemainingPercentagePoints() {
        return errorBudgetRemainingPercentagePoints;
    }
    public String getStatus() { return status; }
    public boolean isAlertTriggered() { return alertTriggered; }
    public String getAlertSeverity() { return alertSeverity; }
    public String getAlertMessage() { return alertMessage; }
}
