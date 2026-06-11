package io.echotrace.collector.metrics.response.projection;

import java.time.Instant;

public interface EventCountProjection {

    Instant getBucketTime();

    String getBucketLabel();

    Long getTotal();
}
