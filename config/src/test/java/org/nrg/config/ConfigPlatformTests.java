/*
 * config: org.nrg.config.ConfigPlatformTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.configuration.NrgConfigTestConfiguration;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.extensions.postChange.Separatepetmr.Bool.PETMRSettingChange;
import org.nrg.config.services.ConfigService;
import org.nrg.config.util.TestDBUtils;
import org.nrg.framework.constants.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.nrg.config.entities.Configuration.DISABLED_STRING;
import static org.nrg.config.entities.Configuration.ENABLED_STRING;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Rollback
public class ConfigPlatformTests {
    private static final String CONTENTS                  = "yellow=#FFFF00";
    private static final String NEW_CONTENTS              = "NEW CONTENTS";
    private static final String PATH                      = "/path/to/config";
    private static final String PROJECT                   = "project1";
    private static final String TOOL_NAME                 = "coloringTool";
    private static final String LISTENER_TOOL             = "separatePetMr";
    private static final String LISTENER_PATH             = "Bool";
    private static final String USERNAME                  = "admin";
    private static final String REASON_CREATED            = "created";
    private static final String REASON_REPLACED           = "bug so i replaced it";
    private static final String REASON_STATUS             = "Updating status";
    public static final  String NOT_THAT_PROJECT          = "NOT_THAT_PROJECT";
    public static final  String SOME_OTHER_TOOL           = "SOME_OTHER_TOOL";
    public static final  String NEW_PATH                  = "newPath";
    public static final  String ANOTHER_PATH              = "anotherPath";
    public static final  String FRANK                     = "Frank";
    public static final  String BILL                      = "Bill";
    public static final  String SOME_STUFF                = "Some Stuff";
    public static final  String OTHER_STUFF               = "Other Stuff";
    public static final  String CHANGE                    = "change";
    public static final  String DIFFERENT_TOOL            = "Different Tool";
    public static final  String NULLING_CONFIG            = "nulling config";
    public static final  String TEST_CONFIG               = "testConfigurationFile.txt";
    public static final  String SOP_1                     = "SOP";
    public static final  String SOP_2                     = "SOP2";
    public static final  String REASON_DISABLING          = "disabling";
    public static final  String REASON_ENABLING           = "enabling";
    public static final  String PROJECT_CHANGE            = "projectChange";
    public static final  String CONTENTS_V1               = "version1";
    public static final  String CONTENTS_V2               = "version2";
    public static final  String CONTENTS_V3               = "version3";
    public static final  String CONTENTS_V4               = "version4";
    public static final  String CONTENTS_TESTING_VERSIONS = "testingVersions";

    @Autowired
    public void setTestDBUtils(final TestDBUtils testDBUtils) {
        _testDBUtils = testDBUtils;
    }

    @Autowired
    public void setConfigService(final ConfigService configService) {
        _configService = configService;
    }

    @Before
    public void setup() {
        _testDBUtils.cleanDb();
    }

    @Test
    public void testGetAll() throws ConfigServiceException {
        final List<String> paths = Stream.of(PATH, NEW_PATH, ANOTHER_PATH).sorted().collect(Collectors.toList());

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.getFirst(), CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.get(1), CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.get(2), CONTENTS);

        final List<Configuration> list = _configService.getAll();

        assertThat(list).isNotNull()
                        .hasSize(3)
                        .extracting(Configuration::getPath)
                        .allMatch(paths::contains);
    }

    @Test
    public void testConfigInsert() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        Configuration config = _configService.getConfig(TOOL_NAME, PATH);
        assertThat(config).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS);
    }

    @Test
    public void testGetConfigContents() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        assertThat(_configService.getConfigContents(TOOL_NAME, PATH)).isNotNull().isNotBlank().isEqualTo(CONTENTS);
    }

    @Test
    public void testGetTools() throws ConfigServiceException {
        final List<String> tools = Stream.of(PATH, FRANK, BILL).sorted().collect(Collectors.toList());

        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.getFirst(), PATH, CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(1), PATH, CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(2), PATH, CONTENTS);

        final List<String> list = _configService.getTools();
        Collections.sort(list);

        assertThat(list).isNotNull().isNotEmpty().hasSize(3).containsExactlyInAnyOrderElementsOf(tools);
    }

    @Test
    public void testGetToolsByProject() throws ConfigServiceException {
        final List<String> tools = Stream.of(TOOL_NAME, FRANK).sorted().collect(Collectors.toList());

        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.getFirst(), PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(1), PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<String> list = _configService.getTools(Scope.Project, PROJECT);
        Collections.sort(list);

        assertThat(list).isNotNull().isNotEmpty().hasSize(2).containsExactlyInAnyOrderElementsOf(tools);
    }

    @Test
    public void testGetConfigsByTool() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<Configuration> list = _configService.getConfigsByTool(TOOL_NAME);
        assertThat(list).isNotNull().isNotEmpty().hasSize(1);

        final Configuration configuration = list.getFirst();

        assertThat(configuration).isNotNull().hasFieldOrPropertyWithValue("tool", TOOL_NAME);

        //now test by project
        final List<Configuration> franksNotProjectList = _configService.getConfigsByTool(FRANK, Scope.Project, NOT_THAT_PROJECT);
        assertThat(franksNotProjectList).isNotNull().isEmpty();

        final List<Configuration> billsNotProjectList = _configService.getConfigsByTool(BILL, Scope.Project, NOT_THAT_PROJECT);
        final Configuration       billsConfiguration  = billsNotProjectList.getFirst();
        assertThat(billsConfiguration).isNotNull().hasFieldOrPropertyWithValue("tool", BILL);
    }

    @Test
    public void testGetProjects() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<String> allProjects = _configService.getProjects();
        assertThat(allProjects).isNotNull().isNotEmpty().hasSize(2).containsExactlyInAnyOrder(PROJECT, NOT_THAT_PROJECT);

        final List<String> toolProjects = _configService.getProjects(TOOL_NAME);
        assertThat(toolProjects).isNotNull().isNotEmpty().hasSize(1).containsExactly(PROJECT);
    }

    @Test
    public void testGetStatus() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        String status = _configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(status).isNotNull().isNotBlank().isEqualTo(ENABLED_STRING);
    }

    @Test
    public void testReplaceConfig() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        Configuration config1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(config1).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS);

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, NEW_CONTENTS, Scope.Project, PROJECT);

        Configuration config2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(config2).isNotNull().hasFieldOrPropertyWithValue("contents", NEW_CONTENTS);
    }

    /**
     * Make sure a config doesn't get truncated, corrupted or do anything silly to the database. fill that file with whatever you think might cause problems.
     */
    @Test
    public void testRealConfigFile() throws ConfigServiceException {
        final StringWriter writer = new StringWriter();
        try (final InputStream inputStream = getClass().getResourceAsStream(TEST_CONFIG)) {
            assert inputStream != null;
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Unable to open testConfigurationFile.txt from the org.nrg.config classpath.");
        }

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, SOP_1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, SOME_OTHER_TOOL, PATH, CONTENTS, Scope.Project, SOP_2);

        final String configFile = writer.toString();
        assertThat(configFile).isNotNull().isNotBlank();

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, configFile, Scope.Project, PROJECT);

        final String newContents = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents();
        assertThat(newContents).isNotNull().isNotBlank().hasSize(configFile.length()).isEqualTo(configFile);

        Configuration config = _configService.getConfig(TOOL_NAME, PATH);
        assertThat(config).isNull();
    }

    /**
     * Make sure the app lets you know if you create a file that would be truncated.
     */
    @Test
    public void testLargeConfigFile() {
        final String configFile = new String(new char[ConfigService.MAX_FILE_LENGTH + 1]);

        try {
            _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, configFile, Scope.Project, PROJECT);
        } catch (ConfigServiceException e) {
            Configuration config = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
            assertThat(config).isNull();
            return;
        }
        fail("There was supposed to be a ConfigServiceException causing the save to fail but for some reason I was able to retrieve the config.");
    }

    @Test
    public void testStatus() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, SOP_1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, SOME_OTHER_TOOL, PATH, CONTENTS, Scope.Project, SOP_2);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        final String first = _configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(first).isNotNull().isNotBlank().isEqualTo(ENABLED_STRING);

        _configService.disable(USERNAME, REASON_CREATED, TOOL_NAME, PATH, Scope.Project, PROJECT);

        final String second = _configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(second).isNotNull().isNotBlank().isEqualTo(DISABLED_STRING);

        final String status = _configService.getStatus(TOOL_NAME, PATH);
        assertThat(status).isNull();
    }

    @Test
    public void testHistory() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, SOP_1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, SOME_OTHER_TOOL, PATH, CONTENTS, Scope.Project, SOP_2);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_REPLACED, TOOL_NAME, PATH, CONTENTS + SOME_STUFF, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_REPLACED, TOOL_NAME, PATH, CONTENTS + OTHER_STUFF, Scope.Project, PROJECT);

        final List<Configuration> list = _configService.getHistory(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(list).isNotNull().isNotEmpty().hasSize(3);
        assertThat(list.get(1)).isNotNull().hasFieldOrPropertyWithValue("reason", REASON_REPLACED);
    }

    @Test
    public void testDuplicateConfigurations() throws ConfigServiceException {
        _testDBUtils.cleanDb();

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        //we should wind up with 3 configurations, 1 config file
        final List<Configuration> list = _configService.getHistory(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(list).isNotNull().isNotEmpty().hasSize(3);
        assertThat(_testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT)).isEqualTo(1);  //FYI:  This will fail when the table name changes.

        //Make sure enabling and disabling does not create a new script each time. 
        //Also, make sure it adds the appropriate # of rows to XHBM_CONFIGURATION
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);

        //1 config file, 9 configurations
        assertThat(_testDBUtils.countConfigurationRows(TOOL_NAME, PATH, Scope.Project, PROJECT)).isEqualTo(9);  //FYI:  This will fail when the table name changes.
        assertThat(_testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT)).isEqualTo(1);  //FYI:  This will fail when the table name changes.

        //change the contents and assure there are 2 rows in config data
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS + CHANGE, Scope.Project, PROJECT);
        assertThat(_testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT)).isEqualTo(2);  //FYI:  This will fail when the table name changes.

        //add the same configuration to a different tool. 
        //configs aren't shared between tools, so we should end up with three rows in config data.
        _configService.replaceConfig(USERNAME, REASON_CREATED, DIFFERENT_TOOL, PATH, CONTENTS, Scope.Project, PROJECT);
        assertThat(_testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT)).isEqualTo(2);  //FYI:  This will fail when the table name changes.
        assertThat(_testDBUtils.countConfigurationDataRows()).isEqualTo(3);  //FYI:  This will fail when the table name changes.
    }

    @Test
    public void testNullContents() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        Configuration config1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(config1).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS);

        _configService.replaceConfig(USERNAME, NULLING_CONFIG, TOOL_NAME, PATH, null, Scope.Project, PROJECT);
        assertThat(_testDBUtils.countConfigurationDataRows()).isEqualTo(2);
        assertThat(_testDBUtils.countConfigurationRows()).isEqualTo(2);
        Configuration config2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(config2).isNotNull().hasFieldOrPropertyWithValue("contents", null);
    }

    @Test
    public void testDisablingAndEnablingConfig() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        assertThat(_configService.getConfig(TOOL_NAME, PATH)).isNotNull().hasFieldOrPropertyWithValue("status", ENABLED_STRING);
        _configService.disable(USERNAME, REASON_DISABLING, TOOL_NAME, PATH);
        assertThat(_configService.getConfig(TOOL_NAME, PATH)).isNotNull().hasFieldOrPropertyWithValue("status", DISABLED_STRING);
        _configService.enable(USERNAME, REASON_ENABLING, TOOL_NAME, PATH);

        Configuration siteConfig = _configService.getConfig(TOOL_NAME, PATH);
        assertThat(siteConfig).isNotNull().hasFieldOrPropertyWithValue("status", ENABLED_STRING).hasFieldOrPropertyWithValue("contents", CONTENTS);

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        assertThat(_configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT)).isNotNull().hasFieldOrPropertyWithValue("status", ENABLED_STRING);
        _configService.disable(USERNAME, REASON_DISABLING, TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(_configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT)).isNotNull().hasFieldOrPropertyWithValue("status", DISABLED_STRING);
        _configService.enable(USERNAME, REASON_ENABLING, TOOL_NAME, PATH, Scope.Project, PROJECT);

        Configuration prjConfig = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(prjConfig).isNotNull().hasFieldOrPropertyWithValue("status", ENABLED_STRING).hasFieldOrPropertyWithValue("contents", CONTENTS);
    }

    @Test
    public void testNullPath() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, null, CONTENTS, Scope.Project, PROJECT);
        assertThat(_configService.getConfig(TOOL_NAME, null, Scope.Project, PROJECT)).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS);
        assertThat(_configService.getConfig(TOOL_NAME, null)).isNull();
    }

    @Test
    public void testNullPathAndTool() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, null);
        assertThat(_configService.getConfig(null, null)).isNotNull().hasFieldOrPropertyWithValue("contents", null);

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS);
        assertThat(_configService.getConfig(null, null)).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS);

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS + CHANGE);
        assertThat(_configService.getConfig(null, null)).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS + CHANGE);

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS + PROJECT_CHANGE, Scope.Project, PROJECT);
        assertThat(_configService.getConfig(null, null, Scope.Project, PROJECT)).isNotNull().hasFieldOrPropertyWithValue("contents", CONTENTS + PROJECT_CHANGE);
    }

    @Test
    public void testGetById() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V1, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v3 = _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V3, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V4, Scope.Project, PROJECT);

        //really, if they both have the same ID, you had to pull the correct one.
        assertThat(_configService.getConfigById(TOOL_NAME, PATH, Long.toString(v3.getId()), Scope.Project, PROJECT)).isNotNull().hasFieldOrPropertyWithValue("id", v3.getId());

        //let's try to pull one from a different project but the same ID. just to make sure this method isn't behaving
        //like the unchecked .getById(Long id) because that would be a problem.
        final Configuration otherProj = _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_TESTING_VERSIONS, Scope.Project, SOP_1);
        assertThat(_configService.getConfigById(TOOL_NAME, PATH, Long.toString(v3.getId()), Scope.Project, PROJECT)).isNotNull().hasFieldOrProperty("id").extracting(Configuration::getId).isNotEqualTo(otherProj.getId());
        assertThat(_configService.getConfigById(TOOL_NAME, PATH, Long.toString(v3.getId()), Scope.Project, PROJECT)).isNotNull().hasFieldOrProperty("id").extracting(Configuration::getId).isNotEqualTo(otherProj.getId());

        final Configuration another = _configService.getById(otherProj.getId());
        assertThat(another).isNotNull().isEqualTo(otherProj);
    }

    @Test
    public void testVersionNumbers() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V1, Scope.Project, PROJECT);
        final Configuration v1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(v1).isNotNull().hasFieldOrPropertyWithValue("version", 1).hasFieldOrPropertyWithValue("contents", CONTENTS_V1);

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(v2).isNotNull().hasFieldOrPropertyWithValue("version", 2).hasFieldOrPropertyWithValue("contents", CONTENTS_V2);

        final Configuration v2Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 2, Scope.Project, PROJECT);
        assertThat(v2Check).isNotNull().hasFieldOrPropertyWithValue("version", v2.getVersion()).hasFieldOrPropertyWithValue("contents", v2.getContents());

        final Configuration v1Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 1, Scope.Project, PROJECT);
        assertThat(v1Check).isNotNull().hasFieldOrPropertyWithValue("version", v1.getVersion()).hasFieldOrPropertyWithValue("contents", v1.getContents());
    }

    @Test
    @Ignore("This test broke with the upgrade to Hibernate and caching for unclear reasons")
    public void testUnversionedConfig() throws ConfigServiceException {
        // First call defines this as unversioned.
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, true, CONTENTS_V1, Scope.Project, PROJECT);
        final Configuration v1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(v1).isNotNull().hasFieldOrPropertyWithValue("version", 1).hasFieldOrPropertyWithValue("contents", CONTENTS_V1);

        // Second call it should remain unversioned.
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertThat(v2).isNotNull().hasFieldOrPropertyWithValue("version", 1).hasFieldOrPropertyWithValue("contents", CONTENTS_V2);

        final Configuration v2Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 2, Scope.Project, PROJECT);
        assertThat(v2Check).isNull();

        final Configuration v1Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 1, Scope.Project, PROJECT);
        assertThat(v1Check).isNotNull().hasFieldOrPropertyWithValue("version", v1.getVersion()).hasFieldOrPropertyWithValue("version", v2.getVersion()).hasFieldOrPropertyWithValue("contents", v2.getContents());
    }

    @Test
    public void testPostChangeListener() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, LISTENER_TOOL, LISTENER_PATH, Boolean.TRUE.toString());
        final Configuration separatePetMr = _configService.getConfig(LISTENER_TOOL, LISTENER_PATH);
        assertThat(separatePetMr).isNotNull();
        assertThat(PETMRSettingChange.getChanges()).isEqualTo(1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, LISTENER_TOOL, LISTENER_PATH, Boolean.FALSE.toString());
        assertThat(PETMRSettingChange.getChanges()).isEqualTo(2);
    }

    private TestDBUtils   _testDBUtils;
    private ConfigService _configService;
}
