package org.nrg.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

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

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests various platform attributes to ensure that they're working, e.g. connection
 * to the data source.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ConfigPlatformTests {
	
	static final String contents = "yellow=#FFFF00";
	static final String path = "/path/to/config";
	static final String project = "project1";
	static final String toolName = "coloringTool";
	static final String xnatUser = "admin";
	static final String reasonCreated = "created";
	
	//used for tests that would rather have an empty DB.
	private void cleanDb(){
		try {
			Connection connection = _dataSource.getConnection();
	        Statement statement = connection.createStatement();
	        statement.execute("DELETE FROM XHBM_CONFIGURATION;");  //FYI: this will fail if the table name changes...
	        statement.execute("DELETE FROM XHBM_CONFIGURATION_DATA;");
	        statement.close();
		} catch (SQLException e){
			fail();
		}
	}
	
	@Before
	public void setup() throws ConfigServiceException {	
		cleanDb();
		//nothing wrong with putting some garbage in the database to start up. 
		//this helps us know if there is any problems with a random set of configs.
        _configService.replaceConfig("frank", "new script", "franksTool", "/some/sort/of/path", "thisisfranksscript", "SOMEOTHERPROJECT");	
    	_configService.replaceConfig("bill", "frank's script was junk", "SOMEOTHERTOOL", path, "thisisbillsscript", "SOMEOTHERPROJECT");
	}
	
	@After
	public void teardown() throws SQLException {
		//don't clean up the database here so you can look at it if you want.
	}
	
    /**
     * This runs some basic sanity checks on the data source to make
     * certain that simple database transactions are working properly
     * before moving onto more complex operations.
     * 
     * @throws SQLException
     */
    @Test
    public void testDataSource() throws SQLException {
        assertNotNull(_dataSource);
        Connection connection = _dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS TEST");
        statement.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
        statement.execute("INSERT INTO TEST VALUES(1, 'Hello')");
        statement.execute("INSERT INTO TEST VALUES(2, 'World')");
        statement.execute("SELECT * FROM TEST ORDER BY ID");
        ResultSet results = statement.getResultSet();
        int index = 1;
        while(results.next()) {
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
    	cleanDb();
    	
    	ArrayList<String> paths = new ArrayList<String>();
    	paths.add(path);
    	paths.add("newPath");
    	paths.add("anotherPath");
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(0), contents);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(1), contents);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, paths.get(2), contents);
    	List<Configuration> list = _configService.getAll();
    	
    	assertEquals(list.size(), 3);
    	
    	List<String> returnedPaths = new ArrayList<String>();
    	for(Configuration c:list){
    		returnedPaths.add(c.getPath());
    	}
    	Collections.sort(returnedPaths);
    	Collections.sort(paths);
    	
    	assertArrayEquals(paths.toArray(),returnedPaths.toArray());
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
    	
    	cleanDb();
    	ArrayList<String> tools = new ArrayList<String>();
    	tools.add(toolName);
    	tools.add("Frank");
    	tools.add("Bill");
    
    	_configService.replaceConfig(xnatUser, reasonCreated, tools.get(0), path, contents);
    	_configService.replaceConfig(xnatUser, reasonCreated, tools.get(1), path, contents);
    	_configService.replaceConfig(xnatUser, reasonCreated, tools.get(2), path, contents);

    	List<String> list = _configService.getTools();
    	assertEquals(3,list.size());

    	Collections.sort(list);
    	Collections.sort(tools);
    	assertArrayEquals(tools.toArray(), list.toArray() );
    }
    
    @Test
    public void testGetToolsByProject() throws ConfigServiceException {
    	
    	ArrayList<String> tools = new ArrayList<String>();
    	tools.add(toolName);
    	tools.add("Frank");
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, tools.get(0), path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, tools.get(1), path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, "NOTPROJECT");
    	List<String> list = _configService.getTools(project);
    	assertEquals(2,list.size());
    	
    	Collections.sort(list);
    	Collections.sort(tools);
    	
    	assertArrayEquals(tools.toArray(), list.toArray() );
    }
    
    @Test
    public void testGetConfigsByTool() throws ConfigServiceException {
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, "NOTPROJECT");
    	
    	List<Configuration> list = _configService.getConfigsByTool(toolName);
    	assertEquals(1,list.size());
    	
    	Configuration c = list.get(0);
    	
    	assertEquals(toolName, c.getTool());

    	//now test by project
    	list = _configService.getConfigsByTool("Frank","NOTPROJECT");
    	assertNull(list);
    	
    	list = _configService.getConfigsByTool("Bill","NOTPROJECT");
    	 c = list.get(0);
     	
     	assertEquals("Bill", c.getTool());  	
    }
    
    
    @Test
    public void testGetProjects() throws ConfigServiceException {
    	cleanDb();
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, "NOTPROJECT");
    	List<String> list = _configService.getProjects();
    	assertEquals(2,list.size());
    	
    	list = _configService.getProjects(toolName);
    	assertEquals(1,list.size());
    	assertEquals(project, list.get(0));
    }
    
    @Test
    public void testGetStatus() throws ConfigServiceException {
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Frank", path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, "Bill", path, contents, "NOTPROJECT");
    	
    	String status = _configService.getStatus(toolName, path, project);
    	
    	assertEquals(status,Configuration.ENABLED_STRING);
    }
    
    @Test
    public void testReplaceConfig() throws ConfigServiceException {
    	final String newContents = "NEW CONTENTS";
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	
    	String config = _configService.getConfig(toolName, path, project).getContents();
    	assertEquals(contents, config);
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, newContents, project);
    	
    	config = _configService.getConfig(toolName, path, project).getContents();
    	assertEquals(newContents, config);
    }
    
    /**
     * Make sure a config Config doesn't get truncated, corrupted or do anything silly to the database.
     * fill that file with whatever you think might cause problems.
     */
    @Test
    public void testRealConfigFile() throws ConfigServiceException {
    	InputStream inputStream = getClass().getResourceAsStream("testConfigurationFile.txt");
    	StringWriter writer = new StringWriter();
    	try {
    		IOUtils.copy(inputStream, writer, "UTF-8");
    		inputStream.close();
    	} catch(IOException e){
    		fail("Unable to open testConfigurationFile.txt from the org.nrg.config classpath.");
    	}
    	
    	String configFile = writer.toString();
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, "SOMEOTHERPROJECT");
    	_configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, "SOMEOTHERPROJECT2");
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, configFile, project);
    	
    	String newContents = _configService.getConfig(toolName, path, project).getContents();
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
    		_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, configFile, project);
    	} catch (ConfigServiceException e) {
        	assertNull(_configService.getConfig(toolName, path, project));	
    		return;
    	}
    	fail();
    }
    
    @Test
    public void testStatus() throws ConfigServiceException {
    	String status;
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, "SOMEOTHERPROJECT");
    	_configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, "SOMEOTHERPROJECT2");
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	
    	status = _configService.getStatus(toolName, path, project);
    	assertEquals(Configuration.ENABLED_STRING, status);
    	
    	_configService.disable(xnatUser, reasonCreated, toolName, path, project);
   
    	status = _configService.getStatus(toolName, path, project); 	
    	assertEquals(Configuration.DISABLED_STRING, status);
    	
    	assertNull(_configService.getStatus(toolName, path));
    }
    
    @Test
    public void testHistory() throws ConfigServiceException {
    	String replaceReasonString = "bug so i replaced it";
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, "SOMEOTHERPROJECT");
    	_configService.replaceConfig(xnatUser, reasonCreated, "SOMEOTHERTOOL", path, contents, "SOMEOTHERPROJECT2");
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, replaceReasonString, toolName, path, contents + "SOMESTUFF", project);
    	_configService.replaceConfig(xnatUser, replaceReasonString, toolName, path, contents + "OtherSTUFF", project);
    	
    	List<Configuration> list = _configService.getHistory(toolName, path, project);
    	assertEquals(3,list.size());	
    	assertEquals(replaceReasonString, ((Configuration)list.get(1)).getReason());
    }
    
    private int countRows(String sql) {
    	int index = 0;
    	try {
	        Connection connection = _dataSource.getConnection();
	        Statement statement = connection.createStatement();
	        statement.execute(sql);  
	        ResultSet results = statement.getResultSet();  
	    	
	        while(results.next()) {
	            index++;
	        }
	        results.close();
	        statement.close();
    	} catch (SQLException e){
    		fail();
    	}
    	return index;
    }
    
    @Test
    public void testDuplicateConfigurations() throws ConfigServiceException{
    	cleanDb();
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	
    	//we should wind up with 3 configurations, 1 config file
    	List<Configuration> list = _configService.getHistory(toolName, path, project);
    	assertEquals(3,list.size());
        assertEquals(1, countRows("SELECT * FROM XHBM_CONFIGURATION_DATA"));  //FYI:  This will fail when the table name changes.
  
        
        //Make sure enabling and disabling does not create a new script each time. 
        //Also, make sure it adds the appropriate # of rows to XHBM_CONFIGURATION
        _configService.disable(xnatUser, "updatingStatus", toolName, path, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, project);
        _configService.disable(xnatUser, "updatingStatus", toolName, path, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, project);
        _configService.disable(xnatUser, "updatingStatus", toolName, path, project);
        _configService.enable(xnatUser, "updatingStatus", toolName, path, project);
        
        //1 config file, 9 configurations
        assertEquals(1,countRows("SELECT * FROM XHBM_CONFIGURATION_DATA"));  //FYI:  This will fail when the table name changes.
        assertEquals(9, countRows("SELECT * FROM XHBM_CONFIGURATION"));  //FYI:  This will fail when the table name changes.
  
        
        //change the contents and assure there are 2 rows in config data
        _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents +"change", project);
        assertEquals(2, countRows("SELECT * FROM XHBM_CONFIGURATION_DATA"));  //FYI:  This will fail when the table name changes.

        
        //add the same configuration to a different tool. 
        //configs aren't shared between tools so we should end up with three rows in config data.
        _configService.replaceConfig(xnatUser, reasonCreated, "DIFFERENT TOOL", path, contents, project);
        assertEquals(3, countRows("SELECT * FROM XHBM_CONFIGURATION_DATA"));  //FYI:  This will fail when the table name changes.
     }

    @Test
    public void testNullContents() throws ConfigServiceException {
    	cleanDb();
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	assertNotNull(_configService.getConfig(toolName, path, project).getContents());
    	_configService.replaceConfig(xnatUser, "nulling config", toolName, path, null, project);
        assertEquals(2,countRows("SELECT * FROM XHBM_CONFIGURATION_DATA"));  //FYI:  This will fail when the table name changes.
        assertEquals(2, countRows("SELECT * FROM XHBM_CONFIGURATION"));  //FYI:  This will fail when the table name changes.    	
    	assertNull(_configService.getConfig(toolName, path, project).getContents());
    }
    
    @Test
    public void testDisablingAndEnablingConfig() throws ConfigServiceException {
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, contents, project);
    	assertEquals("enabled", _configService.getConfig(toolName, path, project).getStatus());	
    	_configService.disable(xnatUser, "disabling", toolName, path, project);
    	assertEquals("disabled",_configService.getConfig(toolName, path, project).getStatus());
    	_configService.enable(xnatUser, "enabling", toolName, path, project);
    	assertEquals("enabled",_configService.getConfig(toolName, path, project).getStatus());
    }
    
    @Test
    public void testNullPath() throws ConfigServiceException {
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, null, contents, project);
    	assertEquals(contents, _configService.getConfig(toolName, null, project).getContents());
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
    	
    	_configService.replaceConfig(xnatUser, reasonCreated, null, null, contents + "projectChange", project);
    	assertEquals(contents + "projectChange",_configService.getConfig(null, null, project).getContents());
	
    }
    
    @Test
    public void testGetById() throws ConfigServiceException {
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version1", project);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version2", project);
    	Configuration v3 = _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version3", project);
    	_configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "version4", project);
    	
    	Configuration retrieved = _configService.getConfigById(toolName, path,v3.getId() + "", project);
    	
    	//really, if they both have the same ID, you had to pull the correct one.
    	assertEquals(v3.getId(), retrieved.getId());
    	
    	//let's try to pull one from a different project but the same ID. just to make sure this method isn't behaving
    	//like the unchecked .getById(Long id) because that would be a problem.
    	Configuration otherProj = _configService.replaceConfig(xnatUser, reasonCreated, toolName, path, "testingVersions", "someOtherProject");
    	retrieved = _configService.getConfigById(toolName, path,v3.getId() + "", project);
    	assertFalse(retrieved.getId() == otherProj.getId());
    	
    }
    
    @Inject
    @Named("dataSource")
    private DataSource _dataSource;
    
    @Autowired
    private ConfigService _configService;
}
