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
import org.junit.After;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
public class ConfigPlatformTests {

    static final String contents = "yellow=#FFFF00";
    static final String path = "/path/to/config";
    static final String project = "project1";
    static final String toolName = "coloringTool";
    static final String listenerToolName = "separatePetMr";
    static final String listenerPath = "Bool";
    static final String xnatUser = "admin";
    static final String reasonCreated = "created";

    @Before
    public void setup() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        //nothing wrong with putting some garbage in the database to start up.
        //this helps us know if there is any problems with a random set of configs.
        _configService.replaceConfig("frank", "new script", "franksTool", "/some/sort/of/path", "thisisfranksscript", Scope.Project, "SOP");
        _configService.replaceConfig("bill", "frank's script was junk", "SOMEOTHERTOOL", path, "thisisbillsscript", Scope.Project, "SOP");
    }

    @After
    public void teardown() throws SQLException {
        //don't clean up the database here so you can look at it if you want.
    }

    /**
     * This runs some basic sanity checks on the data source to make certain that simple database transactions are working properly before moving onto more complex operations.
     *
     * @throws SQLException When an error occurs running SQL.
     */
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
	@Test
    public void testDataSource() throws SQLException {
        Connection connection = _testDBUtils.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS TEST");
        statement.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
        statement.execute("INSERT INTO TEST VALUES(1, 'Hello')");
        statement.execute("INSERT INTO TEST VALUES(2, 'World')");
        statement.execute("SELECT * FROM TEST ORDER BY ID");
        ResultSet results = statement.getResultSet();
        int index = 1;
        while (results.next()) {
            int id = results.getInt("ID");
            String name = results.getString("NAME");
            assertEquals(index, id);
            assertEquals(index == 1 ? "Hello" : "World", name);
            index++;
        }
        statement.execute("DROP TABLE TEST");
    }

    @Test
    public void testGetAll() throws ConfigServiceException {
        //clean up from the @before
        _testDBUtils.cleanDb();

        ArrayList<String> paths = new ArrayList<>();
        paths.add(path);
        paths.add("newPath");
        paths.add("another");

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(0), contents);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(1), contents);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(2), contents);
        List<Configuration> list = _configService.getAll();

        assertEquals(list.size(), 3);

        List<String> returnedPaths = new ArrayList<>();
        for (Configuration c : list) {
            returnedPaths.add(c.getPath());
        }
        Collections.sort(returnedPaths);
        Collections.sort(paths);

        assertArrayEquals(paths.toArray(), returnedPaths.toArray());
    }

    @Test
    public void testConfigInsert() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents);
        String result = _configService.getConfig(toolName, path).getContents();
        assertEquals(contents, result);
    }

    @Test
    public void testGetConfigContents() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents);
        String result = _configService.getConfigContents(toolName, path);
        assertEquals(contents, result);
    }

    @Test
    public void testGetTools() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        ArrayList<String> tools = new ArrayList<>();
        tools.add(toolName);
        tools.add("Frank");
        tools.add("Bill");

        _configService.replaceConfig(xnatUser, reasonCreated, tools.get(0), path, contents);
        _configService.replaceConfig(xnatUser, reasonCreated, tools.get(1), path, contents);
        _configService.replaceConfig(xnatUser, reasonCreated, tools.get(2), path, contents);

        List<String> list = _configService.getTools();
        assertEquals(3, list.size());

        Collections.sort(list);
        Collections.sort(tools);
        assertArrayEquals(tools.toArray(), list.toArray());
    }

    @Test
    public void testGetToolsByProject() throws ConfigServiceException {
        ArrayList<String> tools = new ArrayList<>();
        tools.add(toolName);
        tools.add("Frank");

        _configService.replaceConfig(xnatUser, reasonCreated, tools.get(0), path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, tools.get(1), path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, Scope.Project, "NOTPROJECT");
        List<String> list = _configService.getTools(Scope.Project, project);
        assertEquals(2, list.size());

        Collections.sort(list);
        Collections.sort(tools);

        assertArrayEquals(tools.toArray(), list.toArray());
    }

    @Test
    public void testGetConfigsByTool() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, Scope.Project, "NOTPROJECT");

        List<Configuration> list = _configService.getConfigsByTool(toolName);
        assertEquals(1, list.size());

        Configuration c = list.get(0);

        assertEquals(toolName, c.getTool());

        //now test by project
        list = _configService.getConfigsByTool("Frank", Scope.Project, "NOTPROJECT");
        assertNull(list);

        list = _configService.getConfigsByTool("Bill", Scope.Project, "NOTPROJECT");
        c = list.get(0);

        assertEquals("Bill", c.getTool());
    }

    @Test
    public void testGetProjects() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, Scope.Project, "NOTPROJECT");
        List<String> list = _configService.getProjects();
        assertEquals(2, list.size());

        list = _configService.getProjects(toolName);
        assertEquals(1, list.size());
        assertEquals(project, list.get(0));
    }

    @Test
    public void testGetStatus() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, Scope.Project, "NOTPROJECT");

        String status = _configService.getStatus(toolName, path, Scope.Project, project);

        assertEquals(status, Configuration.ENABLED_STRING);
    }

    @Test
    public void testReplaceConfig() throws ConfigServiceException {
        final String newContents = "NEW CONTENTS";

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);

        String config = _configService.getConfig(toolName, path, Scope.Project, project).getContents();
        assertEquals(contents, config);

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, newContents, Scope.Project, project);

        config = _configService.getConfig(toolName, path, Scope.Project, project).getContents();
        assertEquals(newContents, config);
    }

    /**
     * Make sure a config Config doesn't get truncated, corrupted or do anything silly to the database. fill that file with whatever you think might cause problems.
     */
    @Test
    public void testRealConfigFile() throws ConfigServiceException {
        InputStream inputStream = getClass().getResourceAsStream("testConfigurationFile.txt");
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
            inputStream.close();
        } catch (IOException e) {
            fail("Unable to open testConfigurationFile.txt from the org.nrg.config classpath.");
        }

        String configFile = writer.toString();

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, "SOP");
        _configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, Scope.Project, "SOP2");

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, configFile, Scope.Project, project);

        String newContents = _configService.getConfig(toolName, path, Scope.Project, project).getContents();
        assertEquals(configFile.length(), newContents.length());  //truncated?
        assertEquals(configFile, newContents); //corrupted?

        assertNull(_configService.getConfig(toolName, path));
    }

    /**
     * Make sure the app lets you know if you create a file that would be truncated.
     */
    @Test
    public void testLargeConfigFile() {
        char[] data = new char[ConfigService.MAX_FILE_LENGTH + 1];
        String configFile = new String(data);

        try {
            _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, configFile, Scope.Project, project);
        } catch (ConfigServiceException e) {
            assertNull(_configService.getConfig(toolName, path, Scope.Project, project));
            return;
        }
        fail();
    }

    @Test
    public void testStatus() throws ConfigServiceException {
        String status;
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, "SOP");
        _configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, Scope.Project, "SOP2");

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);

        status = _configService.getStatus(toolName, path, Scope.Project, project);
        assertEquals(Configuration.ENABLED_STRING, status);

        _configService.disable(xnatUser, reasonCreated, toolName, path, Scope.Project, project);

        status = _configService.getStatus(toolName, path, Scope.Project, project);
        assertEquals(Configuration.DISABLED_STRING, status);

        assertNull(_configService.getStatus(toolName, path));
    }

    @Test
    public void testHistory() throws ConfigServiceException {
        String replaceReasonString = "bug so i replaced it";
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, "SOP");
        _configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, Scope.Project, "SOP2");
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, replaceReasonString, toolName, path, contents + "SOMESTUFF", Scope.Project, project);
        _configService.replaceConfig(xnatUser, replaceReasonString, toolName, path, contents + "OtherSTUFF", Scope.Project, project);

        List<Configuration> list = _configService.getHistory(toolName, path, Scope.Project, project);
        assertEquals(3, list.size());
        assertEquals(replaceReasonString, list.get(1).getReason());
    }

    @Test
    public void testDuplicateConfigurations() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);

        //we should wind up with 3 configurations, 1 config file
        List<Configuration> list = _configService.getHistory(toolName, path, Scope.Project, project);
        assertEquals(3, list.size());
        assertEquals(1, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.

        //Make sure enabling and disabling does not create a new script each time. 
        //Also, make sure it adds the appropriate # of rows to XHBM_CONFIGURATION
        _configService.disable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);
        _configService.disable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);
        _configService.disable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, Scope.Project, project);

        //1 config file, 9 configurations
        assertEquals(1, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.
        assertEquals(9, _testDBUtils.countConfigurationRows());  //FYI:  This will fail when the table name changes.

        //change the contents and assure there are 2 rows in config data
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents + "change", Scope.Project, project);
        assertEquals(2, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.


        //add the same configuration to a different tool. 
        //configs aren't shared between tools so we should end up with three rows in config data.
        _configService.replaceConfig(xnatUser, reasonCreated, "DIFFERENT TOOL", path, contents, Scope.Project, project);
        assertEquals(3, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.
    }

    @Test
    public void testNullContents() throws ConfigServiceException {
        _testDBUtils.cleanDb();
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        assertNotNull(_configService.getConfig(toolName, path, Scope.Project, project).getContents());
        _configService.replaceConfig(xnatUser, "nulling config", toolName, path, null, Scope.Project, project);
        assertEquals(2, _testDBUtils.countConfigurationDataRows());  //FYI:  This will fail when the table name changes.
        assertEquals(2, _testDBUtils.countConfigurationRows());  //FYI:  This will fail when the table name changes.    	
        assertNull(_configService.getConfig(toolName, path, Scope.Project, project).getContents());
    }

    @Test
    public void testDisablingAndEnablingConfig() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents);
        assertEquals("enabled", _configService.getConfig(toolName, path).getStatus());
        _configService.disable(xnatUser, "disabling", toolName, path);
        assertEquals("disabled", _configService.getConfig(toolName, path).getStatus());
        _configService.enable(xnatUser, "enabling", toolName, path);

        Configuration siteConfig = _configService.getConfig(toolName, path);
        assertEquals("enabled", siteConfig.getStatus());
        assertEquals(contents, siteConfig.getContents());

        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, Scope.Project, project);
        assertEquals("enabled", _configService.getConfig(toolName, path, Scope.Project, project).getStatus());
        _configService.disable(xnatUser, "disabling", toolName, path, Scope.Project, project);
        assertEquals("disabled", _configService.getConfig(toolName, path, Scope.Project, project).getStatus());
        _configService.enable(xnatUser, "enabling", toolName, path, Scope.Project, project);

        Configuration prjConfig = _configService.getConfig(toolName, path, Scope.Project, project);
        assertEquals("enabled", prjConfig.getStatus());
        assertEquals(contents, prjConfig.getContents());
    }

    @Test
    public void testNullPath() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, null, contents, Scope.Project, project);
        assertEquals(contents, _configService.getConfig(toolName, null, Scope.Project, project).getContents());
        assertNull(_configService.getConfig(toolName, null));
    }

    @Test
    public void testNullPathAndTool() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, null, null, null);
        assertNull(contents, _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(xnatUser, reasonCreated, null, null, contents);
        assertEquals(contents, _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(xnatUser, reasonCreated, null, null, contents + "change");
        assertEquals(contents + "change", _configService.getConfig(null, null).getContents());

        _configService.replaceConfig(xnatUser, reasonCreated, null, null, contents + "projectChange", Scope.Project, project);
        assertEquals(contents + "projectChange", _configService.getConfig(null, null, Scope.Project, project).getContents());
    }

    @Test
    public void testGetById() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version1", Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version2", Scope.Project, project);
        Configuration v3 = _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version3", Scope.Project, project);
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version4", Scope.Project, project);

        Configuration retrieved = _configService.getConfigById(toolName, path, v3.getId() + "", Scope.Project, project);

        //really, if they both have the same ID, you had to pull the correct one.
        assertEquals(v3.getId(), retrieved.getId());

        //let's try to pull one from a different project but the same ID. just to make sure this method isn't behaving
        //like the unchecked .getById(Long id) because that would be a problem.
        Configuration otherProj = _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "testingVersions", Scope.Project, "SOP");
        retrieved = _configService.getConfigById(toolName, path, v3.getId() + "", Scope.Project, project);
        assertFalse(retrieved.getId() == otherProj.getId());

        Configuration another = _configService.getById(otherProj.getId());
        assertEquals(otherProj, another);
    }

    @Test
    public void testVersionNumbers() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version1", Scope.Project, project);
        Configuration v1 = _configService.getConfig(toolName, path, Scope.Project, project);
        assertEquals(1, v1.getVersion());
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version2", Scope.Project, project);
        Configuration v2 = _configService.getConfig(toolName, path, Scope.Project, project);
        assertEquals(2, v2.getVersion());

        Configuration v2Check = _configService.getConfigByVersion(toolName, path, 2, Scope.Project, project);
        assertEquals(v2.getContents(), v2Check.getContents());
        assertEquals(v2.getVersion(), v2Check.getVersion());

        Configuration v1Check = _configService.getConfigByVersion(toolName, path, 1, Scope.Project, project);
        assertEquals(v1.getContents(), v1Check.getContents());
        assertEquals(v1.getVersion(), v1Check.getVersion());
    }

    @Test
    public void testUnversionedConfig() throws ConfigServiceException {
        // First call defines this as unversioned.
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, true, "version1", Scope.Project, project);
        Configuration v1 = _configService.getConfig(toolName, path, Scope.Project, project);
        assertEquals(1, v1.getVersion());
        assertEquals("version1", v1.getContents());

        // Second call it should remain unversioned.
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version2", Scope.Project, project);
        Configuration v2 = _configService.getConfig(toolName, path, Scope.Project, project);
        assertEquals(1, v2.getVersion());
        assertEquals("version2", v2.getContents());

        Configuration v2Check = _configService.getConfigByVersion(toolName, path, 2, Scope.Project, project);
        assertNull(v2Check);

        Configuration v1Check = _configService.getConfigByVersion(toolName, path, 1, Scope.Project, project);
        assertEquals(v2.getContents(), v1Check.getContents());
        assertEquals(v2.getVersion(), v1Check.getVersion());
        assertEquals(v1.getVersion(), v1Check.getVersion());
    }

    @Test
    public void testPostChangeListener() throws ConfigServiceException {
        _configService.replaceConfig(xnatUser, reasonCreated, listenerToolName, listenerPath, "true");
        final Configuration separatePetMr = _configService.getConfig(listenerToolName, listenerPath);
        assertNotNull(separatePetMr);
        assertEquals(1, PETMRSettingChange.getChanges());
        _configService.replaceConfig(xnatUser, reasonCreated, listenerToolName, listenerPath, "false");
        assertEquals(2, PETMRSettingChange.getChanges());
    }

    @Inject
    private TestDBUtils _testDBUtils;

    @Autowired
    private ConfigService _configService;
}
