package org.nrg.automation.services;

public interface AutomationService {
    String AUTOMATION_ENABLED_PROPERTY = "${automation.enabled:false}";

    void incrementEventId(String projectId, String srcEventClass, String eventText);

    void addValueToStoredFilters(final String externalId, final String srcEventClass, final String field, final String value);

    /**
     * Indicates whether automation is currently enabled on this system. Automation can be disabled to prevent any
     * automated scripts from running. Note that enabling and disabling automation cannot be changed at runtime,
     * but must be set at application startup by setting the <pre>automation.enabled</pre> property in the
     * <pre>xnat-conf.properties</pre> file.
     *
     * @return Returns <pre>true</pre> if automation is enabled, <pre>false</pre> otherwise.
     */
    boolean getAutomationEnabled();
}
