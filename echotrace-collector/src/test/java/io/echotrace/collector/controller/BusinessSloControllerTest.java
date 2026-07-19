package io.echotrace.collector.controller;

import io.echotrace.collector.repository.EventRepository;
import io.echotrace.collector.slo.config.BusinessSloProperties;
import io.echotrace.collector.slo.response.SloEvaluationResponse;
import io.echotrace.collector.slo.service.BusinessSloService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessSloControllerTest {

    @Test
    void returnsEmptyEvaluationListWhenNoSlosAreConfigured() {
        EventRepository repository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(), new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("toString")) return "EventRepositoryStub";
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    return null;
                });
        BusinessSloController controller = new BusinessSloController(
                new BusinessSloService(repository, new BusinessSloProperties()));

        List<SloEvaluationResponse> response = controller.evaluate(
                Instant.parse("2026-07-19T12:00:00Z"));

        assertEquals(List.of(), response);
    }
}
