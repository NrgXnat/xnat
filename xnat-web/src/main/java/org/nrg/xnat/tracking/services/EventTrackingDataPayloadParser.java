package org.nrg.xnat.tracking.services;

import org.nrg.xnat.tracking.entities.EventTrackingData;

import java.io.IOException;

public interface EventTrackingDataPayloadParser<E> {
    /**
     * Get payload parsed as POJO
     * @param eventTrackingData the event listener
     * @return the POJO
     * @throws IOException if parsing fails
     */
    E getParsedPayload(EventTrackingData eventTrackingData) throws IOException;

    /**
     * Stringify payload
     * @param payload the payload POJO
     * @return the string
     * @throws IOException if parsing fails
     */
    String stringifyPayload(E payload) throws IOException;
}
