package io.echotrace.starter.autoconfigure;

import io.echotrace.core.EventPublisher;
import io.echotrace.starter.publisher.AsyncHttpEventPublisher;
import io.echotrace.starter.publisher.LogEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EventTraceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EventTraceAutoConfiguration.class));

    @Test
    void defaultsToLogPublisher() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EventPublisher.class);
            assertThat(context.getBean(EventPublisher.class)).isInstanceOf(LogEventPublisher.class);
        });
    }

    @Test
    void usesHttpPublisherWhenCollectorUrlConfigured() {
        contextRunner
                .withPropertyValues("echotrace.collector-url=http://localhost:3001")
                .run(context -> {
                    assertThat(context).hasSingleBean(EventPublisher.class);
                    assertThat(context.getBean(EventPublisher.class)).isInstanceOf(AsyncHttpEventPublisher.class);
                });
    }
}

