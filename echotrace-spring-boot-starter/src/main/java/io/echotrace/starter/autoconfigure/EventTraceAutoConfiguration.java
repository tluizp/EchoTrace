package io.echotrace.starter.autoconfigure;

import io.echotrace.starter.config.TelemetryProperties;
import io.echotrace.starter.publisher.AsyncHttpEventPublisher;
import io.echotrace.starter.publisher.LogEventPublisher;
import io.echotrace.core.EventPublisher;
import io.echotrace.starter.interceptor.EchoTraceInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TelemetryProperties.class)
public class EventTraceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EventTraceAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(
            prefix = "echotrace",
            name = "collector-url"
    )
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher httpPublisher(TelemetryProperties properties) {
        log.debug("[EchoTrace] HTTP publisher enabled (echotrace.collector-url configured)");
        return new AsyncHttpEventPublisher(properties);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher logPublisher() {
        log.debug("[EchoTrace] Log publisher enabled (default)");
        return new LogEventPublisher();
    }

    @Bean
    @ConditionalOnBean(EventPublisher.class)
    public EchoTraceInterceptor echoEventInterceptor(EventPublisher publisher) {
        return new EchoTraceInterceptor(publisher);
    }
}
