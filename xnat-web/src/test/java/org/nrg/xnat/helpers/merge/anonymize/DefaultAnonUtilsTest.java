/*
 * web: org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge.anonymize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import javax.cache.Cache;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.config.entities.Configuration.DISABLED_STRING;
import static org.nrg.config.entities.Configuration.ENABLED_STRING;
import static org.nrg.xnat.test.AnonUtilsTestSupport.resetAnonUtilsSingleton;

/**
 * Tests for the canonical site-wide anonymization write path, {@link DefaultAnonUtils#setSiteWideSettings}.
 * The scenarios named for tickets are regression tests for historical bugs:
 * <ul>
 * <li>XNAT-5558: editing the script while disabled must not (visibly or invisibly) enable it.</li>
 * <li>XNAT-5830: enabling and editing in one operation must end enabled with the new script, written
 *     exactly once — historically these were two racing async events.</li>
 * </ul>
 */
public class DefaultAnonUtilsTest {
    private static final String USER      = "kate";
    private static final String TOOL      = "anon";
    private static final String PATH      = "script";
    private static final String CACHE_KEY = "site-wide";

    private ConfigService                _configService;
    private SiteConfigPreferences        _preferences;
    private Cache<String, Configuration> _cache;
    private DefaultAnonUtils             _anonUtils;

    @Before
    public void setUp() throws Exception {
        resetAnonUtilsSingleton();
        _configService = mock(ConfigService.class);
        _preferences = mock(SiteConfigPreferences.class);
        final JCacheHelper cacheHelper = mock(JCacheHelper.class);
        //noinspection unchecked
        _cache = (Cache<String, Configuration>) mock(Cache.class);
        doReturn(_cache).when(cacheHelper).getCache(anyString(), eq(String.class), eq(Configuration.class));
        _anonUtils = new DefaultAnonUtils(_configService, _preferences, cacheHelper);
    }

    @After
    public void tearDown() throws Exception {
        resetAnonUtilsSingleton();
    }

    @Test
    public void editingScriptWhileDisabledLeavesItDisabled() throws Exception {
        // XNAT-5558 scenario: the script write must preserve the disabled status explicitly.
        givenCurrentConfig("old script", DISABLED_STRING);

        _anonUtils.setSiteWideScript(USER, "new script");

        // Config service written first — script before status — then the preference mirror.
        final InOrder order = inOrder(_configService, _preferences);
        order.verify(_configService).replaceConfig(USER, "", TOOL, PATH, "new script");
        order.verify(_configService).disable(USER, "", TOOL, PATH);
        order.verify(_preferences).setSitewideAnonymizationScript("new script");
        order.verify(_preferences).setEnableSitewideAnonymizationScript(false);
        verify(_configService, never()).enable(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void enablingAndEditingTogetherEndsEnabledWithNewScript() throws Exception {
        // XNAT-5830 scenario: one logical operation, one content write, correct final status.
        givenCurrentConfig("old script", DISABLED_STRING);

        _anonUtils.setSiteWideSettings(USER, "new script", true);

        verify(_configService, times(1)).replaceConfig(USER, "", TOOL, PATH, "new script");
        final InOrder order = inOrder(_configService);
        order.verify(_configService).replaceConfig(USER, "", TOOL, PATH, "new script");
        order.verify(_configService).enable(USER, "", TOOL, PATH);
        verify(_configService, never()).disable(anyString(), anyString(), anyString(), anyString());
        verify(_preferences).setSitewideAnonymizationScript("new script");
        verify(_preferences).setEnableSitewideAnonymizationScript(true);
    }

    @Test
    public void resavingIdenticalContentCreatesNoNewRevision() throws Exception {
        givenCurrentConfig("same script", ENABLED_STRING);

        _anonUtils.setSiteWideScript(USER, "same script");

        verify(_configService, never()).replaceConfig(anyString(), anyString(), anyString(), anyString(), anyString());
        // The mirror is still synced even when the config write is skipped.
        verify(_preferences).setSitewideAnonymizationScript("same script");
        verify(_preferences).setEnableSitewideAnonymizationScript(true);
    }

    @Test
    public void enableToggleAloneNeverRewritesScript() throws Exception {
        givenCurrentConfig("old script", DISABLED_STRING);

        _anonUtils.setSiteWideSettings(USER, null, true);

        verify(_configService, never()).replaceConfig(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(_preferences, never()).setSitewideAnonymizationScript(anyString());
        verify(_configService).enable(USER, "", TOOL, PATH);
        verify(_preferences).setEnableSitewideAnonymizationScript(true);
    }

    @Test
    public void firstEverScriptDefaultsToEnabled() throws Exception {
        when(_configService.getConfig(TOOL, PATH)).thenReturn(null);

        _anonUtils.setSiteWideScript(USER, "first script");

        verify(_configService).replaceConfig(USER, "", TOOL, PATH, "first script");
        verify(_configService).enable(USER, "", TOOL, PATH);
        verify(_preferences).setEnableSitewideAnonymizationScript(true);
    }

    @Test
    public void enableToggleWithoutExistingConfigSkipsStatusWrite() throws Exception {
        when(_configService.getConfig(TOOL, PATH)).thenReturn(null);

        _anonUtils.setSiteWideSettings(USER, null, true);

        // Nothing exists in the config service to toggle (a status change on a missing configuration
        // throws), but the preference still records the intent.
        verify(_configService, never()).replaceConfig(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(_configService, never()).enable(anyString(), anyString(), anyString(), anyString());
        verify(_configService, never()).disable(anyString(), anyString(), anyString(), anyString());
        verify(_preferences).setEnableSitewideAnonymizationScript(true);
    }

    @Test
    public void writesInvalidateTheLocalScriptCache() throws Exception {
        givenCurrentConfig("old script", ENABLED_STRING);

        _anonUtils.setSiteWideScript(USER, "new script");

        verify(_cache, atLeastOnce()).remove(CACHE_KEY);
    }

    private void givenCurrentConfig(final String contents, final String status) {
        when(_configService.getConfig(TOOL, PATH)).thenReturn(configuration(contents, status));
    }

    private static Configuration configuration(final String contents, final String status) {
        final Configuration configuration = new Configuration();
        configuration.setConfigData(new ConfigurationData(contents));
        configuration.setStatus(status);
        return configuration;
    }

}
