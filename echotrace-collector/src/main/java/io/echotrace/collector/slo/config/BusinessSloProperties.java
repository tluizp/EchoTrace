package io.echotrace.collector.slo.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "echotrace")
public class BusinessSloProperties implements InitializingBean {

    private List<Definition> businessSlos = new ArrayList<>();

    public List<Definition> getBusinessSlos() {
        return businessSlos;
    }

    public void setBusinessSlos(List<Definition> businessSlos) {
        this.businessSlos = businessSlos == null ? new ArrayList<>() : businessSlos;
    }

    @Override
    public void afterPropertiesSet() {
        Set<String> names = new HashSet<>();
        for (Definition definition : businessSlos) {
            requireText(definition.name, "business-slos.name");
            requireText(definition.journeyType, "business-slos.journey-type");
            requireText(definition.completionStage, "business-slos.completion-stage");
            if (!names.add(definition.name.trim())) {
                throw new IllegalArgumentException("Business SLO names must be unique: " + definition.name);
            }
            if (definition.objectivePercentage == null
                    || definition.objectivePercentage.compareTo(BigDecimal.ZERO) <= 0
                    || definition.objectivePercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException(
                        "business-slos.objective-percentage must be greater than 0 and at most 100");
            }
            if (definition.windowMinutes < 1) {
                throw new IllegalArgumentException("business-slos.window-minutes must be greater than zero");
            }
            if (definition.minimumJourneys < 1) {
                throw new IllegalArgumentException("business-slos.minimum-journeys must be greater than zero");
            }
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    public static class Definition {
        private String name;
        private String journeyType;
        private String completionStage;
        private BigDecimal objectivePercentage;
        private long windowMinutes = 60;
        private int minimumJourneys = 20;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getJourneyType() { return journeyType; }
        public void setJourneyType(String journeyType) { this.journeyType = journeyType; }
        public String getCompletionStage() { return completionStage; }
        public void setCompletionStage(String completionStage) { this.completionStage = completionStage; }
        public BigDecimal getObjectivePercentage() { return objectivePercentage; }
        public void setObjectivePercentage(BigDecimal objectivePercentage) {
            this.objectivePercentage = objectivePercentage;
        }
        public long getWindowMinutes() { return windowMinutes; }
        public void setWindowMinutes(long windowMinutes) { this.windowMinutes = windowMinutes; }
        public int getMinimumJourneys() { return minimumJourneys; }
        public void setMinimumJourneys(int minimumJourneys) { this.minimumJourneys = minimumJourneys; }
    }
}
