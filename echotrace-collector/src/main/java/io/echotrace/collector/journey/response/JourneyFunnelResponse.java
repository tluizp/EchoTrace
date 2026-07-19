package io.echotrace.collector.journey.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class JourneyFunnelResponse {

    private final String journeyType;
    private final Instant start;
    private final Instant end;
    private final long totalJourneys;
    private final long completedJourneys;
    private final long failedJourneys;
    private final BigDecimal conversionRate;
    private final String stageOrdering;
    private final String dropOffDefinition;
    private final List<FunnelStepResponse> steps;

    public JourneyFunnelResponse(String journeyType, Instant start, Instant end,
                                 long totalJourneys, long completedJourneys,
                                 long failedJourneys, BigDecimal conversionRate,
                                 List<FunnelStepResponse> steps) {
        this.journeyType = journeyType;
        this.start = start;
        this.end = end;
        this.totalJourneys = totalJourneys;
        this.completedJourneys = completedJourneys;
        this.failedJourneys = failedJourneys;
        this.conversionRate = conversionRate;
        this.stageOrdering = "FIRST_OBSERVED";
        this.dropOffDefinition = "DID_NOT_REACH_NEXT_STAGE_IN_WINDOW";
        this.steps = List.copyOf(steps);
    }

    public String getJourneyType() { return journeyType; }
    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
    public long getTotalJourneys() { return totalJourneys; }
    public long getCompletedJourneys() { return completedJourneys; }
    public long getFailedJourneys() { return failedJourneys; }
    public BigDecimal getConversionRate() { return conversionRate; }
    public String getStageOrdering() { return stageOrdering; }
    public String getDropOffDefinition() { return dropOffDefinition; }
    public List<FunnelStepResponse> getSteps() { return steps; }
}
