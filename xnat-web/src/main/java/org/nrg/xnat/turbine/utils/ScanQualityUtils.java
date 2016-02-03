/*
 * org.nrg.xnat.turbine.utils.ScanQualityUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */
package org.nrg.xnat.turbine.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ScanQualityUtils {
    private static final List<String> DEFAULT_LABELS = new ArrayList<String>();
    static {
        DEFAULT_LABELS.add("usable");
        DEFAULT_LABELS.add("questionable");
        DEFAULT_LABELS.add("unusable");
    }

    private static Logger _log = LoggerFactory.getLogger(ScanQualityUtils.class);

    private ScanQualityUtils() {}

    public static List<String> getQualityLabels(final String project, final UserI user) {
        final ConfigService configService = XDAT.getConfigService();
        final String projectId;
        if (Strings.isNullOrEmpty(project) || StringUtils.equals("Unassigned",project)) {
            projectId = null;
        } else {
            final XnatProjectdata projectData = XnatProjectdata.getXnatProjectdatasById(project, user, false);
            if (projectData != null) {
                projectId = (String) projectData.getItem().getProps().get("id");
            } else {
                projectId = null;
            }
        }
        final String configVal = configService.getConfigContents("scan-quality", "labels", StringUtils.isBlank(projectId) ? Scope.Site : Scope.Project, projectId);
        if (Strings.isNullOrEmpty(configVal)) {
            if (Strings.isNullOrEmpty(project)) {
                try {
                    XDAT.getConfigService().replaceConfig("admin", "Set default scan quality labels configuration", "scan-quality", "labels", true, Joiner.on(", ").join(DEFAULT_LABELS));
                } catch (ConfigServiceException e) {
                    _log.error("An error occurred trying to save the default scan quality labels", e);
                }
                return DEFAULT_LABELS;
            } else {
                return getQualityLabels(null, user);    // try the site config
            }
        } else {
            return Arrays.asList(configVal.trim().split("\\s*,\\s*"));
        }
    }
}
