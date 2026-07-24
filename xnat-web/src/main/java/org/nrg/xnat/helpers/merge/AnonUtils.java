/*
 * web: org.nrg.xnat.helpers.merge.AnonUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;

import java.util.List;

/**
 * Defines the interface for managing XNAT's DICOM anonymization scripts.
 */
public interface AnonUtils {
    /**
     * The site-config preference IDs for the site-wide anonymization script and its enabled flag. These
     * are also the field names the site-admin anonymization panel submits and the keys REST clients use
     * with /xapi/siteConfig and /xapi/anonymize/settings, so renaming the values is a breaking change.
     * They'd fit naturally alongside SITE_URL on SiteConfigPreferences, but live here so the definition
     * stays within xnat-web.
     */
    String SITEWIDE_ANONYMIZATION_SCRIPT        = "sitewideAnonymizationScript";
    String ENABLE_SITEWIDE_ANONYMIZATION_SCRIPT = "enableSitewideAnonymizationScript";

    Configuration getSiteWideScriptConfiguration();

    Configuration getProjectScriptConfiguration(String projectId);

    boolean isSiteWideScriptEnabled();

    boolean isProjectScriptEnabled(String projectId);

    List<Configuration> getAllScripts();

    List<Configuration> getAllScripts(String projectId);

    String getStudyScript(String studyId) throws ConfigServiceException;

    String getProjectScript(String projectId) throws ConfigServiceException;

    void setProjectScript(String login, String script, String projectId) throws ConfigServiceException;

    String getSiteWideScript() throws ConfigServiceException;

    /**
     * Sets the site-wide anonymization script and/or its enabled state as a single operation. This is the
     * canonical write path for the site-wide settings: it writes the config service copy (the copy that is
     * actually applied to incoming DICOM) with the submitted user, preserves or sets the enabled status
     * explicitly, invalidates the local script cache, and mirrors the values to the site-config preferences
     * so that preference consumers and other nodes (via the preference events) stay consistent.
     *
     * <p>The default implementation delegates to the individual operations for source compatibility with
     * implementations that predate this method; real implementations should override it, as
     * DefaultAnonUtils does, to apply the write ordering and mirroring invariants as a single operation.
     *
     * @param login  The user setting the script.
     * @param script The new script contents, or null to leave the script unchanged.
     * @param enable The new enabled state, or null to preserve the current state.
     */
    default void setSiteWideSettings(String login, String script, Boolean enable) throws ConfigServiceException {
        if (script != null) {
            setSiteWideScript(login, script);
        }
        if (enable != null) {
            if (enable) {
                enableSiteWide(login);
            } else {
                disableSiteWide(login);
            }
        }
    }

    /**
     * Equivalent to {@link #setSiteWideSettings(String, String, Boolean)} with the current enabled state
     * preserved.
     */
    void setSiteWideScript(String login, String script) throws ConfigServiceException;

    void enableSiteWide(String login) throws ConfigServiceException;

    void enableProjectSpecific(String login, String projectId) throws ConfigServiceException;

    void disableSiteWide(String login) throws ConfigServiceException;

    void disableProjectSpecific(String login, String projectId) throws ConfigServiceException;

    void disableStudy(String login, final String studyId) throws ConfigServiceException;
}
