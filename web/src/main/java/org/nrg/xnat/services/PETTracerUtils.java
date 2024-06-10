/*
 * web: org.nrg.xnat.services.PETTracerUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import org.apache.commons.io.IOUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PETTracerUtils {

    public static final String SITE_WIDE_PATH = PETTracerUtils.buildScriptPath(ResourceScope.SITE_WIDE, "");

    @Autowired
    public PETTracerUtils(final ConfigService configService) {
        _configService = configService;
    }

    public Configuration getTracerList(final String path, final Long project) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving tracer list for {}, {} for project: {}", TOOL_NAME, path, project);
        }

        return project == null ? _configService.getConfig(TOOL_NAME, path) : _configService.getConfig(TOOL_NAME, path, Scope.Project, project.toString());
    }

    @SuppressWarnings("unused")
    public void setProjectTracerList(final String login, final String path, final String tracerList, Long project) throws ConfigServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting tracer list for {}, {} for project: {}", TOOL_NAME, path, project);
        }
        if (project == null) {
            _configService.replaceConfig(login, "", TOOL_NAME, path, tracerList);
        } else {
            _configService.replaceConfig(login, "", TOOL_NAME, path, tracerList, Scope.Project, project.toString());
        }
    }

    public Configuration getSiteWideTracerList() {
        return getTracerList(PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, ""), null);
    }

    public void setSiteWideTracerList(String login, String tracerList) throws ConfigServiceException {
        _configService.replaceConfig(login, "", TOOL_NAME, SITE_WIDE_PATH, tracerList);
    }

    public static String getDefaultTracerList() throws IOException {
        final List<String> tracers = Lists.newArrayList();
        final List<Resource> resources = BasicXnatResourceLocator.getResources(DEFAULT_TRACER_LIST);
        for (final Resource resource : resources) {
            try (final InputStream input = resource.getInputStream()) {
                tracers.addAll(IOUtils.readLines(input, "UTF-8"));
            }
        }
        return Joiner.on("\n").join(tracers);
    }

    // flat out stolen from DicomEdit.java
    public static String buildScriptPath(ResourceScope scope, Object project) {
        switch (scope) {
            case PROJECT:
                return DicomEdit.getProjectScriptPath(project);
            case SITE_WIDE:
                return "tracers";
            default:
                return "";
        }
    }

    public enum ResourceScope {
        SITE_WIDE,
        PROJECT
    }

    private static final Logger logger = LoggerFactory.getLogger(PETTracerUtils.class);

    private static final String TOOL_NAME = "tracers";

    private static final String DEFAULT_TRACER_LIST = "classpath*:META-INF/xnat/defaults/**/PET-tracers.txt";

    private final ConfigService _configService;
}
