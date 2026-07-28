/*
 * web: org.nrg.xnat.event.listeners.methods.AnonymizationHandlerMethodTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;

import javax.cache.Cache;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.xnat.test.AnonUtilsTestSupport.resetAnonUtilsSingleton;

/**
 * The handler must be cache-coherence only: the canonical write path (AnonUtils.setSiteWideSettings) writes
 * both stores synchronously, and on peer nodes this handler — triggered by the dist-events preference
 * relay — must do nothing but invalidate the local script cache. A handler that writes reintroduces the
 * XNAT-5830 race and the duplicate-revision behavior on multi-node deployments.
 */
public class AnonymizationHandlerMethodTest {
    private static final String CACHE_KEY = "site-wide";

    private AnonUtils                    _anonUtils;
    private SiteConfigPreferences        _preferences;
    private Cache<String, Configuration> _cache;
    private AnonymizationHandlerMethod   _handler;

    @Before
    public void setUp() throws Exception {
        resetAnonUtilsSingleton();

        // Construct a real DefaultAnonUtils with mocks so the static invalidateSitewideAnonCache() the
        // handler calls has an instance (and a cache mock) to operate on.
        final JCacheHelper cacheHelper = mock(JCacheHelper.class);
        //noinspection unchecked
        _cache = (Cache<String, Configuration>) mock(Cache.class);
        doReturn(_cache).when(cacheHelper).getCache(anyString(), eq(String.class), eq(Configuration.class));
        new DefaultAnonUtils(mock(ConfigService.class), mock(SiteConfigPreferences.class), cacheHelper);

        _anonUtils = mock(AnonUtils.class);
        _preferences = mock(SiteConfigPreferences.class);

        // Keep the two stores consistent so the drift alarm stays quiet.
        final Configuration configuration = new Configuration();
        configuration.setConfigData(new ConfigurationData("script"));
        configuration.setStatus(Configuration.ENABLED_STRING);
        when(_anonUtils.getSiteWideScriptConfiguration()).thenReturn(configuration);
        when(_preferences.getSitewideAnonymizationScript()).thenReturn("script");

        _handler = new AnonymizationHandlerMethod(_preferences, _anonUtils);
    }

    @After
    public void tearDown() throws Exception {
        resetAnonUtilsSingleton();
    }

    @Test
    public void handlesBothAnonymizationPreferences() {
        assertThat(_handler.getHandledPreferences()).containsExactlyInAnyOrder("sitewideAnonymizationScript", "enableSitewideAnonymizationScript");
    }

    @Test
    public void scriptPreferenceEventOnlyInvalidatesCache() throws Exception {
        _handler.handlePreference("sitewideAnonymizationScript", "some new script");
        assertCacheInvalidatedAndNothingWritten();
    }

    @Test
    public void enablePreferenceEventOnlyInvalidatesCache() throws Exception {
        _handler.handlePreference("enableSitewideAnonymizationScript", "true");
        assertCacheInvalidatedAndNothingWritten();
    }

    private void assertCacheInvalidatedAndNothingWritten() throws Exception {
        verify(_cache).remove(CACHE_KEY);
        verify(_anonUtils, never()).setSiteWideScript(anyString(), anyString());
        verify(_anonUtils, never()).setSiteWideSettings(anyString(), any(), any());
        verify(_anonUtils, never()).enableSiteWide(anyString());
        verify(_anonUtils, never()).disableSiteWide(anyString());
    }

}
