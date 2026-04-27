package io.echotrace.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "echotrace")
public class TelemetryProperties {

    private String collectorUrl;
    private Duration httpTimeout = Duration.ofSeconds(2);

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public void setCollectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    public Duration getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(Duration httpTimeout) {
        this.httpTimeout = httpTimeout;
    }
}
