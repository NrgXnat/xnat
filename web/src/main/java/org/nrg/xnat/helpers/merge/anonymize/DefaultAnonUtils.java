/*
 * web: org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge.anonymize;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("RedundantThrows")
@Service
@Slf4j
public class DefaultAnonUtils implements AnonUtils {
    @Autowired
    public DefaultAnonUtils(final ConfigService configService, final CacheManager cacheManager) throws Exception {
        if (_instance != null) {
            throw new Exception("The AnonUtils service is already initialized, try calling getInstance() instead.");
        }
        _instance = this;
        _configService = configService;
        _cache = cacheManager.getCache(ANON_SCRIPT_CACHE);
    }

    public static AnonUtils getService() {
        if (_instance == null) {
            _instance = (DefaultAnonUtils) XDAT.getContextService().getBean(AnonUtils.class);
        }
        return _instance;
    }

    public static String getDefaultScript() throws IOException {
        final List<Resource> resources = BasicXnatResourceLocator.getResources(DEFAULT_ANON_SCRIPT);
        if (resources.isEmpty()) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Didn't find any default anonymization scripts at: " + DEFAULT_ANON_SCRIPT);
        }
        if (resources.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Found more than one \"default\" anonymization script: " + resources.stream().map(DefaultAnonUtils::getURI).collect(Collectors.joining(", ")));
        }
        try (final InputStream input = resources.get(0).getInputStream()) {
            return StringUtils.join(IOUtils.readLines(input, "UTF-8"), "\n");
        }
    }

    private static String getURI(final Resource resource) {
        try {
            return resource.getURI().toString();
        } catch (IOException e) {
            return resource.toString();
        }
    }

    public static void invalidateSitewideAnonCache() {
        _instance._cache.evict(SITE_WIDE);
    }

    public static Configuration getCachedSitewideAnon() throws Exception {
        final Cache.ValueWrapper cached = _instance._cache.get(SITE_WIDE);
        if (null != cached) {
            return (Configuration) cached.get();
        } else {
            final Configuration configuration = getService().getSiteWideScriptConfiguration();
            _instance._cache.put(SITE_WIDE, configuration);
            return configuration;
        }
    }

    @Override
    public Configuration getSiteWideScriptConfiguration() {
        return getProjectScriptConfiguration(null);
    }

    @Override
    public Configuration getProjectScriptConfiguration(final String projectId) {
        final boolean isSiteWide = StringUtils.isBlank(projectId);
        final String path = isSiteWide ? SITE_WIDE_PATH : DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving script for tool {} path {} for project {}", DicomEdit.ToolName, path, projectId);
        }

        return isSiteWide
               ? _configService.getConfig(DicomEdit.ToolName, path)
               : _configService.getConfig(DicomEdit.ToolName, path, Scope.Project, projectId);
    }

    @Override
    public boolean isSiteWideScriptEnabled() {
        return isProjectScriptEnabled(null);
    }

    @Override
    public boolean isProjectScriptEnabled(final String projectId) {
        final Configuration config = getProjectScriptConfiguration(projectId);
        final boolean enabled = config != null && config.getStatus().equals(Configuration.ENABLED_STRING);
        if (log.isDebugEnabled()) {
            if (StringUtils.isNotBlank(projectId)) {
                log.debug("Retrieved status {} for the site-wide anonymization script", enabled);
            } else {
                log.debug("Retrieved status {} for the anonymization script for project {}", enabled, projectId);
            }
        }
        return enabled;
    }

    @Override
    public List<Configuration> getAllScripts() {
        return getAllScripts(null);
    }

    @Override
    public List<Configuration> getAllScripts(final String projectId) {
        final boolean isSiteWide = projectId == null;
        final List<Configuration> scripts = isSiteWide
                                            ? _configService.getConfigsByTool(DicomEdit.ToolName)
                                            : _configService.getConfigsByTool(DicomEdit.ToolName, Scope.Project, projectId);

        if (log.isDebugEnabled()) {
            final String identifier = isSiteWide ? "the site" : "project: " + projectId;
            if (scripts == null) {
                log.debug("Retrieved no scripts for tool {} identifier {}", DicomEdit.ToolName, identifier);
            } else if (scripts.size() == 0) {
                log.debug("Retrieved no scripts for tool {} identifier {}", DicomEdit.ToolName, identifier);
            } else {
                log.debug("Retrieved {} scripts for tool {} identifier {}", scripts.size(), DicomEdit.ToolName, identifier);
            }
        }

        return scripts;
    }

    @Override
    public String getStudyScript(String studyId) throws ConfigServiceException{
        if (log.isDebugEnabled()) {
            log.debug("Getting {} script for study: {}", DicomEdit.ToolName, studyId);
        }
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.STUDY, studyId);
        final boolean enabled = StringUtils.equals(_configService.getStatus(DicomEdit.ToolName, path, Scope.Site, studyId),Configuration.ENABLED_STRING);
        if(enabled) {
            return _configService.getConfigContents(DicomEdit.ToolName, path, Scope.Site, studyId);
        }
        else{
            return null;
        }
    }

    public static void setStudyScript(String login, String script, String studyId) throws ConfigServiceException{
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.STUDY, studyId);
        if (log.isDebugEnabled()) {
            log.debug("User {} is setting {} script for project {}", login, DicomEdit.ToolName, studyId);
        }
        if (studyId == null) {
            XDAT.getConfigService().replaceConfig(login, "", DicomEdit.ToolName, path, script);
        } else {
            XDAT.getConfigService().replaceConfig(login, "", DicomEdit.ToolName, path, script, Scope.Site, studyId);
        }

    }

    @Override
    public String getProjectScript(final String projectId) throws ConfigServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Getting {} script for project: {}", DicomEdit.ToolName, projectId);
        }
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        return _configService.getConfigContents(DicomEdit.ToolName, path, Scope.Project, projectId);
    }

    @Override
    public void setProjectScript(final String login, final String script, final String projectId) throws ConfigServiceException {
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        if (log.isDebugEnabled()) {
            log.debug("User {} is setting {} script for project {}", login, DicomEdit.ToolName, projectId);
        }
        if (projectId == null) {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script);
        } else {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script, Scope.Project, projectId);
        }
    }

    @Override
    public String getSiteWideScript() throws ConfigServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Getting {} site-wide script", DicomEdit.ToolName);
        }
        return _configService.getConfigContents(DicomEdit.ToolName, SITE_WIDE_PATH);
    }

    @Override
    public void setSiteWideScript(String login, String script) throws ConfigServiceException {
        if (log.isDebugEnabled()) {
            log.debug("User {} is setting {} site-wide script", login, DicomEdit.ToolName);
        }
        _configService.replaceConfig(login, "", DicomEdit.ToolName, SITE_WIDE_PATH, script);
        invalidateSitewideAnonCache();
    }

    @Override
    public void enableSiteWide(String login) throws ConfigServiceException {
        enableProjectSpecific(login, null);
        invalidateSitewideAnonCache();
    }

    @Override
    public void enableProjectSpecific(final String login, final String projectId) throws ConfigServiceException {
        if (StringUtils.isBlank(projectId)) {
            _configService.enable(login, "", DicomEdit.ToolName, SITE_WIDE_PATH);
        } else {
            final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
            _configService.enable(login, "", DicomEdit.ToolName, path, Scope.Project, projectId);
        }
    }

    @Override
    public void disableSiteWide(final String login) throws ConfigServiceException {
        disableProjectSpecific(login, null);
        invalidateSitewideAnonCache();
    }

    @Override
    public void disableProjectSpecific(String login, final String projectId) throws ConfigServiceException {
        if (StringUtils.isBlank(projectId)) {
            _configService.disable(login, "", DicomEdit.ToolName, SITE_WIDE_PATH);
        } else {
            final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
            _configService.disable(login, "", DicomEdit.ToolName, path, Scope.Project, projectId);
        }
    }

    @Override
    public void disableStudy(String login, final String studyId) throws ConfigServiceException {
        if (StringUtils.isNotBlank(studyId)) {
            final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.STUDY, studyId);
            _configService.disable(login, "", DicomEdit.ToolName, path, Scope.Site, studyId);
        }
    }

    private static final String DEFAULT_ANON_SCRIPT       = "classpath*:META-INF/xnat/defaults/**/id.das";
    private static final String SITE_WIDE_PATH            = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null);
    private static final String SITE_WIDE                 = "site-wide";
    private static final String ANON_SCRIPT_CACHE         = DefaultAnonUtils.class.getSimpleName() + "ScriptsCache";

    private static DefaultAnonUtils _instance;

    private final Cache         _cache;
    private final ConfigService _configService;
}
