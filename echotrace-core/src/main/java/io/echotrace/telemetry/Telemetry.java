package io.echotrace.telemetry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class Telemetry {

    private static final ThreadLocal<Deque<Context>> contexts =
            ThreadLocal.withInitial(ArrayDeque::new);

    private Telemetry() {
    }

    /** Starts an isolated scope while inheriting the current trace correlation. */
    public static Scope startScope() {
        Deque<Context> stack = contexts.get();
        Context parent = stack.peek();
        Context child = new Context();
        if (parent != null) {
            child.traceId = parent.traceId;
            child.spanId = parent.spanId;
        }
        stack.push(child);
        return new Scope();
    }

    private static Context current() {
        Deque<Context> stack = contexts.get();
        if (stack.isEmpty()) {
            stack.push(new Context());
        }
        return stack.peek();
    }

    private static Context existing() {
        return contexts.get().peek();
    }

    public static void capture(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Telemetry key must not be blank");
        }
        current().attributes.put(key, value);
    }

    public static String getTraceId() {
        Context context = existing();
        return context == null ? null : context.traceId;
    }

    public static void setTraceId(String traceId) {
        current().traceId = traceId;
    }

    public static String getSpanId() {
        Context context = existing();
        return context == null ? null : context.spanId;
    }

    public static void setSpanId(String spanId) {
        current().spanId = spanId;
    }

    public static Map<String, Object> read() {
        Context context = existing();
        return context == null ? new HashMap<>() : new HashMap<>(context.attributes);
    }

    public static void clear() {
        contexts.remove();
    }

    private static final class Context {
        private final Map<String, Object> attributes = new HashMap<>();
        private String traceId;
        private String spanId;
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;

        private Scope() {
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            Deque<Context> stack = contexts.get();
            if (!stack.isEmpty()) {
                stack.pop();
            }
            if (stack.isEmpty()) {
                contexts.remove();
            }
            closed = true;
        }
    }
}
