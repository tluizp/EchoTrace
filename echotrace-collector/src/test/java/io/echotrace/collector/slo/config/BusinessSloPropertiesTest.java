package io.echotrace.collector.slo.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessSloPropertiesTest {

    @Test
    void acceptsValidDefinition() {
        BusinessSloProperties properties = properties(definition("payment-approval"));

        assertDoesNotThrow(properties::afterPropertiesSet);
    }

    @Test
    void rejectsDuplicateNames() {
        BusinessSloProperties properties = properties(
                definition("payment-approval"), definition("payment-approval"));

        assertThrows(IllegalArgumentException.class, properties::afterPropertiesSet);
    }

    @Test
    void rejectsInvalidObjective() {
        BusinessSloProperties.Definition definition = definition("payment-approval");
        definition.setObjectivePercentage(new BigDecimal("101"));

        assertThrows(IllegalArgumentException.class,
                properties(definition)::afterPropertiesSet);
    }

    private BusinessSloProperties properties(BusinessSloProperties.Definition... definitions) {
        BusinessSloProperties properties = new BusinessSloProperties();
        properties.setBusinessSlos(List.of(definitions));
        return properties;
    }

    private BusinessSloProperties.Definition definition(String name) {
        BusinessSloProperties.Definition definition = new BusinessSloProperties.Definition();
        definition.setName(name);
        definition.setJourneyType("order.checkout");
        definition.setCompletionStage("confirmed");
        definition.setObjectivePercentage(new BigDecimal("93"));
        definition.setWindowMinutes(60);
        definition.setMinimumJourneys(20);
        return definition;
    }
}
