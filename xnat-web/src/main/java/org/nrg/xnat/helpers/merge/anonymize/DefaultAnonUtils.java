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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("RedundantThrows")
@Service
@Slf4j
public class DefaultAnonUtils implements AnonUtils {
    @Autowired
    public DefaultAnonUtils(final ConfigService configService, final SiteConfigPreferences preferences, final JCacheHelper cacheHelper) throws Exception {
        if (_instance != null) {
            throw new Exception("The AnonUtils service is already initialized, try calling getInstance() instead.");
        }
        _instance      = this;
        _configService = configService;
        _preferences   = preferences;
        _cache         = cacheHelper.getCache(ANON_SCRIPT_CACHE, String.class, Configuration.class);
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
        try (final InputStream input = resources.getFirst().getInputStream()) {
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
        _instance._cache.remove(SITE_WIDE);
    }

    public static Configuration getCachedSitewideAnon() throws Exception {
        return ObjectUtils.getIfNull(_instance._cache.get(SITE_WIDE), () -> {
            final Configuration configuration = getService().getSiteWideScriptConfiguration();
            _instance._cache.put(SITE_WIDE, configuration);
            return configuration;
        });
    }

    @Override
    public Configuration getSiteWideScriptConfiguration() {
        return getProjectScriptConfiguration(null);
    }

    @Override
    public Configuration getProjectScriptConfiguration(final String projectId) {
        final boolean isSiteWide = StringUtils.isBlank(projectId);
        final String  path       = isSiteWide ? SITE_WIDE_PATH : DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving script for tool {} path {} for project {}", DicomEdit.ToolName, path, projectId);
        }

        return isSiteWide
               ? _configService.getConfig(DicomEdit.ToolName, path)
               : _configService.getConfig(DicomEdit.ToolName, path, Scope.Project, projectId);
    }

    @Override
    public boolean isSiteWideScriptEnabled() {
        try {
            final Configuration config = getCachedSitewideAnon();
            return config != null && config.getStatus().equals(Configuration.ENABLED_STRING);
        } catch (Exception e) {
            log.error("Error checking site-wide anon script status, falling back to uncached check", e);
            return isProjectScriptEnabled(null);
        }
    }

    @Override
    public boolean isProjectScriptEnabled(final String projectId) {
        final Configuration config  = getProjectScriptConfiguration(projectId);
        final boolean       enabled = config != null && config.getStatus().equals(Configuration.ENABLED_STRING);
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
    public String getStudyScript(String studyId) throws ConfigServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Getting {} script for study: {}", DicomEdit.ToolName, studyId);
        }
        final String  path    = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.STUDY, studyId);
        final boolean enabled = StringUtils.equals(_configService.getStatus(DicomEdit.ToolName, path, Scope.Site, studyId), Configuration.ENABLED_STRING);
        if (enabled) {
            return _configService.getConfigContents(DicomEdit.ToolName, path, Scope.Site, studyId);
        } else {
            return null;
        }
    }

    public static void setStudyScript(String login, String script, String studyId) throws ConfigServiceException {
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
    public void setSiteWideSettings(final String login, final String script, final Boolean enable) throws ConfigServiceException {
        if (log.isDebugEnabled()) {
            log.debug("User {} is setting the site-wide {} settings: script {}, enable {}", login, DicomEdit.ToolName, script != null ? "updated" : "unchanged", enable != null ? enable : "preserved");
        }

        final Configuration current    = getSiteWideScriptConfiguration();
        // A fresh install has no site-wide configuration and replaceConfig() creates it enabled, so treat
        // "no config" as enabled when preserving status. This matches the XNAT-4825 semantics.
        final boolean       wasEnabled = current == null || StringUtils.equals(current.getStatus(), Configuration.ENABLED_STRING);
        final boolean       enabled    = enable != null ? enable : wasEnabled;

        // Write the script first and the status second: replaceConfig() always creates the new revision
        // enabled (XNAT-4825), so a disable must follow the script write. The brief enabled window is in the
        // safe direction (extra anonymization), and both writes happen synchronously on this thread — the
        // asynchronous, event-driven form of this same sequence is what produced XNAT-5830.
        if (script != null && (current == null || !StringUtils.equals(script, current.getContents()))) {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, SITE_WIDE_PATH, script);
        }
        // Only touch the status when a configuration exists or was just written above: the config service
        // throws for a status change on a missing configuration, and before the initialization task seeds
        // the default script there's nothing to toggle. The preference mirror below still records the
        // intent.
        if (script != null || current != null) {
            if (enabled) {
                _configService.enable(login, "", DicomEdit.ToolName, SITE_WIDE_PATH);
            } else {
                _configService.disable(login, "", DicomEdit.ToolName, SITE_WIDE_PATH);
            }
        } else {
            log.debug("No site-wide {} configuration exists and no script was provided: skipping the status update and mirroring the preferences only", DicomEdit.ToolName);
        }
        invalidateSitewideAnonCache();

        // Mirror to the site-config preferences last. The config service copy above is the one applied to
        // incoming DICOM; the preference is display/compatibility data. Setting the preferences fires the
        // preference events that dist-events relays to other nodes to invalidate their caches —
        // AnonymizationHandlerMethod handles those events with cache invalidation ONLY and must never write.
        if (script != null) {
            _preferences.setSitewideAnonymizationScript(script);
        }
        _preferences.setEnableSitewideAnonymizationScript(enabled);
    }

    @Override
    public void setSiteWideScript(String login, String script) throws ConfigServiceException {
        setSiteWideSettings(login, script, null);
    }

    @Override
    public void enableSiteWide(String login) throws ConfigServiceException {
        setSiteWideSettings(login, null, true);
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
        setSiteWideSettings(login, null, false);
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

    private static final String DEFAULT_ANON_SCRIPT = "classpath*:META-INF/xnat/defaults/**/id.das";
    private static final String SITE_WIDE_PATH      = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null);
    private static final String SITE_WIDE           = "site-wide";
    private static final String ANON_SCRIPT_CACHE   = DefaultAnonUtils.class.getSimpleName() + "ScriptsCache";

    private static DefaultAnonUtils _instance;

    private final Cache<String, Configuration> _cache;
    private final ConfigService                _configService;
    private final SiteConfigPreferences        _preferences;
}
