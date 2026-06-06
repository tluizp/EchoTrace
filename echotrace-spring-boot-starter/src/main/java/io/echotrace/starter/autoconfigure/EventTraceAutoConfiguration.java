package io.echotrace.starter.autoconfigure;

import io.echotrace.starter.config.TelemetryProperties;
import io.echotrace.starter.publisher.AsyncHttpEventPublisher;
import io.echotrace.starter.publisher.LogEventPublisher;
import io.echotrace.core.EventPublisher;
import io.echotrace.starter.interceptor.BusinessEventInterceptor;
import org.springframework.beans.factory.annotation.Value;
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
            prefix = "echotrace",
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
    public BusinessEventInterceptor businessEventInterceptor(
            EventPublisher publisher,
            @Value("${spring.application.name:unknown-service}")
            String serviceName,
            @Value("${spring.profiles.active:default}")
            String environment
    ) {
        return new BusinessEventInterceptor(
                publisher,
                serviceName,
                environment
        );
    }

    @Bean
    public CommandLineRunner debug(EventPublisher publisher) {
        return args -> {
            System.out.println("Publisher ativo: " + publisher.getClass());
        };
    }
}
