package com.event.trace.starter.autoconfigure;

import com.event.trace.core.EventPublisher;
import com.event.trace.starter.config.TelemetryProperties;
import com.event.trace.starter.interceptor.BusinessEventInterceptor;
import com.event.trace.starter.publisher.AsyncHttpEventPublisher;
import com.event.trace.starter.publisher.LogEventPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TelemetryProperties.class)
public class EventTraceAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "event.trace",
            name = "collector-url"
    )
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher httpPublisher(TelemetryProperties properties) {
        System.out.println("HTTP PUBLISHER ATIVADO");
        return new AsyncHttpEventPublisher(properties);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher logPublisher() {
        return new LogEventPublisher();
    }

    @Bean
    @ConditionalOnBean(EventPublisher.class)
    public BusinessEventInterceptor businessEventInterceptor(EventPublisher publisher) {
        return new BusinessEventInterceptor(publisher);
    }

    @Bean
    public CommandLineRunner debug(EventPublisher publisher) {
        return args -> {
            System.out.println("Publisher ativo: " + publisher.getClass());
        };
    }
}
