package io.echotrace.starter.publisher;

import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LogEventPublisher.class);

    @Override
    public void publish(EventPayload payload) {
        log.info("[EchoTrace] {}", payload);
    }
}
