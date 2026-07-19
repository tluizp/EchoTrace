package io.echotrace.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeSanitizerTest {

    @Test
    void redactsSensitiveKeysAtEveryLevel() {
        AttributeSanitizer sanitizer = new AttributeSanitizer();

        Map<String, Object> result = sanitizer.sanitize(Map.of(
                "authorization", "Bearer secret",
                "customer", Map.of("password", "123", "name", "Ana")
        ));

        assertEquals("[REDACTED]", result.get("authorization"));
        Map<?, ?> customer = (Map<?, ?>) result.get("customer");
        assertEquals("[REDACTED]", customer.get("password"));
        assertEquals("Ana", customer.get("name"));
    }

    @Test
    void truncatesOversizedStrings() {
        AttributeSanitizer sanitizer = new AttributeSanitizer(4, 10, 3);

        assertEquals("abcd[TRUNCATED]", sanitizer.sanitize(Map.of("value", "abcdefgh")).get("value"));
    }
}
