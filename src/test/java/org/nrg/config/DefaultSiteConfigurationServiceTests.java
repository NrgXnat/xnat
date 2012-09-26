package org.nrg.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.exceptions.DuplicateConfigurationDetectedException;
import org.nrg.config.exceptions.SiteConfigurationFileNotFoundException;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.config.services.impl.DefaultSiteConfigurationService;
import org.nrg.config.util.TestDBUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DefaultSiteConfigurationServiceTests {
	
	private List<String> savedConfigFileLocations;
	
	@Before
	public void setUp() {
		_testDBUtils.cleanDb();
		savedConfigFileLocations = new ArrayList<String>(((DefaultSiteConfigurationService) _service).getConfigFilesLocations());
	}
	
	@After
	public void tearDown() {
		((DefaultSiteConfigurationService) _service).setConfigFilesLocations(savedConfigFileLocations);
	}
	
    @Test
    public void initSiteConfigurationSuccess() throws ConfigServiceException {
    	Properties props = _service.getSiteConfiguration();
    	assertNotNull(props);
    	assertEquals("val1", props.getProperty("prop1"));
    	assertEquals("fooval1", props.getProperty("foo.prop1"));
    	assertEquals("overrideval2", props.getProperty("foo.prop2"));
    	assertEquals("val1", props.getProperty("dontpersist1.dontpersistprop1"));
    	assertEquals("true", props.getProperty("boolprop2"));
    	assertNotNull(props.getProperty("override.prop1"));
    	assertNull(props.getProperty("foo.prop3"));
    	Configuration persistedConfig = ((DefaultSiteConfigurationService) _service).getPersistedSiteConfiguration();
    	assertTrue(persistedConfig.getContents().contains("foo.prop1"));
    	assertTrue(persistedConfig.getContents().contains("foo.prop2"));
    	assertTrue(persistedConfig.getContents().contains("foo.prop1"));
    	assertFalse(persistedConfig.getContents().contains("dontpersistprop1"));
    	assertFalse(persistedConfig.getContents().contains("dontpersistprop2"));
    	assertFalse(persistedConfig.getContents().contains("persist"));
    	assertFalse(persistedConfig.getContents().contains("override.prop1"));
    }
    
    @Test
    public void initSiteConfigurationSuccessWithAdditionalPropertiesOnSecondLaunch() throws ConfigServiceException {
    	Properties props = _service.getSiteConfiguration();
    	assertNull(props.getProperty("prop2"));
    	List<String> mockConfigFileLocations = new ArrayList<String>(savedConfigFileLocations);
    	mockConfigFileLocations.set(0, mockConfigFileLocations.get(0).concat("/additionalProperties"));
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
   		props = _service.getSiteConfiguration();
    	assertNotNull(props.getProperty("prop2"));
    	assertEquals(2, _testDBUtils.countConfigurationDataRows());
    }
    
    @Test
    public void initSiteConfigurationSuccessWithNoAdditionalPropertiesOnSecondLaunch() throws ConfigServiceException {
    	_service.getSiteConfiguration();
    	List<String> mockConfigFileLocations = new ArrayList<String>(savedConfigFileLocations);
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
    	// it shouldn't be writing the config to DB again, since nothing's changed
   		_service.getSiteConfiguration();
    	assertEquals(1, _testDBUtils.countConfigurationDataRows());
    }
    
    @Test(expected=SiteConfigurationFileNotFoundException.class)
    public void initSiteConfigurationFailsWhenNoSiteConfigIsFound() throws ConfigServiceException {
    	List<String> mockConfigFileLocations = Arrays.asList("/bridge/to/nowhere", "/foo/bar", "/baz");
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
   		_service.getSiteConfiguration();
    }
    
    @Test(expected=DuplicateConfigurationDetectedException.class)
    public void initSiteConfigurationFailsWhenDuplicateSiteConfigFileIsFound() throws ConfigServiceException {
    	List<String> mockConfigFileLocations = new ArrayList<String>(savedConfigFileLocations);
    	mockConfigFileLocations.add(mockConfigFileLocations.get(0));
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
   		_service.getSiteConfiguration();
    }
    
    @Test(expected=DuplicateConfigurationDetectedException.class)
    public void initSiteConfigurationFailsWhenDuplicateCustomConfigFileIsFound() throws ConfigServiceException {
    	List<String> mockConfigFileLocations = new ArrayList<String>(savedConfigFileLocations);
    	mockConfigFileLocations.add(mockConfigFileLocations.get(0).concat("/duplicateFiles"));
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
   		_service.getSiteConfiguration();
    }
    
    @Test(expected=DuplicateConfigurationDetectedException.class)
    public void initSiteConfigurationFailsWhenDuplicateCustomConfigPropertyIsFound() throws ConfigServiceException {
    	List<String> mockConfigFileLocations = new ArrayList<String>(savedConfigFileLocations);
    	mockConfigFileLocations.add(mockConfigFileLocations.get(0).concat("/duplicateProperties"));
    	((DefaultSiteConfigurationService) _service).setConfigFilesLocations(mockConfigFileLocations);
   		_service.getSiteConfiguration();
    }
    
    @Test
    public void setSiteConfiguration() throws ConfigServiceException {
    	
    	Properties props = _service.getSiteConfiguration();
    	assertNull(props.getProperty("foo.prop3"));
    	props.setProperty("foo.prop3", "fooval3");
    	_service.setSiteConfiguration(ADMIN_USER, props);
    	props = _service.getSiteConfiguration();
    	assertEquals("fooval3", props.getProperty("foo.prop3"));
    }
    
    @Test
    public void getSiteConfigurationReturnedPropertiesShouldBeReadOnly() throws ConfigServiceException {
    	
    	Properties props = _service.getSiteConfiguration();
    	assertNull(props.getProperty("foo.prop3"));
    	props.setProperty("foo.prop3", "fooval3");
    	props = _service.getSiteConfiguration();
    	assertNull(props.getProperty("foo.prop3"));
    }
    
    @Test
    public void getSiteConfigurationProperty() throws ConfigServiceException {
    	
    	assertEquals(_service.getSiteConfigurationProperty("foo.prop1"), "fooval1");
    }
    
    @Test
    public void setSiteConfigurationProperty() throws ConfigServiceException {
    	
    	assertEquals("fooval1", _service.getSiteConfigurationProperty("foo.prop1"));
    	_service.setSiteConfigurationProperty(ADMIN_USER, "foo.prop1", "fooval2");
    	assertEquals("fooval2", _service.getSiteConfigurationProperty("foo.prop1"));
    	assertNull(_service.getSiteConfigurationProperty("foo.prop3"));
    	_service.setSiteConfigurationProperty(ADMIN_USER, "foo.prop3", "fooval3");
    	assertEquals("fooval3", _service.getSiteConfigurationProperty("foo.prop3"));
    }
    
    @Inject
    private SiteConfigurationService _service;
    
    @Inject
    private TestDBUtils _testDBUtils;
    
    private static final String ADMIN_USER = "admin";
}
