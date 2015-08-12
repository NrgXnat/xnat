package org.nrg.automation.services;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

public class EventReferencedException extends NrgServiceException {
    private final String _eventId;
    private final int _referenceCount;

    public EventReferencedException(final String eventId, final int referenceCount) {
        super(NrgServiceError.PermissionsViolation, "The event " + eventId + " has one or more outstanding referenceCount and can't be deleted without a cascade.");
        _eventId = eventId;
        _referenceCount = referenceCount;
    }

    public String getEventId() {
        return _eventId;
    }

    public int getReferenceCount() {
        return _referenceCount;
    }
}
