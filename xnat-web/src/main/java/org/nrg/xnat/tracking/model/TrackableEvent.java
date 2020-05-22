package org.nrg.xnat.tracking.model;

import org.nrg.framework.event.EventI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public interface TrackableEvent extends EventI {
    /**
     * Return a unique identifier for tracking the event
     * @return the unique ID
     */
    @Nonnull
    String getTrackingId();

    /**
     * Has the event succeeded?
     * @return T/F
     */
    boolean isSuccess();

    /**
     * Has the event completed?
     * @return T/F
     */
    boolean isCompleted();

    /**
     * Return a status message or null
     * @return the status message or null
     */
    @Nullable
    String getMessage();

    /**
     * Update tracking payload with info from this event
     * @param currentPayload the current payload or null
     * @return the updated payload
     * @throws IOException parsing/stringification issues
     */
    String updateTrackingPayload(@Nullable String currentPayload) throws IOException;
}
