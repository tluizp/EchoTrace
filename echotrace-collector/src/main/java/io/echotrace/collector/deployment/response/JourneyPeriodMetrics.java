package io.echotrace.collector.deployment.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public final class JourneyPeriodMetrics {

    private final Instant start;
    private final Instant end;
    private final long totalJourneys;
    private final long completedJourneys;
    private final long failedJourneys;
    private final BigDecimal conversionRate;
    private final BigDecimal failureRate;
    private final Map<String, BigDecimal> affectedValueByCurrency;

    public JourneyPeriodMetrics(Instant start, Instant end, long totalJourneys,
                                long completedJourneys, long failedJourneys,
                                BigDecimal conversionRate, BigDecimal failureRate,
                                Map<String, BigDecimal> affectedValueByCurrency) {
        this.start = start;
        this.end = end;
        this.totalJourneys = totalJourneys;
        this.completedJourneys = completedJourneys;
        this.failedJourneys = failedJourneys;
        this.conversionRate = conversionRate;
        this.failureRate = failureRate;
        this.affectedValueByCurrency = Map.copyOf(affectedValueByCurrency);
    }

    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
    public long getTotalJourneys() { return totalJourneys; }
    public long getCompletedJourneys() { return completedJourneys; }
    public long getFailedJourneys() { return failedJourneys; }
    public BigDecimal getConversionRate() { return conversionRate; }
    public BigDecimal getFailureRate() { return failureRate; }
    public Map<String, BigDecimal> getAffectedValueByCurrency() { return affectedValueByCurrency; }
}
