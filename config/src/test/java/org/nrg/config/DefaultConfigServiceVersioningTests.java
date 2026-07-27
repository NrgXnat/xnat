/*
 * config: org.nrg.config.DefaultConfigServiceVersioningTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.configuration.NrgConfigTestConfiguration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.util.TestDBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Regression tests for configuration version-number assignment. Historically the next version was computed
 * as the version of the most recent configuration BY CREATED DATE plus one, with no serialization of
 * writers. That duplicated version numbers under concurrent writes (see XNAT-5830) and computed the wrong
 * next version whenever the newest row by date didn't carry the highest version number. The fixed
 * implementation serializes writers with a transaction-scoped advisory lock and computes MAX(version) + 1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
public class DefaultConfigServiceVersioningTests {
    private static final String TOOL = "versioningTool";
    private static final String PATH = "/versioning/path";
    private static final String USER = "versioningUser";

    @Autowired
    public void setConfigService(final ConfigService configService) {
        _configService = configService;
    }

    @Autowired
    public void setTestDBUtils(final TestDBUtils testDBUtils) {
        _testDBUtils = testDBUtils;
    }

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate template) {
        _template = template;
    }

    @Before
    public void setup() {
        _testDBUtils.cleanDb();
    }

    @Test
    public void assignsSequentialVersionsToSequentialWrites() throws ConfigServiceException {
        _configService.replaceConfig(USER, "first", TOOL, PATH, "contents one");
        _configService.replaceConfig(USER, "second", TOOL, PATH, "contents two");
        _configService.replaceConfig(USER, "third", TOOL, PATH, "contents three");
        assertThat(versions()).containsExactly(1, 2, 3);
    }

    @Test
    public void computesNextVersionFromMaxVersionNotNewestRow() throws ConfigServiceException {
        _configService.replaceConfig(USER, "first", TOOL, PATH, "contents one");   // version 1
        _configService.replaceConfig(USER, "second", TOOL, PATH, "contents two");  // version 2, newest by created
        // Simulate the numbering left behind by the historical race: an OLDER row carrying a HIGHER version
        // than the newest-by-created row.
        _template.update("UPDATE xhbm_configuration SET version = 5 WHERE tool = ? AND path = ? AND version = 1", TOOL, PATH);
        _configService.replaceConfig(USER, "third", TOOL, PATH, "contents three");
        // The broken algorithm computed <newest row by created>.version + 1 = 3. Fixed: MAX(version) + 1 = 6.
        assertThat(versions()).containsExactly(2, 5, 6);
    }

    @Test
    public void unversionedConfigsUpdateInPlaceWithVersionOne() throws ConfigServiceException {
        _configService.replaceConfig(USER, "first", TOOL, PATH, true, "contents one");
        _configService.replaceConfig(USER, "second", TOOL, PATH, true, "contents two");
        assertThat(versions()).containsExactly(1);
        assertThat(_configService.getConfig(TOOL, PATH).getContents()).isEqualTo("contents two");
    }

    private List<Integer> versions() {
        return _template.queryForList("SELECT version FROM xhbm_configuration WHERE tool = ? AND path = ? ORDER BY version", Integer.class, TOOL, PATH);
    }

    private ConfigService _configService;
    private TestDBUtils   _testDBUtils;
    private JdbcTemplate  _template;
}
