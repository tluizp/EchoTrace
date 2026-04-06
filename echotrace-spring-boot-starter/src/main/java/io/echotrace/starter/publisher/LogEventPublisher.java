package io.echotrace.starter.publisher;

import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;

public class LogEventPublisher implements EventPublisher {

    @Override
    public void publish(EventPayload payload) {
        System.out.println("EVENT: " + payload);
    }
}
