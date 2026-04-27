package io.echotrace.starter.interceptor;

import io.echotrace.annotation.EchoTrace;
import io.echotrace.core.EventPublisher;
import io.echotrace.model.EventPayload;
import io.echotrace.telemetry.Telemetry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
public class EchoTraceInterceptor {

    private static final Logger log = LoggerFactory.getLogger(EchoTraceInterceptor.class);

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final EventPublisher publisher;

    public EchoTraceInterceptor(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Around("@annotation(event)")
    public Object intercept(ProceedingJoinPoint pjp, EchoTrace event) throws Throwable {

        Telemetry.Scope scope = Telemetry.beginScope();
        long start = System.nanoTime();
        Object result = null;
        Throwable error = null;

        // garante IDs antes de executar o metodo para suportar capturas manuais dentro dele
        String traceId = Telemetry.ensureTraceId();
        String spanId = Telemetry.startSpan();

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

                Map<String, Object> metadata = new HashMap<>(autoCaptured);
                manualCaptured.forEach((k, v) ->
                        metadata.merge(k, v, (autoVal, manualVal) -> manualVal)
                );

                metadata.put("method", pjp.getSignature().toShortString());
                metadata.put("class", pjp.getTarget().getClass().getSimpleName());

                // captura retorno
                if (event.captureReturn() && error == null) {
                    metadata.put("methodReturn", safeSerialize(result));
                }

                // captura erro
                if (error != null) {
                    metadata.put("errorType", error.getClass().getSimpleName());
                    metadata.put("errorMessage", error.getMessage());
                    metadata.put("errorStack",
                            Arrays.stream(error.getStackTrace())
                                    .limit(10)
                                    .map(StackTraceElement::toString)
                                    .collect(Collectors.toList())
                    );
                }

                //TODO ->>> "Feature futura para ambiente"
                EventPayload payload = new EventPayload(
                        event.name(),
                        "local",
                        duration,
                        traceId,
                        spanId,
                        metadata
                );

                publisher.publish(payload);

            } catch (Exception telemetryError) {
                log.error("[EchoTrace] Telemetry failure", telemetryError);
            } finally {
                Telemetry.endSpan();
                scope.close();
            }
        }
    }

    private Map<String, Object> extractCapturedValues(
            ProceedingJoinPoint pjp,
            String[] capturePaths
    ) {
        if (capturePaths.length == 0) return Collections.emptyMap();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String[] argNames = sig.getParameterNames();
        Object[] argValues = pjp.getArgs();

        if (argNames == null || argNames.length != argValues.length) {
            argNames = IntStream.range(0, argValues.length)
                    .mapToObj(i -> "arg" + i)
                    .toArray(String[]::new);
        }

        final String[] effectiveArgNames = argNames;
        Map<String, Object> argMap = IntStream.range(0, effectiveArgNames.length)
                .boxed()
                .collect(Collectors.toMap(i -> effectiveArgNames[i], i -> argValues[i]));

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
                if (rootObj == null) {
                    Integer index = parseArgIndex(rootName);
                    if (index != null && index >= 0 && index < argValues.length) {
                        rootObj = argValues[index];
                    }
                }
                if (rootObj == null && argValues.length == 1) {
                    // fallback pragmatico: metodos com 1 argumento sao comuns e o nome pode nao estar disponivel
                    rootObj = argValues[0];
                }
                if (rootObj == null) continue;

                Object value = resolveFieldPath(rootObj, fieldPath);
                result.put(path, value);

            } catch (Exception exception) {
                log.error("[EchoTrace] Failed to capture path: {}", path, exception);
            }
        }

        return result;
    }

    private Integer parseArgIndex(String rootName) {
        try {
            if (rootName == null || rootName.isBlank()) return null;
            if (rootName.matches("^\\d+$")) return Integer.parseInt(rootName);
            if (rootName.startsWith("arg") && rootName.length() > 3) return Integer.parseInt(rootName.substring(3));
            if (rootName.startsWith("p") && rootName.length() > 1) return Integer.parseInt(rootName.substring(1));
            return null;
        } catch (Exception ignored) {
            return null;
        }
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
            log.error("[EchoTrace] Serialization error: ", e);
            return "serialization_error";
        }
    }
}
