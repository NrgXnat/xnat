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

import static org.junit.Assert.*;
import static org.nrg.config.entities.Configuration.DISABLED_STRING;
import static org.nrg.config.entities.Configuration.ENABLED_STRING;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.get(0), CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.get(1), CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, paths.get(2), CONTENTS);

        final List<Configuration> list = _configService.getAll();

        assertEquals(list.size(), 3);
        assertArrayEquals(paths.toArray(), list.stream().map(Configuration::getPath).sorted().toArray());
    }

    @Test
    public void testConfigInsert() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        assertEquals(CONTENTS, _configService.getConfig(TOOL_NAME, PATH).getContents());
    }

    @Test
    public void testGetConfigContents() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        assertEquals(CONTENTS, _configService.getConfigContents(TOOL_NAME, PATH));
    }

    @Test
    public void testGetTools() throws ConfigServiceException {
        final List<String> tools = Stream.of(PATH, FRANK, BILL).sorted().collect(Collectors.toList());

        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(0), PATH, CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(1), PATH, CONTENTS);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(2), PATH, CONTENTS);

        final List<String> list = _configService.getTools();
        Collections.sort(list);

        assertEquals(3, list.size());
        assertArrayEquals(tools.toArray(), list.toArray());
    }

    @Test
    public void testGetToolsByProject() throws ConfigServiceException {
        final List<String> tools = Stream.of(TOOL_NAME, FRANK).sorted().collect(Collectors.toList());

        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(0), PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, tools.get(1), PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<String> list = _configService.getTools(Scope.Project, PROJECT);
        Collections.sort(list);

        assertEquals(2, list.size());
        assertArrayEquals(tools.toArray(), list.toArray());
    }

    @Test
    public void testGetConfigsByTool() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<Configuration> list = _configService.getConfigsByTool(TOOL_NAME);
        assertEquals(1, list.size());

        final Configuration configuration = list.get(0);

        assertEquals(TOOL_NAME, configuration.getTool());

        //now test by project
        final List<Configuration> franksNotProjectList = _configService.getConfigsByTool(FRANK, Scope.Project, NOT_THAT_PROJECT);
        assertNull(franksNotProjectList);

        final List<Configuration> billsNotProjectList = _configService.getConfigsByTool(BILL, Scope.Project, NOT_THAT_PROJECT);
        final Configuration       billsConfiguration  = billsNotProjectList.get(0);

        assertEquals(BILL, billsConfiguration.getTool());
    }

    @Test
    public void testGetProjects() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        final List<String> allProjects = _configService.getProjects();
        assertEquals(2, allProjects.size());

        final List<String> toolProjects = _configService.getProjects(TOOL_NAME);
        assertEquals(1, toolProjects.size());
        assertEquals(PROJECT, toolProjects.get(0));
    }

    @Test
    public void testGetStatus() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, FRANK, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, BILL, PATH, CONTENTS, Scope.Project, NOT_THAT_PROJECT);

        assertEquals(_configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT), ENABLED_STRING);
    }

    @Test
    public void testReplaceConfig() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        assertEquals(CONTENTS, _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents());

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, NEW_CONTENTS, Scope.Project, PROJECT);

        assertEquals(NEW_CONTENTS, _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents());
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
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, configFile, Scope.Project, PROJECT);

        String newContents = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents();
        assertEquals(configFile.length(), newContents.length());  //truncated?
        assertEquals(configFile, newContents); //corrupted?
        assertNull(_configService.getConfig(TOOL_NAME, PATH));
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
            assertNull(_configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT));
            return;
        }
        fail();
    }

    @Test
    public void testStatus() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, SOP_1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, SOME_OTHER_TOOL, PATH, CONTENTS, Scope.Project, SOP_2);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        final String first = _configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT);

        assertEquals(ENABLED_STRING, first);

        _configService.disable(USERNAME, REASON_CREATED, TOOL_NAME, PATH, Scope.Project, PROJECT);
        final String second = _configService.getStatus(TOOL_NAME, PATH, Scope.Project, PROJECT);

        assertEquals(DISABLED_STRING, second);
        assertNull(_configService.getStatus(TOOL_NAME, PATH));
    }

    @Test
    public void testHistory() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, SOP_1);
        _configService.replaceConfig(USERNAME, REASON_CREATED, SOME_OTHER_TOOL, PATH, CONTENTS, Scope.Project, SOP_2);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_REPLACED, TOOL_NAME, PATH, CONTENTS + SOME_STUFF, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_REPLACED, TOOL_NAME, PATH, CONTENTS + OTHER_STUFF, Scope.Project, PROJECT);

        final List<Configuration> list = _configService.getHistory(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(3, list.size());
        assertEquals(REASON_REPLACED, list.get(1).getReason());
    }

    @Test
    public void testDuplicateConfigurations() throws ConfigServiceException {
        _testDBUtils.cleanDb();

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);

        //we should wind up with 3 configurations, 1 config file
        final List<Configuration> list = _configService.getHistory(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(3, list.size());
        assertEquals(1, _testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT));  //FYI:  This will fail when the table name changes.

        //Make sure enabling and disabling does not create a new script each time. 
        //Also, make sure it adds the appropriate # of rows to XHBM_CONFIGURATION
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.disable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);
        _configService.enable(USERNAME, REASON_STATUS, TOOL_NAME, PATH, Scope.Project, PROJECT);

        //1 config file, 9 configurations
        assertEquals(9, _testDBUtils.countConfigurationRows(TOOL_NAME, PATH, Scope.Project, PROJECT));  //FYI:  This will fail when the table name changes.
        assertEquals(1, _testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT));  //FYI:  This will fail when the table name changes.

        //change the contents and assure there are 2 rows in config data
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS + CHANGE, Scope.Project, PROJECT);
        assertEquals(2, _testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT));  //FYI:  This will fail when the table name changes.

        //add the same configuration to a different tool. 
        //configs aren't shared between tools, so we should end up with three rows in config data.
        _configService.replaceConfig(USERNAME, REASON_CREATED, DIFFERENT_TOOL, PATH, CONTENTS, Scope.Project, PROJECT);
        assertEquals(2, _testDBUtils.countConfigurationDataRows(TOOL_NAME, PATH, Scope.Project, PROJECT));  //FYI:  This will fail when the table name changes.
        assertEquals(3, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.
    }

    @Test
    public void testNullContents() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        assertNotNull(_configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents());
        _configService.replaceConfig(USERNAME, NULLING_CONFIG, TOOL_NAME, PATH, null, Scope.Project, PROJECT);
        assertEquals(2, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.
        assertEquals(2, _testDBUtils.countConfigurationRows());  //FYI:  This will fail when the table name changes.    	
        assertNull(_configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getContents());
    }

    @Test
    public void testDisablingAndEnablingConfig() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS);
        assertEquals(ENABLED_STRING, _configService.getConfig(TOOL_NAME, PATH).getStatus());
        _configService.disable(USERNAME, REASON_DISABLING, TOOL_NAME, PATH);
        assertEquals(DISABLED_STRING, _configService.getConfig(TOOL_NAME, PATH).getStatus());
        _configService.enable(USERNAME, REASON_ENABLING, TOOL_NAME, PATH);

        Configuration siteConfig = _configService.getConfig(TOOL_NAME, PATH);
        assertEquals(ENABLED_STRING, siteConfig.getStatus());
        assertEquals(CONTENTS, siteConfig.getContents());

        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS, Scope.Project, PROJECT);
        assertEquals(ENABLED_STRING, _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getStatus());
        _configService.disable(USERNAME, REASON_DISABLING, TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(DISABLED_STRING, _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT).getStatus());
        _configService.enable(USERNAME, REASON_ENABLING, TOOL_NAME, PATH, Scope.Project, PROJECT);

        Configuration prjConfig = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(ENABLED_STRING, prjConfig.getStatus());
        assertEquals(CONTENTS, prjConfig.getContents());
    }

    @Test
    public void testNullPath() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, null, CONTENTS, Scope.Project, PROJECT);
        assertEquals(CONTENTS, _configService.getConfig(TOOL_NAME, null, Scope.Project, PROJECT).getContents());
        assertNull(_configService.getConfig(TOOL_NAME, null));
    }

    @Test
    public void testNullPathAndTool() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, null);
        assertNull(CONTENTS, _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS);
        assertEquals(CONTENTS, _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS + CHANGE);
        assertEquals(CONTENTS + CHANGE, _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(USERNAME, REASON_CREATED, null, null, CONTENTS + PROJECT_CHANGE, Scope.Project, PROJECT);
        assertEquals(CONTENTS + PROJECT_CHANGE, _configService.getConfig(null, null, Scope.Project, PROJECT).getContents());
    }

    @Test
    public void testGetById() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V1, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v3 = _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V3, Scope.Project, PROJECT);
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V4, Scope.Project, PROJECT);

        //really, if they both have the same ID, you had to pull the correct one.
        assertEquals(v3.getId(), _configService.getConfigById(TOOL_NAME, PATH, Long.toString(v3.getId()), Scope.Project, PROJECT).getId());

        //let's try to pull one from a different project but the same ID. just to make sure this method isn't behaving
        //like the unchecked .getById(Long id) because that would be a problem.
        final Configuration otherProj = _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_TESTING_VERSIONS, Scope.Project, SOP_1);
        assertNotEquals(otherProj.getId(), _configService.getConfigById(TOOL_NAME, PATH, Long.toString(v3.getId()), Scope.Project, PROJECT).getId());

        final Configuration another = _configService.getById(otherProj.getId());
        assertEquals(otherProj, another);
    }

    @Test
    public void testVersionNumbers() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V1, Scope.Project, PROJECT);
        final Configuration v1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(1, v1.getVersion());
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(2, v2.getVersion());

        final Configuration v2Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 2, Scope.Project, PROJECT);
        assertEquals(v2.getContents(), v2Check.getContents());
        assertEquals(v2.getVersion(), v2Check.getVersion());

        final Configuration v1Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 1, Scope.Project, PROJECT);
        assertEquals(v1.getContents(), v1Check.getContents());
        assertEquals(v1.getVersion(), v1Check.getVersion());
    }

    @Test
    public void testUnversionedConfig() throws ConfigServiceException {
        // First call defines this as unversioned.
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, true, CONTENTS_V1, Scope.Project, PROJECT);
        final Configuration v1 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(1, v1.getVersion());
        assertEquals(CONTENTS_V1, v1.getContents());

        // Second call it should remain unversioned.
        _configService.replaceConfig(USERNAME, REASON_CREATED, TOOL_NAME, PATH, CONTENTS_V2, Scope.Project, PROJECT);
        final Configuration v2 = _configService.getConfig(TOOL_NAME, PATH, Scope.Project, PROJECT);
        assertEquals(1, v2.getVersion());
        assertEquals(CONTENTS_V2, v2.getContents());

        final Configuration v2Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 2, Scope.Project, PROJECT);
        assertNull(v2Check);

        final Configuration v1Check = _configService.getConfigByVersion(TOOL_NAME, PATH, 1, Scope.Project, PROJECT);
        assertEquals(v2.getContents(), v1Check.getContents());
        assertEquals(v2.getVersion(), v1Check.getVersion());
        assertEquals(v1.getVersion(), v1Check.getVersion());
    }

    @Test
    public void testPostChangeListener() throws ConfigServiceException {
        _configService.replaceConfig(USERNAME, REASON_CREATED, LISTENER_TOOL, LISTENER_PATH, Boolean.TRUE.toString());
        final Configuration separatePetMr = _configService.getConfig(LISTENER_TOOL, LISTENER_PATH);
        assertNotNull(separatePetMr);
        assertEquals(1, PETMRSettingChange.getChanges());
        _configService.replaceConfig(USERNAME, REASON_CREATED, LISTENER_TOOL, LISTENER_PATH, Boolean.FALSE.toString());
        assertEquals(2, PETMRSettingChange.getChanges());
    }

    private TestDBUtils   _testDBUtils;
    private ConfigService _configService;
}
