package org.nrg.xnat.eventservice.services;

import com.google.common.base.MoreObjects;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.eventservice.model.EventServicePrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Event Service preferences, stored as a prefs bean.
 */
@NrgPreferenceBean(toolId = "eventService",
    toolName = "Event Service Prefs",
    description = "Preferences to enable/disable Event Service functionality")
public class EventServicePrefsBean extends AbstractPreferenceBean {
    private static final Logger _log = LoggerFactory.getLogger(EventServicePrefsBean.class);
    private final EventService eventService;

    @Autowired
    public EventServicePrefsBean(@Lazy final EventService eventService, final NrgPreferenceService preferenceService, final ConfigPaths configFolderPaths, final
                                 OrderedProperties initPrefs) {
        super(preferenceService, configFolderPaths, initPrefs);
        this.eventService = eventService;
    }

    // ** Enable/Disable all Event Service operations - overriding all other ES prefs ** //
    @NrgPreference(defaultValue = "false")
    public Boolean getEnabled() {
        return getBooleanValue("enabled");
    }
    public void setEnabled(final Boolean enabled) {
        if (enabled != null) {
            try {
                setBooleanValue(enabled, "enabled");
                if (enabled){
                    eventService.reactivateAllSubscriptions();
                }
            } catch (InvalidPreferenceName e) {
                _log.error("Error setting Event Service preference \"name\".", e.getMessage());
            }
        }
    }


    public EventServicePrefs toPojo(){
        return EventServicePrefs.builder()
                .enabled(this.getEnabled())
                .build();
    }

    public void update(EventServicePrefs prefs){
        if(prefs.enabled() != null) this.setEnabled(prefs.enabled());
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("enabled", getEnabled())
                          .toString();
    }
}
