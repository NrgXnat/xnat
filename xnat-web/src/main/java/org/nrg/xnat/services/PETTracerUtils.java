package org.nrg.xnat.services;/*
 * org.nrg.xnat.services.PETTracerUtils.java
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Created 8/7/14 12:50 PM
 */

import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xft.XFT;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;

@Service
public class PETTracerUtils {
    public PETTracerUtils() throws Exception {
        if (_instance != null) {
            throw new Exception("The PETTracerUtils service is already initialized, try calling getInstance() instead.");
        }
        _instance = this;
    }

    public static PETTracerUtils getService() {
        return _instance;
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

    public void setSiteWideTracerList(String login, String path, String tracerList) throws ConfigServiceException {
        _configService.replaceConfig(login, "", TOOL_NAME, path, tracerList);
    }

    public static File getDefaultTracerList() throws FileNotFoundException {
        final File def = new File(XFT.GetConfDir(), DEFAULT_TRACER_LIST);
        if (def.exists()) {
            return def;
        } else {
            throw new FileNotFoundException("Default tracer list: " + DEFAULT_TRACER_LIST + " not found in " + XFT.GetConfDir());
        }
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

    private static final String DEFAULT_TRACER_LIST = "PET-tracers.txt";

    private static PETTracerUtils _instance;

    @Inject
    private ConfigService _configService;
}
