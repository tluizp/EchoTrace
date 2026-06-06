package io.echotrace.collector.metrics.response;

import java.util.Objects;

public final class MetricsOverviewResponse {
    private final long totalEvents;
    private final double successRate;
    private final long errorCount;

    public MetricsOverviewResponse(long totalEvents, double successRate, long errorCount) {
        this.totalEvents = totalEvents;
        this.successRate = successRate;
        this.errorCount = errorCount;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public long getErrorCount() {
        return errorCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MetricsOverviewResponse) obj;
        return this.totalEvents == that.totalEvents &&
                Double.doubleToLongBits(this.successRate) == Double.doubleToLongBits(that.successRate) &&
                this.errorCount == that.errorCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalEvents, successRate, errorCount);
    }

    @Override
    public String toString() {
        return "MetricsOverviewResponse[" +
                "totalEvents=" + totalEvents + ", " +
                "successRate=" + successRate + ", " +
                "errorCount=" + errorCount + ']';
    }
}