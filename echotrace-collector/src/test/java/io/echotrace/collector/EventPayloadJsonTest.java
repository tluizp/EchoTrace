package io.echotrace.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.echotrace.model.EventPayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventPayloadJsonTest {

    @Test
    void deserializesSpecificationTwoBusinessOutcome() throws Exception {
        String json = "{\"specVersion\":\"2.0\",\"eventId\":\"event-1\",\"eventVersion\":1,"
                + "\"eventName\":\"payment.processed\",\"createdAt\":\"2026-07-19T12:00:00Z\","
                + "\"observedAt\":\"2026-07-19T12:00:01Z\",\"businessOutcome\":{"
                + "\"name\":\"payment.approval\",\"journeyId\":\"order-42\","
                + "\"value\":349.90,\"currency\":\"BRL\"}}";

        EventPayload payload = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(json, EventPayload.class);

        assertEquals("payment.approval", payload.getBusinessOutcome().getName());
        assertEquals("order-42", payload.getBusinessOutcome().getJourneyId());
    }
}
