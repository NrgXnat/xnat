package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AnonymizationHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public AnonymizationHandlerMethod(final SiteConfigPreferences preferences, final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider);
        _preferences = preferences;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateAnon();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateAnon();
        }
    }

    private void updateAnon() {
        try {
            AnonUtils.getService().setSiteWideScript(getAdminUsername(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null), _preferences.getSitewideAnonymizationScript());
        } catch (Exception e) {
            _log.error("Failed to set sitewide anon script.", e);
        }
        try {
            if (_preferences.getEnableSitewideAnonymizationScript()) {
                AnonUtils.getService().enableSiteWide(getAdminUsername(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null));
            } else {
                AnonUtils.getService().disableSiteWide(getAdminUsername(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null));
            }
        } catch (Exception e) {
            _log.error("Failed to enable/disable sitewide anon script.", e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(AnonymizationHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("enableSitewideAnonymizationScript", "sitewideAnonymizationScript"));

    private final SiteConfigPreferences _preferences;
}
