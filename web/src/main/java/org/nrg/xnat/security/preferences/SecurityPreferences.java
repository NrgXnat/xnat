package org.nrg.xnat.security.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unused")
@NrgPreferenceBean(toolId = SecurityPreferences.SECURITY_TOOL_ID,
                   toolName = "XNAT Security Preferences",
                   description = "Manages security configurations and settings for the XNAT system.")
@XnatMixIn(ProxiedBeanMixIn.class)
@Slf4j
public class SecurityPreferences extends EventTriggeringAbstractPreferenceBean {
    public static final String SECURITY_TOOL_ID = "security";

    @Autowired
    public SecurityPreferences(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService, final ConfigPaths configPaths, final OrderedProperties initPrefs) {
        super(preferenceService, eventService, configPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "false")
    public boolean getAllowInsecureCookies() {
        return getBooleanValue("allowInsecureCookies");
    }

    public void setAllowInsecureCookies(final boolean allowInsecureCookies) {
        try {
            setBooleanValue(allowInsecureCookies, "allowInsecureCookies");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'allowInsecureCookies': something is very wrong here.", e);
        }
    }
}
