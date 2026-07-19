package io.echotrace.starter.interceptor;

import io.echotrace.annotation.EchoTrace;
import io.echotrace.model.EventPayload;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessEventInterceptorTest {

    @Test
    void emitsAnnotatedBusinessOutcomeAndDeploymentMetadata() throws Throwable {
        List<EventPayload> published = new ArrayList<>();
        BusinessEventInterceptor interceptor = new BusinessEventInterceptor(
                published::add, "payment-service", "production", "2.14.3", "deploy-7", "abc123");
        Method method = Fixture.class.getDeclaredMethod("approve", PaymentRequest.class);
        MethodSignature signature = mock(MethodSignature.class);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        PaymentRequest request = new PaymentRequest("order-42", new BigDecimal("349.90"));

        when(signature.getParameterNames()).thenReturn(new String[]{"request"});
        when(signature.toShortString()).thenReturn("Fixture.approve(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        when(joinPoint.getTarget()).thenReturn(new Fixture());
        when(joinPoint.proceed()).thenReturn("approved");

        interceptor.intercept(joinPoint, method.getAnnotation(EchoTrace.class));

        EventPayload event = published.get(0);
        assertEquals("payment.approval", event.getBusinessOutcome().getName());
        assertEquals("order-42", event.getBusinessOutcome().getJourneyId());
        assertEquals(new BigDecimal("349.90"), event.getBusinessOutcome().getValue());
        assertEquals("2.14.3", event.getServiceVersion());
        assertEquals("deploy-7", event.getDeploymentId());
    }

    static class Fixture {
        @EchoTrace(name = "payment.processed", outcome = "payment.approval",
                journey = "order.checkout", stage = "payment",
                correlationId = "request.orderId", value = "request.amount", currency = "BRL")
        String approve(PaymentRequest request) {
            return "approved";
        }
    }

    static class PaymentRequest {
        private final String orderId;
        private final BigDecimal amount;

        PaymentRequest(String orderId, BigDecimal amount) {
            this.orderId = orderId;
            this.amount = amount;
        }
    }
}
