package io.echotrace.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Applies safe defaults before attributes leave the instrumented application. */
public final class AttributeSanitizer {

    private static final Set<String> SENSITIVE_TERMS = Set.of(
            "password", "passwd", "secret", "token", "authorization",
            "cookie", "creditcard", "cardnumber", "cvv"
    );
    private static final String REDACTED = "[REDACTED]";

    private final int maxStringLength;
    private final int maxCollectionSize;
    private final int maxDepth;

    public AttributeSanitizer() {
        this(2048, 100, 8);
    }

    public AttributeSanitizer(int maxStringLength, int maxCollectionSize, int maxDepth) {
        if (maxStringLength < 1 || maxCollectionSize < 1 || maxDepth < 1) {
            throw new IllegalArgumentException("Sanitizer limits must be greater than zero");
        }
        this.maxStringLength = maxStringLength;
        this.maxCollectionSize = maxCollectionSize;
        this.maxDepth = maxDepth;
    }

    public Map<String, Object> sanitize(Map<String, Object> attributes) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (attributes == null) {
            return result;
        }
        attributes.forEach((key, value) -> result.put(key,
                isSensitive(key) ? REDACTED : sanitizeValue(value, 0)));
        return result;
    }

    private Object sanitizeValue(Object value, int depth) {
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof Enum) {
            return value;
        }
        if (depth >= maxDepth) {
            return "[MAX_DEPTH]";
        }
        if (value instanceof CharSequence || value instanceof Character) {
            String text = String.valueOf(value);
            return text.length() <= maxStringLength ? text : text.substring(0, maxStringLength) + "[TRUNCATED]";
        }
        if (value instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            int count = 0;
            for (Object item : ((Map<?, ?>) value).entrySet()) {
                if (count++ >= maxCollectionSize) break;
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
                String key = String.valueOf(entry.getKey());
                result.put(key, isSensitive(key) ? REDACTED : sanitizeValue(entry.getValue(), depth + 1));
            }
            return result;
        }
        if (value instanceof Collection) {
            List<Object> result = new ArrayList<>();
            int count = 0;
            for (Object item : (Collection<?>) value) {
                if (count++ >= maxCollectionSize) break;
                result.add(sanitizeValue(item, depth + 1));
            }
            return result;
        }
        if (value.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            int length = Math.min(Array.getLength(value), maxCollectionSize);
            for (int i = 0; i < length; i++) {
                result.add(sanitizeValue(Array.get(value, i), depth + 1));
            }
            return result;
        }
        return sanitizeValue(String.valueOf(value), depth + 1);
    }

    private boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return SENSITIVE_TERMS.stream().anyMatch(normalized::contains);
    }
}
