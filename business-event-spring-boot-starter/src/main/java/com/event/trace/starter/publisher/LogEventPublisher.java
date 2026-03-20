package com.event.trace.starter.publisher;

import com.event.trace.core.EventPublisher;
import com.event.trace.model.EventPayload;

public class LogEventPublisher implements EventPublisher {

    @Override
    public void publish(EventPayload payload) {
        System.out.println("EVENT: " + payload);
    }
}
