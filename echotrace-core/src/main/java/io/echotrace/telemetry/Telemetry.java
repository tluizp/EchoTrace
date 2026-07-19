package io.echotrace.telemetry;

import io.echotrace.model.BusinessOutcome;

import java.math.BigDecimal;
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
            child.outcomeName = parent.outcomeName;
            child.journeyId = parent.journeyId;
            child.journeyType = parent.journeyType;
            child.stage = parent.stage;
            child.reason = parent.reason;
            child.value = parent.value;
            child.currency = parent.currency;
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

    public static void outcome(String name) {
        current().outcomeName = normalize(name, "Outcome name");
    }

    public static void journey(String type, String id) {
        current().journeyType = normalize(type, "Journey type");
        current().journeyId = normalize(id, "Journey id");
    }

    public static void stage(String stage) {
        current().stage = normalize(stage, "Journey stage");
    }

    public static void reason(String reason) {
        current().reason = normalize(reason, "Outcome reason");
    }

    public static void value(BigDecimal value, String currency) {
        if (value == null) {
            throw new IllegalArgumentException("Business value must not be null");
        }
        current().value = value;
        current().currency = normalize(currency, "Currency");
    }

    public static BusinessOutcome readBusinessOutcome() {
        Context context = existing();
        if (context == null) {
            return null;
        }
        BusinessOutcome outcome = new BusinessOutcome(
                context.outcomeName, context.journeyId, context.journeyType,
                context.stage, context.reason, context.value, context.currency);
        return outcome.isEmpty() ? null : outcome;
    }

    public static void clear() {
        contexts.remove();
    }

    private static final class Context {
        private final Map<String, Object> attributes = new HashMap<>();
        private String traceId;
        private String spanId;
        private String outcomeName;
        private String journeyId;
        private String journeyType;
        private String stage;
        private String reason;
        private BigDecimal value;
        private String currency;
    }

    private static String normalize(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
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
