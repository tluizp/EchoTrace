package io.echotrace.starter.publisher;

import io.echotrace.starter.config.TelemetryProperties;
import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AsyncHttpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AsyncHttpEventPublisher.class);

    private final String collectorUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Duration timeout;

    public AsyncHttpEventPublisher(TelemetryProperties props) {
        if (props.getCollectorUrl() == null || props.getCollectorUrl().isBlank()) {
            throw new IllegalStateException("echotrace.collector-url must be configured");
        }
        this.collectorUrl = props.getCollectorUrl();
        this.timeout = props.getHttpTimeout() == null ? Duration.ofSeconds(2) : props.getHttpTimeout();
        this.client = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void publish(EventPayload payload) {
        try {
            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(collectorUrl + "/api/events"))
                    .header("Content-Type", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .exceptionally(ex -> {
                        log.warn("[EchoTrace] Failed to publish event via HTTP", ex);
                        return null;
                    });

        } catch (Exception e) {
            log.warn("[EchoTrace] Failed to serialize/publish event", e);
        }
    }
}
