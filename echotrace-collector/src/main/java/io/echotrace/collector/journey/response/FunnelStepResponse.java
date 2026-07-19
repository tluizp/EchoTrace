package io.echotrace.collector.journey.response;

import java.math.BigDecimal;

public final class FunnelStepResponse {

    private final int position;
    private final String stage;
    private final long reachedJourneys;
    private final long successfulJourneys;
    private final long failedJourneys;
    private final long dropOffJourneys;
    private final BigDecimal conversionFromPrevious;
    private final BigDecimal conversionFromStart;

    public FunnelStepResponse(int position, String stage, long reachedJourneys,
                              long successfulJourneys, long failedJourneys,
                              long dropOffJourneys, BigDecimal conversionFromPrevious,
                              BigDecimal conversionFromStart) {
        this.position = position;
        this.stage = stage;
        this.reachedJourneys = reachedJourneys;
        this.successfulJourneys = successfulJourneys;
        this.failedJourneys = failedJourneys;
        this.dropOffJourneys = dropOffJourneys;
        this.conversionFromPrevious = conversionFromPrevious;
        this.conversionFromStart = conversionFromStart;
    }

    public int getPosition() { return position; }
    public String getStage() { return stage; }
    public long getReachedJourneys() { return reachedJourneys; }
    public long getSuccessfulJourneys() { return successfulJourneys; }
    public long getFailedJourneys() { return failedJourneys; }
    public long getDropOffJourneys() { return dropOffJourneys; }
    public BigDecimal getConversionFromPrevious() { return conversionFromPrevious; }
    public BigDecimal getConversionFromStart() { return conversionFromStart; }
}
