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
    Configuration getSiteWideScriptConfiguration();

    Configuration getProjectScriptConfiguration(String projectId);

    boolean isSiteWideScriptEnabled();

    boolean isProjectScriptEnabled(String projectId);

    List<Configuration> getAllScripts();

    List<Configuration> getAllScripts(String projectId);

    String getProjectScript(String projectId) throws ConfigServiceException;

    void setProjectScript(String login, String script, String projectId) throws ConfigServiceException;

    String getSiteWideScript() throws ConfigServiceException;

    void setSiteWideScript(String login, String script) throws ConfigServiceException;

    void enableSiteWide(String login) throws ConfigServiceException;

    void enableProjectSpecific(String login, String projectId) throws ConfigServiceException;

    void disableSiteWide(String login) throws ConfigServiceException;

    void disableProjectSpecific(String login, String projectId) throws ConfigServiceException;
}
