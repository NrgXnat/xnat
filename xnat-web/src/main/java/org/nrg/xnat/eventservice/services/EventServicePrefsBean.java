package org.nrg.xnat.eventservice.services;

import com.google.common.base.MoreObjects;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event Service preferences, stored as a prefs bean.
 */
@NrgPreferenceBean(toolId = "eventService",
    toolName = "Event Service Prefs",
    description = "Preferences to enable/disable Event Service functionality")
public class EventServicePrefsBean extends AbstractPreferenceBean {
    private static final Logger _log = LoggerFactory.getLogger(EventServicePrefsBean.class);

    public EventServicePrefsBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @Autowired
    public EventServicePrefsBean(final NrgPreferenceService preferenceService, final ConfigPaths configFolderPaths, final
                                 OrderedProperties initPrefs) {
        super(preferenceService, configFolderPaths, initPrefs);
    }

    // ** Enable/Disable all Event Service operations - overriding all other ES prefs ** //
    @NrgPreference(defaultValue = "true")
    public Boolean getEnabled() {
        return getBooleanValue("enabled");
    }
    public void setEnabled(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "enabled");
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }

    // ** Enable/Disable triggering events from core XNAT operations ** //
    @NrgPreference(defaultValue = "true")
    public Boolean getTriggerCoreEvents() {
        return getBooleanValue("triggerCoreEvents") && getEnabled();
    }
    public void setTriggerCoreEvents(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "triggerCoreEvents");
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }

    // ** Enable/Disable triggering events from XNAT workflow status updates ** //
    @NrgPreference(defaultValue = "true")
    public Boolean getTriggerWorkflowStatusEvents() {
        return getBooleanValue("triggerWorkflowStatusEvents") && getEnabled();
    }
    public void setTriggerWorkflowStatusEvents(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "triggerWorkflowStatusEvents");
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }

    // ** Enable/Disable triggering events from XNAT plugins ** //
    @NrgPreference(defaultValue = "true")
    public Boolean getTriggerPluginEvents() {
        return getBooleanValue("triggerPluginEvents") && getEnabled();
    }
    public void setTriggerPluginEvents(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "triggerPluginEvents");
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }

    // ** Enable/Disable responding to triggered events ** //
    @NrgPreference(defaultValue = "true")
    public Boolean getRespondToEvents() {
        return getBooleanValue("respondToEvents") && getEnabled();
    }
    public void setRespondToEvents(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "respondToEvents");
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("enabled", getEnabled())
                          .add("triggeringCoreEvents", getTriggerCoreEvents())
                          .add("triggeringWorkflowStatusEvents", getTriggerWorkflowStatusEvents())
                          .add("triggeringPluginEvents", getTriggerPluginEvents())
                          .add("respondToEvents", getRespondToEvents())
                          .toString();
    }
}
