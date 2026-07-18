package io.echotrace.starter.publisher;

import io.echotrace.starter.config.TelemetryProperties;
import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncHttpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AsyncHttpEventPublisher.class);

    private final String collectorUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Duration requestTimeout;
    private final int maxRetries;

    public AsyncHttpEventPublisher(TelemetryProperties props) {
        if (props.getCollectorUrl() == null) {
            throw new IllegalStateException("echotrace.collector-url must be configured");
        }
        validatePositive(props.getConnectTimeoutMs(), "connect-timeout-ms");
        validatePositive(props.getRequestTimeoutMs(), "request-timeout-ms");
        if (props.getMaxRetries() < 0) {
            throw new IllegalArgumentException("echotrace.max-retries must not be negative");
        }
        this.collectorUrl = props.getCollectorUrl().replaceAll("/+$", "");
        this.requestTimeout = Duration.ofMillis(props.getRequestTimeoutMs());
        this.maxRetries = props.getMaxRetries();
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
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
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            send(request, payload.getEventId(), 0);

        } catch (Exception e) {
            log.warn("EchoTrace could not serialize event {}", payload.getEventId(), e);
        }
    }

    private void send(HttpRequest request, String eventId, int attempt) {
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, error) -> {
                    if (error == null && response.statusCode() >= 200 && response.statusCode() < 300) {
                        return;
                    }
                    boolean retryableStatus = error == null
                            && (response.statusCode() == 429 || response.statusCode() >= 500);
                    if (attempt < maxRetries && (error != null || retryableStatus)) {
                        long delayMs = 100L * (1L << attempt);
                        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                                .execute(() -> send(request, eventId, attempt + 1));
                        return;
                    }
                    if (error != null) {
                        log.warn("EchoTrace delivery failed for event {} after {} attempts",
                                eventId, attempt + 1, error);
                    } else {
                        log.warn("EchoTrace collector returned HTTP {} for event {} after {} attempts",
                                response.statusCode(), eventId, attempt + 1);
                    }
                });
    }

    private static void validatePositive(int value, String property) {
        if (value < 1) {
            throw new IllegalArgumentException("echotrace." + property + " must be greater than zero");
        }
    }
}
