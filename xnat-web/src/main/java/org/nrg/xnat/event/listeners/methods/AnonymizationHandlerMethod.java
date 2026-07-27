/*
 * web: org.nrg.xnat.event.listeners.methods.AnonymizationHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cache-coherence handler for the site-wide anonymization preferences. The site-wide script and its enabled
 * flag are written synchronously — config service first, preferences second — by {@link
 * AnonUtils#setSiteWideSettings(String, String, Boolean)}, so by the time the preference events handled here
 * fire (locally, or replayed on another node by the dist-events plugin), both stores are already up to date.
 * This handler only invalidates the local script cache so the next read picks up the new values.
 *
 * <p>This handler must NOT write to the config service. It historically copied the preference values into the
 * config service from here, which ran asynchronously, attributed every change to the admin user, raced its own
 * replays on multi-node deployments (duplicating configuration versions), and could persist a stale preference
 * read over a newer script. See XNAT-5830 for the original race this design tried to mitigate.
 */
@Component
@Slf4j
public class AnonymizationHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public AnonymizationHandlerMethod(final SiteConfigPreferences preferences, final AnonUtils anonUtils) {
        super(AnonUtils.ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT, AnonUtils.SITEWIDE_ANONYMIZATION_SCRIPT);
        _preferences = preferences;
        _anonUtils = anonUtils;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        // On the node that made the change, this invalidation is redundant: AnonUtils.setSiteWideSettings()
        // already invalidated synchronously. But on every OTHER node, this handler — triggered by the
        // dist-events preference relay — is the ONLY cache invalidation. Don't remove either call as a
        // deduplication: the direct call gives the writing node immediate coherence, this one gives the
        // peers theirs.
        log.debug("Invalidating the cached site-wide anonymization script on change to the {} preference", preference);
        DefaultAnonUtils.invalidateSitewideAnonCache();
        warnOnMismatch();
    }

    /**
     * Drift alarm: the config service copy of the script is what gets applied to incoming DICOM, while the
     * preference copy is what most UI surfaces display. The canonical write path keeps them in step, so a
     * mismatch here means something wrote one store without the other (e.g., a direct database edit).
     */
    private void warnOnMismatch() {
        try {
            final Configuration configuration = _anonUtils.getSiteWideScriptConfiguration();
            final String        configScript  = StringUtils.trimToNull(configuration != null ? configuration.getContents() : null);
            final String        prefScript    = StringUtils.trimToNull(_preferences.getSitewideAnonymizationScript());
            final boolean       configEnabled = configuration != null && StringUtils.equals(configuration.getStatus(), Configuration.ENABLED_STRING);
            final boolean       prefEnabled   = Boolean.TRUE.equals(_preferences.getEnableSitewideAnonymizationScript());
            if (!StringUtils.equals(configScript, prefScript) || configEnabled != prefEnabled) {
                log.warn("The site-wide anonymization settings differ between the config service (script {} characters, {}) and the site-config preferences (script {} characters, {}). The config service copy is what gets applied to incoming DICOM. These are kept in step by AnonUtils.setSiteWideSettings(), so something has updated one store without the other.",
                         configScript != null ? configScript.length() : 0, configEnabled ? "enabled" : "disabled",
                         prefScript != null ? prefScript.length() : 0, prefEnabled ? "enabled" : "disabled");
            }
        } catch (Exception e) {
            log.debug("Unable to compare the site-wide anonymization settings stores", e);
        }
    }

    private final SiteConfigPreferences _preferences;
    private final AnonUtils             _anonUtils;
}
