package org.nrg.xdat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventService;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.nrg.xdat.preferences.SecurityPreferences.SECURITY_TOOL_ID;

@NrgPreferenceBean(toolId = SECURITY_TOOL_ID,
                   toolName = "XNAT Security Preferences",
                   description = "Manages site security for the XNAT system.")
@XnatMixIn(ProxiedBeanMixIn.class)
@Slf4j
public class SecurityPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String SECURITY_TOOL_ID = "security";

    @Autowired
    public SecurityPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configPaths) {
        super(preferenceService, eventService, configPaths);
    }

    @NrgPreference(defaultValue = "['" + XdatUserAuthService.LOCALDB + "']")
    public List<String> getEnabledProviders() {
        return getListValue("enabledProviders");
    }

    public void setEnabledProviders(final List<String> enabledProviders) {
        try {
            setListValue("enabledProviders", enabledProviders);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name enabledProviders: something is very wrong here.", e);
        }
    }
}
