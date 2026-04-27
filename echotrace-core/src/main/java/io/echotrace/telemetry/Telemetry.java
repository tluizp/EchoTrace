package io.echotrace.telemetry;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Telemetry {

    private static final ThreadLocal<String> traceId = new ThreadLocal<>();
    private static final ThreadLocal<ArrayDeque<String>> spanStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<Map<String, Object>>> captureStack =
            ThreadLocal.withInitial(() -> {
                ArrayDeque<Map<String, Object>> stack = new ArrayDeque<>();
                stack.push(new HashMap<>());
                return stack;
            });

    public static void capture(String key, Object value) {
        Map<String, Object> top = captureStack.get().peek();
        if (top == null) {
            top = new HashMap<>();
            captureStack.get().push(top);
        }
        top.put(key, value);
    }

    public static String getTraceId() {
        return traceId.get();
    }

    public static void setTraceId(String traceId) {
        Telemetry.traceId.set(traceId);
    }

    public static String getSpanId() {
        ArrayDeque<String> stack = spanStack.get();
        return stack.peek();
    }

    public static void setSpanId(String spanId) {
        ArrayDeque<String> stack = spanStack.get();
        if (stack.isEmpty()) {
            stack.push(spanId);
        } else {
            stack.pop();
            stack.push(spanId);
        }
    }

    public static Map<String, Object> read() {
        ArrayDeque<Map<String, Object>> stack = captureStack.get();
        Map<String, Object> top = stack.peek();
        return top == null ? Map.of() : new HashMap<>(top);
    }

    public static void clear() {
        traceId.remove();
        spanStack.get().clear();
        captureStack.get().clear();
        captureStack.get().push(new HashMap<>());
    }

    /**
     * Inicia um escopo de captura isolado (para evitar vazamento de Telemetry.capture entre chamadas).
     * O chamador deve fechar o escopo no finally.
     */
    public static Scope beginScope() {
        captureStack.get().push(new HashMap<>());
        return new Scope();
    }

    /**
     * Garante um traceId existente ou cria um novo se ainda nao houver.
     */
    public static String ensureTraceId() {
        String current = traceId.get();
        if (current != null && !current.isBlank()) return current;
        String generated = UUID.randomUUID().toString();
        traceId.set(generated);
        return generated;
    }

    /**
     * Inicia um novo span e retorna o spanId.
     */
    public static String startSpan() {
        String spanId = UUID.randomUUID().toString();
        spanStack.get().push(spanId);
        return spanId;
    }

    /**
     * Encerra o span atual e restaura o anterior (se houver).
     */
    public static void endSpan() {
        ArrayDeque<String> stack = spanStack.get();
        if (!stack.isEmpty()) stack.pop();
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed = false;

        private Scope() {}

        @Override
        public void close() {
            if (closed) return;
            closed = true;

            ArrayDeque<Map<String, Object>> stack = captureStack.get();
            if (stack.size() > 1) {
                stack.pop();
            } else {
                Map<String, Object> top = stack.peek();
                if (top != null) top.clear();
            }
        }
    }
}
