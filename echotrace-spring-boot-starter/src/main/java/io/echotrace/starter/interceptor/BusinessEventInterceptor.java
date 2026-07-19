package io.echotrace.starter.interceptor;

import io.echotrace.annotation.EchoTrace;
import io.echotrace.core.EventPublisher;
import io.echotrace.core.AttributeSanitizer;
import io.echotrace.model.EventPayload;
import io.echotrace.model.BusinessOutcome;
import io.echotrace.telemetry.Telemetry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
public class BusinessEventInterceptor {

    private static final Logger log = LoggerFactory.getLogger(BusinessEventInterceptor.class);

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final EventPublisher publisher;

    private final String serviceName;

    private final String environment;
    private final String serviceVersion;
    private final String deploymentId;
    private final String commitSha;
    private final AttributeSanitizer sanitizer = new AttributeSanitizer();

    public BusinessEventInterceptor(EventPublisher publisher,
                                    @Value("${spring.application.name:unknown-service}")
                                    String serviceName,
                                    @Value("${spring.profiles.active:default}")
                                    String environment) {
        this(publisher, serviceName, environment, null, null, null);
    }

    public BusinessEventInterceptor(EventPublisher publisher, String serviceName, String environment,
                                    String serviceVersion, String deploymentId, String commitSha) {
        this.publisher = publisher;
        this.serviceName = serviceName;
        this.environment = environment;
        this.serviceVersion = serviceVersion;
        this.deploymentId = deploymentId;
        this.commitSha = commitSha;
    }

    @Around("@annotation(event)")
    public Object intercept(ProceedingJoinPoint pjp, EchoTrace event) throws Throwable {

        Telemetry.Scope scope = Telemetry.startScope();
        String traceId = Telemetry.getTraceId();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            Telemetry.setTraceId(traceId);
        }
        String spanId = UUID.randomUUID().toString();
        Telemetry.setSpanId(spanId);
        Instant createdAt = Instant.now();
        long start = System.nanoTime();
        Object result = null;
        Throwable error = null;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            try {

                long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

                // (A) Captura automática via anotação
                Map<String, Object> autoCaptured =
                        extractCapturedValues(pjp, event.capture());

                // (B) Captura manual via telemetry.capture(...)
                Map<String, Object> manualCaptured = Telemetry.read();

                Map<String, Object> payloadData = new HashMap<>(autoCaptured);
                manualCaptured.forEach((k, v) ->
                        payloadData.merge(k, v, (autoVal, manualVal) -> manualVal)
                );

                payloadData.put("method", pjp.getSignature().toShortString());
                payloadData.put("class", pjp.getTarget().getClass().getSimpleName());

                // captura retorno
                if (event.captureReturn() && error == null) {
                    payloadData.put("methodReturn", safeSerialize(result));
                }

                // captura erro
                if (error != null) {
                    payloadData.put("errorType", error.getClass().getSimpleName());
                    payloadData.put("errorMessage", error.getMessage());
                    payloadData.put("errorStack",
                            Arrays.stream(error.getStackTrace())
                                    .limit(10)
                                    .map(StackTraceElement::toString)
                                    .collect(Collectors.toList())
                    );
                }

                String status = error == null
                        ? "SUCCESS"
                        : "ERROR";

                BusinessOutcome annotatedOutcome = outcomeFromAnnotation(pjp, event);
                BusinessOutcome manualOutcome = Telemetry.readBusinessOutcome();
                BusinessOutcome businessOutcome = mergeOutcomes(annotatedOutcome, manualOutcome);

                EventPayload payload = new EventPayload(
                        EventPayload.CURRENT_SPEC_VERSION,
                        UUID.randomUUID().toString(),
                        1,
                        event.name(),
                        serviceName,
                        environment,
                        status,
                        duration,
                        traceId,
                        spanId,
                        createdAt,
                        Instant.now(),
                        sanitizer.sanitize(payloadData),
                        businessOutcome,
                        serviceVersion,
                        deploymentId,
                        commitSha
                );

                publisher.publish(payload);

            } catch (Exception telemetryError) {
                log.error("[BusinessEvent] Telemetry failure", telemetryError);
            } finally {
                scope.close();
            }
        }
    }

    private BusinessOutcome outcomeFromAnnotation(ProceedingJoinPoint pjp, EchoTrace event) {
        Object correlation = resolveArgumentPath(pjp, event.correlationId());
        Object rawValue = resolveArgumentPath(pjp, event.value());
        BigDecimal value = toBigDecimal(rawValue, event.value());
        BusinessOutcome outcome = new BusinessOutcome(
                event.outcome(), stringValue(correlation), event.journey(), event.stage(),
                null, value, value == null ? null : event.currency());
        return outcome.isEmpty() ? null : outcome;
    }

    private BusinessOutcome mergeOutcomes(BusinessOutcome base, BusinessOutcome override) {
        if (base == null) return override;
        if (override == null) return base;
        return new BusinessOutcome(
                first(override.getName(), base.getName()),
                first(override.getJourneyId(), base.getJourneyId()),
                first(override.getJourneyType(), base.getJourneyType()),
                first(override.getStage(), base.getStage()),
                first(override.getReason(), base.getReason()),
                override.getValue() != null ? override.getValue() : base.getValue(),
                first(override.getCurrency(), base.getCurrency())
        );
    }

    private Object resolveArgumentPath(ProceedingJoinPoint pjp, String path) {
        if (path == null || path.trim().isEmpty()) return null;
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Object[] values = pjp.getArgs();
        for (int i = 0; i < names.length; i++) {
            if (path.equals(names[i])) return values[i];
            if (path.startsWith(names[i] + ".")) {
                try {
                    return resolveFieldPath(values[i], path.substring(names[i].length() + 1));
                } catch (Exception exception) {
                    log.warn("[BusinessEvent] Failed to resolve business path: {}", path, exception);
                    return null;
                }
            }
        }
        log.warn("[BusinessEvent] Business path does not match a method argument: {}", path);
        return null;
    }

    private BigDecimal toBigDecimal(Object value, String path) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return new BigDecimal(value.toString());
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException exception) {
            log.warn("[BusinessEvent] Business value at {} is not numeric", path);
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String first(String preferred, String fallback) {
        return preferred != null ? preferred : fallback;
    }

    private Map<String, Object> extractCapturedValues(
            ProceedingJoinPoint pjp,
            String[] capturePaths
    ) {
        if (capturePaths.length == 0) return Collections.emptyMap();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String[] argNames = sig.getParameterNames();
        Object[] argValues = pjp.getArgs();

        Map<String, Object> argMap = IntStream.range(0, argNames.length)
                .boxed()
                .collect(Collectors.toMap(i -> argNames[i], i -> argValues[i]));

        Map<String, Object> result = new HashMap<>();

        for (String path : capturePaths) {
            try {
                int dotIndex = path.indexOf('.');

                if (dotIndex == -1) {
                    result.put(path, argMap.get(path));
                    continue;
                }

                String rootName = path.substring(0, dotIndex);
                String fieldPath = path.substring(dotIndex + 1);

                Object rootObj = argMap.get(rootName);
                if (rootObj == null) continue;

                Object value = resolveFieldPath(rootObj, fieldPath);
                result.put(path, value);

            } catch (Exception exception) {
                log.error("[BusinessEvent] Failed to capture path: {}", path, exception);
            }
        }

        return result;
    }

    private Object resolveFieldPath(Object root, String path) throws Exception {
        Object current = root;

        for (String p : path.split("\\.")) {
            if (current == null) return null;

            Field f = getField(current.getClass(), p);
            current = f.get(current);
        }

        return current;
    }

    private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Map<String, Field> fields = FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Map<String, Field> map = new HashMap<>();
            Class<?> current = c;

            while (current != null) {
                for (Field f : current.getDeclaredFields()) {
                    f.setAccessible(true);
                    map.putIfAbsent(f.getName(), f);
                }
                current = current.getSuperclass();
            }
            return map;
        });

        Field field = fields.get(name);
        if (field == null) throw new NoSuchFieldException(name);

        return field;
    }

    private Object safeSerialize(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (Exception e) {
            log.error("[BusinessEvent] Serialization error: ", e);
            return "serialization_error";
        }
    }
}
