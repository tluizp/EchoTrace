package io.echotrace.collector.deployment.response;

import java.math.BigDecimal;
import java.util.Map;

public final class DeploymentEventMetrics {

    private final long totalEvents;
    private final long failedEvents;
    private final BigDecimal failureRate;
    private final Map<String, Long> failureReasons;

    public DeploymentEventMetrics(long totalEvents, long failedEvents, BigDecimal failureRate,
                                  Map<String, Long> failureReasons) {
        this.totalEvents = totalEvents;
        this.failedEvents = failedEvents;
        this.failureRate = failureRate;
        this.failureReasons = Map.copyOf(failureReasons);
    }

    public long getTotalEvents() { return totalEvents; }
    public long getFailedEvents() { return failedEvents; }
    public BigDecimal getFailureRate() { return failureRate; }
    public Map<String, Long> getFailureReasons() { return failureReasons; }
}
