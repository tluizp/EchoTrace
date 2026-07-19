package io.echotrace.collector.deployment.response;

import java.math.BigDecimal;
import java.time.Instant;

public final class DeploymentImpactResponse {

    private final String journeyType;
    private final String completionStage;
    private final String serviceName;
    private final String deploymentId;
    private final String serviceVersion;
    private final Instant cutoverAt;
    private final String cutoverSource;
    private final JourneyPeriodMetrics before;
    private final JourneyPeriodMetrics after;
    private final DeploymentEventMetrics deploymentEvents;
    private final BigDecimal conversionDeltaPercentagePoints;
    private final BigDecimal failureDeltaPercentagePoints;
    private final String signal;
    private final int minimumSampleSize;
    private final boolean sampleSufficient;
    private final String correlationNotice;

    public DeploymentImpactResponse(String journeyType, String completionStage,
                                    String serviceName, String deploymentId,
                                    String serviceVersion, Instant cutoverAt,
                                    JourneyPeriodMetrics before, JourneyPeriodMetrics after,
                                    DeploymentEventMetrics deploymentEvents,
                                    BigDecimal conversionDeltaPercentagePoints,
                                    BigDecimal failureDeltaPercentagePoints, String signal,
                                    int minimumSampleSize, boolean sampleSufficient) {
        this.journeyType = journeyType;
        this.completionStage = completionStage;
        this.serviceName = serviceName;
        this.deploymentId = deploymentId;
        this.serviceVersion = serviceVersion;
        this.cutoverAt = cutoverAt;
        this.cutoverSource = "FIRST_OBSERVED_DEPLOYMENT_EVENT";
        this.before = before;
        this.after = after;
        this.deploymentEvents = deploymentEvents;
        this.conversionDeltaPercentagePoints = conversionDeltaPercentagePoints;
        this.failureDeltaPercentagePoints = failureDeltaPercentagePoints;
        this.signal = signal;
        this.minimumSampleSize = minimumSampleSize;
        this.sampleSufficient = sampleSufficient;
        this.correlationNotice = "Temporal correlation does not prove that the deployment caused the change";
    }

    public String getJourneyType() { return journeyType; }
    public String getCompletionStage() { return completionStage; }
    public String getServiceName() { return serviceName; }
    public String getDeploymentId() { return deploymentId; }
    public String getServiceVersion() { return serviceVersion; }
    public Instant getCutoverAt() { return cutoverAt; }
    public String getCutoverSource() { return cutoverSource; }
    public JourneyPeriodMetrics getBefore() { return before; }
    public JourneyPeriodMetrics getAfter() { return after; }
    public DeploymentEventMetrics getDeploymentEvents() { return deploymentEvents; }
    public BigDecimal getConversionDeltaPercentagePoints() { return conversionDeltaPercentagePoints; }
    public BigDecimal getFailureDeltaPercentagePoints() { return failureDeltaPercentagePoints; }
    public String getSignal() { return signal; }
    public int getMinimumSampleSize() { return minimumSampleSize; }
    public boolean isSampleSufficient() { return sampleSufficient; }
    public String getCorrelationNotice() { return correlationNotice; }
}
