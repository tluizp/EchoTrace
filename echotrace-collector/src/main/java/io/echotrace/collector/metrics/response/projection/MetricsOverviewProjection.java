package io.echotrace.collector.metrics.response.projection;

public interface MetricsOverviewProjection {

    Long getTotalEvents();

    Double getSuccessRate();

    Long getErrorCount();
}
