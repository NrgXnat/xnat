/*
 * web: org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge.anonymize;

import com.google.common.base.Joiner;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DefaultAnonUtils implements AnonUtils {
    @Autowired
    public DefaultAnonUtils(final ConfigService configService) throws Exception {
        if (_instance != null) {
            throw new Exception("The AnonUtils service is already initialized, try calling getInstance() instead.");
        }
        _instance = this;
        _configService = configService;
    }

    public static AnonUtils getService() {
        if (_instance == null) {
            _instance = XDAT.getContextService().getBean(AnonUtils.class);
        }
        return _instance;
    }

    @Override
    public Configuration getSiteWideScriptConfiguration() {
        return getProjectScriptConfiguration(null);
    }

    @Override
    public Configuration getProjectScriptConfiguration(final String projectId) {
        final boolean isSiteWide = StringUtils.isBlank(projectId);
        final String path = isSiteWide ? SITE_WIDE_PATH : DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving script for {}, {}", DicomEdit.ToolName, path, projectId);
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
        final boolean enabled = config.getStatus().equals(Configuration.ENABLED_STRING);
        if (logger.isDebugEnabled()) {
            if (StringUtils.isNotBlank(projectId)) {
                logger.debug("Retrieved status {} for the site-wide anonymization script", enabled);
            } else {
                logger.debug("Retrieved status {} for the anonymization script for project {}", enabled, projectId);
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

        if (logger.isDebugEnabled()) {
            final String identifier = isSiteWide ? "the site" : "project: " + projectId;
            if (scripts == null) {
                logger.debug("Retrieved no scripts for {}, {} for {}", DicomEdit.ToolName, identifier);
            } else if (scripts.size() == 0) {
                logger.debug("Retrieved no scripts for {}, {} for {}", DicomEdit.ToolName, identifier);
            } else {
                logger.debug("Retrieved {} scripts for {}, {} for {}", scripts.size(), DicomEdit.ToolName, identifier);
            }
        }

        return scripts;
    }

    @Override
    public String getProjectScript(final String projectId) throws ConfigServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting {} script for project: {}", DicomEdit.ToolName, projectId);
        }
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        return _configService.getConfigContents(DicomEdit.ToolName, path);
    }

    @Override
    public void setProjectScript(final String login, final String script, final String projectId) throws ConfigServiceException {
        final String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, projectId);
        if (logger.isDebugEnabled()) {
            logger.debug("User {} is setting {} script for project {}", login, DicomEdit.ToolName, projectId);
        }
        if (projectId == null) {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script);
        } else {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script, Scope.Project, projectId);
        }
    }

    @Override
    public String getSiteWideScript() throws ConfigServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting {} site-wide script", DicomEdit.ToolName);
        }
        return _configService.getConfigContents(DicomEdit.ToolName, SITE_WIDE_PATH);
    }

    @Override
    public void setSiteWideScript(String login, String script) throws ConfigServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("User {} is setting {} site-wide script", login, DicomEdit.ToolName);
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

    public static String getDefaultScript() throws IOException {
        final List<Resource> resources = BasicXnatResourceLocator.getResources(DEFAULT_ANON_SCRIPT);
        if (resources.size() == 0) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Didn't find any default anonymization scripts at: " + DEFAULT_ANON_SCRIPT);
        } else if (resources.size() > 1) {
            boolean isFirst = true;
            final StringBuilder duplicates = new StringBuilder();
            for (final Resource resource : resources) {
                if (!isFirst) {
                    duplicates.append(", ");
                } else {
                    isFirst = false;
                }
                duplicates.append(resource.getURI());
            }
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Found more than one \"default\" anonymization script: " + duplicates.toString());
        }
        try (final InputStream input = resources.get(0).getInputStream()) {
            return Joiner.on("\n").join(IOUtils.readLines(input, "UTF-8"));
        }
    }

    /**
     * Adds a cache of site wide anon scripts.  This is currently used by GradualDicomImporter.
     *
     * @return The site anonymization script cache.
     */
    public static Cache getSiteAnonCache() {
        synchronized (cacheManager) {
            if (!cacheManager.cacheExists(ANON_SCRIPT_CACHE)) {
                final CacheConfiguration config = new CacheConfiguration(ANON_SCRIPT_CACHE, 0)
                        .copyOnRead(false).copyOnWrite(false)
                        .eternal(false)
                        .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                        .timeToLiveSeconds(ANON_CACHE_EXPIRY_SECONDS)
                        .maxEntriesLocalHeap(MAX_ENTRIES_LOCAL_HEAP);
                final Cache cache = new Cache(config);
                cacheManager.addCache(cache);
                return cache;
            } else {
                return cacheManager.getCache(ANON_SCRIPT_CACHE);
            }
        }
    }

    public static void invalidateSitewideAnonCache() {
        getSiteAnonCache().removeAndReturnElement(SITE_WIDE);
    }

    public static Configuration getCachedSitewideAnon() throws Exception {
        final Cache anonCache = getSiteAnonCache();

        Element cached = anonCache.get(SITE_WIDE);
        if (null != cached) {
            return (Configuration) cached.getObjectValue();
        } else {
            Configuration c = getService().getSiteWideScriptConfiguration();
            anonCache.put(new Element(SITE_WIDE, c));
            return c;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AnonUtils.class);

    private static final String DEFAULT_ANON_SCRIPT       = "classpath*:META-INF/xnat/defaults/**/id.das";
    private static final String SITE_WIDE_PATH            = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null);
    private static final String SITE_WIDE                 = "site-wide";
    private static final String ANON_SCRIPT_CACHE         = "scripts-anon";
    private static final long   ANON_CACHE_EXPIRY_SECONDS = 120;
    private static final int    MAX_ENTRIES_LOCAL_HEAP    = 5000;

    private static final CacheManager cacheManager              = CacheManager.getInstance();

    private static AnonUtils _instance;

    private final ConfigService         _configService;
}
