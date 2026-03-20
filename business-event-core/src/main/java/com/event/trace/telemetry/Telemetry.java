package com.event.trace.telemetry;

import java.util.HashMap;
import java.util.Map;

public class Telemetry {

    private static final ThreadLocal<Map<String, Object>> context =
            ThreadLocal.withInitial(HashMap::new);

    public static void capture(String key, Object value) {
        context.get().put(key, value);
    }

    public static String getTraceId() {
        return (String) context.get().get("traceId");
    }

    public static void setTraceId(String traceId) {
        context.get().put("traceId", traceId);
    }

    public static String getSpanId() {
        return (String) context.get().get("spanId");
    }

    public static void setSpanId(String spanId) {
        context.get().put("spanId", spanId);
    }

    public static Map<String, Object> read() {
        Map<String, Object> copy = new HashMap<>(context.get());
        copy.remove("traceId");
        copy.remove("spanId");
        return copy;
    }

    public static void clear() {
        context.get().clear();
    }
}
