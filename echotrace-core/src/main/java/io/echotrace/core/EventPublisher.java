package io.echotrace.core;

import io.echotrace.model.EventPayload;

public interface EventPublisher {
    void publish(EventPayload payload);
}
