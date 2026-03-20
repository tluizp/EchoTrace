package com.event.trace.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event.trace")
public class TelemetryProperties {

    private String collectorUrl;

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public void setCollectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }
}
