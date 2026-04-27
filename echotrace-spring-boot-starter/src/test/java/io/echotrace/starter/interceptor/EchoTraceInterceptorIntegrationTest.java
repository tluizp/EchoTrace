package io.echotrace.starter.interceptor;

import io.echotrace.annotation.EchoTrace;
import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import io.echotrace.starter.autoconfigure.EventTraceAutoConfiguration;
import io.echotrace.telemetry.Telemetry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EchoTraceInterceptorIntegrationTest {

    @Test
    void interceptsAnnotatedMethodAndPublishesPayload() {
        AtomicReference<EventPayload> published = new AtomicReference<>();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(EventTraceAutoConfiguration.class))
                .withUserConfiguration(TestConfig.class)
                .withBean(EventPublisher.class, () -> published::set)
                .run(context -> {
                    TestService service = context.getBean(TestService.class);
                    String result = service.doWork(new Request("123"));

                    assertThat(result).isEqualTo("ok:123");
                    assertThat(published.get()).isNotNull();
                    assertThat(published.get().getEventName()).isEqualTo("work");

                    // capture via anotacao (suporta "request.id" e fallback por indice "0.id")
                    assertThat(published.get().getMetadata()).containsEntry("request.id", "123");
                    // capture manual dentro do metodo
                    assertThat(published.get().getMetadata()).containsEntry("manualKey", "manualVal");
                    // retorno capturado
                    assertThat(published.get().getMetadata()).containsKey("methodReturn");
                    assertThat(published.get().getTraceId()).isNotBlank();
                    assertThat(published.get().getSpanId()).isNotBlank();
                });
    }

    @Configuration
    static class TestConfig {
        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    static class TestService {
        @EchoTrace(name = "work", capture = {"request.id"}, captureReturn = true)
        public String doWork(Request request) {
            Telemetry.capture("manualKey", "manualVal");
            return "ok:" + request.id;
        }
    }

    static class Request {
        final String id;

        Request(String id) {
            this.id = id;
        }
    }
}

