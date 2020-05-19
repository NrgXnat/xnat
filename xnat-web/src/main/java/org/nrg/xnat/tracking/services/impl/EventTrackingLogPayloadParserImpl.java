package org.nrg.xnat.tracking.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.xnat.tracking.services.EventTrackingLogPayloadParser;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.entities.EventTrackingLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;

@Service
public class EventTrackingLogPayloadParserImpl implements EventTrackingLogPayloadParser {
    private final ObjectMapper objectMapper;

    @Autowired
    public EventTrackingLogPayloadParserImpl(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public EventTrackingLog getParsedPayload(EventTrackingData eventTrackingData) throws IOException {
        return parsePayload(eventTrackingData.getPayload());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public String stringifyPayload(EventTrackingLog statusLog) throws IOException {
        statusLog.sortEntryList();
        return objectMapper.writeValueAsString(statusLog);
    }

    private EventTrackingLog parsePayload(String payload) throws IOException {
        if (payload == null) {
            return null;
        }
        return objectMapper.readValue(payload, EventTrackingLog.class);
    }
}
