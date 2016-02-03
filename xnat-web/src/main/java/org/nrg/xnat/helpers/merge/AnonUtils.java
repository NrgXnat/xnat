/*
 * org.nrg.xnat.helpers.merge.AnonUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/27/14 11:54 AM
 */
package org.nrg.xnat.helpers.merge;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;

import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
public class AnonUtils {
	public AnonUtils() throws Exception {
        if (_instance != null) {
            throw new Exception("The AnonUtils service is already initialized, try calling getInstance() instead.");
        }
        _instance = this;
    }
	
	public static AnonUtils getService() {
	    if (_instance == null) {
	    	_instance = XDAT.getContextService().getBean(AnonUtils.class);
	    }
	    return _instance;
	}
	
	public Configuration getScript(String path, Long project) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving script for {}, {} for project: {}", DicomEdit.ToolName, path, project);
        }

        return project == null
                ? _configService.getConfig(DicomEdit.ToolName, path)
                : _configService.getConfig(DicomEdit.ToolName, path, Scope.Project, project.toString());
	}
	
	public boolean isEnabled(String path, Long project) {
        final Configuration config = project == null
                ? _configService.getConfig(DicomEdit.ToolName, path)
                : _configService.getConfig(DicomEdit.ToolName, path, Scope.Project, project.toString());

        final boolean enabled = config.getStatus().equals(Configuration.ENABLED_STRING);
        if (logger.isDebugEnabled()) {
            if (project == null) {
                logger.debug("Retrieved status {} for {}, {} for the site", enabled, DicomEdit.ToolName, path);
            } else {
                logger.debug("Retrieved status {} for {}, {} for project: {}", enabled, DicomEdit.ToolName, path, project);
            }
        }
        return enabled;
	}
	
	public List<Configuration> getAllScripts (Long project) {
        final List<Configuration> scripts = project == null
                ? _configService.getConfigsByTool(DicomEdit.ToolName)
                : _configService.getConfigsByTool(DicomEdit.ToolName, Scope.Project, project.toString());

        if (logger.isDebugEnabled()) {
            final String identifier = project == null ? "the site" : "project: " + project.toString();
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
	
	public void setProjectScript (String login, String path, String script, Long project) throws ConfigServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting script for {}, {} for project: {}", DicomEdit.ToolName, path, project);
        }
        if (project == null) {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script);
        } else {
            _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script, Scope.Project, project.toString());
        }
	}
	
	public void setSiteWideScript(String login, String path, String script) throws ConfigServiceException {
        _configService.replaceConfig(login, "", DicomEdit.ToolName, path, script);
        AnonUtils.invalidateSitewideAnonCache();
	}
	
	public void enableSiteWide (String login, String path ) throws ConfigServiceException {
        _configService.enable(login, "", DicomEdit.ToolName, path);
        AnonUtils.invalidateSitewideAnonCache();
	}
	
	public void enableProjectSpecific(String login, String path, Long project) throws ConfigServiceException {
        if (project == null) {
            _configService.enable(login, "", DicomEdit.ToolName, path);
        } else {
            _configService.enable(login, "", DicomEdit.ToolName, path, Scope.Project, project.toString());
        }
	}
	
	public void disableSiteWide(String login, String path) throws ConfigServiceException {
        _configService.disable(login, "", DicomEdit.ToolName, path);
        AnonUtils.invalidateSitewideAnonCache();
	}
	
	public void disableProjectSpecific(String login, String path, Long project) throws ConfigServiceException {
        if (project == null) {
            _configService.disable(login, "", DicomEdit.ToolName, path);
        } else {
            _configService.disable(login, "", DicomEdit.ToolName, path, Scope.Project, project.toString());
        }
	}
	
	public static File getDefaultScript () throws FileNotFoundException {
		final File def = new File (XFT.GetConfDir(), DEFAULT_ANON_SCRIPT);
		if (def.exists()) {
			return def;
		}
		else {
			throw new FileNotFoundException("Default anon script: " + DEFAULT_ANON_SCRIPT + " not found in " + XFT.GetConfDir());
		}
	}


	/**
	 * Adds a cache of site wide anon scripts.  This is currently used by GradualDicomImporter.
	 * @return The site anonymization script cache.
	 */
	public static Cache getSiteAnonCache() {
        synchronized (cacheManager) {
            if (!cacheManager.cacheExists(cacheName)) {
                final CacheConfiguration config = new CacheConfiguration(cacheName, 0)
                .copyOnRead(false).copyOnWrite(false)
                .eternal(false)
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                .timeToLiveSeconds(ANON_CACHE_EXPIRY_SECONDS)
                .maxEntriesLocalHeap(MAX_ENTRIES_LOCAL_HEAP);
                final Cache cache = new Cache(config);
                cacheManager.addCache(cache);
                return cache;
            } else {
                return cacheManager.getCache(cacheName);
            }
        }
    }
	
	public static void invalidateSitewideAnonCache(){
		getSiteAnonCache().removeAndReturnElement(SITE_WIDE);
	}
    
    public static Configuration getCachedSitewideAnon() throws Exception {
        final Cache anonCache=getSiteAnonCache();
        
        Element cached= anonCache.get(SITE_WIDE);
        if(null!=cached){
        	return (Configuration)cached.getObjectValue();
        }else{
            Configuration c = AnonUtils.getService().getScript(path, null);
            anonCache.put(new Element(SITE_WIDE, c));
            return c;
        }
    }

    private static String path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null);
    private static final String cacheName = "scripts-anon";
    
    private static final CacheManager cacheManager = CacheManager.getInstance();
    private static final String SITE_WIDE = "site-wide";
    private static final long ANON_CACHE_EXPIRY_SECONDS = 120;
    private static final int MAX_ENTRIES_LOCAL_HEAP = 5000;

    private static final Logger logger = LoggerFactory.getLogger(AnonUtils.class);

    private static final String DEFAULT_ANON_SCRIPT = "id.das";

    private static AnonUtils _instance;

    @Inject
    private ConfigService _configService;
}
