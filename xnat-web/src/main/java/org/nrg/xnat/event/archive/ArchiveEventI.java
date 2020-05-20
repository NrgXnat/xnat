package org.nrg.xnat.event.archive;

import org.nrg.framework.status.StatusMessage;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.tracking.model.TrackableEvent;

public interface ArchiveEventI extends TrackableEvent {
    enum Status {
        Waiting,
        InProgress,
        Warning,
        Completed,
        Failed;

        public StatusMessage.Status status() {
            switch (this) {
                case Completed:
                    return StatusMessage.Status.COMPLETED;

                case Failed:
                    return StatusMessage.Status.FAILED;

                case InProgress:
                    return StatusMessage.Status.PROCESSING;

                default:
                    return StatusMessage.Status.WARNING;
            }
        }
    }

    /**
     * Returns an ID that identifies the event by its properties.
     *
     * @return An ID based on the event properties.
     */
    String getArchiveEventId();

    /**
     * Gets the project ID associated with the session. This is generally only set for events
     * associated with sessions in the prearchive, as the project ID can be retrieved through
     * the experiment for archived sessions.
     *
     * @return The project ID associated with the session.
     */
    String getProject();

    /**
     * Gets the timestamp associated with the session. This is only set for events associated with
     * session in the prearchive, as the project ID can be retrieved through the experiment for archived
     * sessions.
     *
     * @return The timestamp associated with the session.
     */
    String getTimestamp();

    /**
     * Gets the ID of the session.
     */
    String getSession();

    /**
     * Returns the archive operation for this event.
     *
     * @return The archive operation.
     */
    Operation getOperation();

    /**
     * Returns the status of the archive operation.
     *
     * @return The current status of the archive operation.
     */
    Status getStatus();

    /**
     * Returns the current progress of the archive operation. This should be
     * a value from 0 to 100 indicating the percentage of the task completed.
     *
     * @return The current progress of the archive operation.
     */
    int getProgress();

    /**
     * Returns as message describing the archive operation event.
     *
     * @return A description of the archive operation.
     */
    String getMessage();

    /**
     * Get the time of the event in MS
     * @return event time
     */
    long getEventTime();
}
