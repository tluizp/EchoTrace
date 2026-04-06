package io.echotrace.starter.publisher;

import io.echotrace.starter.config.TelemetryProperties;
import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AsyncHttpEventPublisher implements EventPublisher {

    private final String collectorUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public AsyncHttpEventPublisher(TelemetryProperties props) {
        if (props.getCollectorUrl() == null) {
            throw new IllegalStateException("echotrace.collector-url must be configured");
        }
        this.collectorUrl = props.getCollectorUrl();
        this.client = HttpClient.newHttpClient();
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
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            // log estruturado aqui
        }
    }
}