package org.nrg.automation.services.impl;

public interface AutomationService {
    void incrementEventId(String projectId, String srcEventClass, String eventText);
    void addValueToStoredFilters(final String externalId, final String srcEventClass, final String field, final String value);

}
