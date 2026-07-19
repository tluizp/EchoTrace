package io.echotrace.collector.journey.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class JourneyResponse {

    private final String journeyId;
    private final String journeyType;
    private final String status;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final long durationMs;
    private final BigDecimal affectedValue;
    private final String currency;
    private final List<JourneyEventResponse> events;

    public JourneyResponse(String journeyId, String journeyType, String status,
                           Instant startedAt, Instant finishedAt, long durationMs,
                           BigDecimal affectedValue, String currency,
                           List<JourneyEventResponse> events) {
        this.journeyId = journeyId;
        this.journeyType = journeyType;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMs = durationMs;
        this.affectedValue = affectedValue;
        this.currency = currency;
        this.events = List.copyOf(events);
    }

    public String getJourneyId() { return journeyId; }
    public String getJourneyType() { return journeyType; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public long getDurationMs() { return durationMs; }
    public BigDecimal getAffectedValue() { return affectedValue; }
    public String getCurrency() { return currency; }
    public List<JourneyEventResponse> getEvents() { return events; }
}
