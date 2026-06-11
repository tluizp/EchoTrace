package io.echotrace.collector.metrics.response;

import java.util.Objects;

public final class EventResponse {
    private final String eventName;
    private final String displayName;

    public EventResponse(
            String eventName,
            String displayName
    ) {
        this.eventName = eventName;
        this.displayName = displayName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EventResponse) obj;
        return Objects.equals(this.eventName, that.eventName) &&
                Objects.equals(this.displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, displayName);
    }

    @Override
    public String toString() {
        return "EventResponse[" +
                "eventName=" + eventName + ", " +
                "displayName=" + displayName + ']';
    }

}
