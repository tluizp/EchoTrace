package io.echotrace.collector.metrics.response.enums;

public enum MetricInterval {
    SECOND("second"),
    MINUTE("minute"),
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String postgresValue;

    MetricInterval(String postgresValue) {
        this.postgresValue = postgresValue;
    }

    public String getPostgresValue() {
        return postgresValue;
    }
}
