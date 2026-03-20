package com.event.trace.core;

import com.event.trace.model.EventPayload;

public interface EventPublisher {
    void publish(EventPayload payload);
}
