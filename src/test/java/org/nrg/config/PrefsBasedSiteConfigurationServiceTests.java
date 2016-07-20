/*
 * org.nrg.config.DefaultSiteConfigurationServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.configuration.NrgConfigTestConfiguration;
import org.nrg.config.exceptions.DuplicateConfigurationDetectedException;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.exceptions.SiteConfigurationFileNotFoundException;
import org.nrg.config.listeners.DefaultNamespacePropertyLevelListener;
import org.nrg.config.listeners.FooNamespaceLevelListener;
import org.nrg.config.listeners.FooPropertyLevelListener;
import org.nrg.config.listeners.SiteLevelListener;
import org.nrg.config.services.SiteConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
@Rollback
@Transactional
public class PrefsBasedSiteConfigurationServiceTests {

	@Before
	public void setUp() throws SiteConfigurationException {
		_service.updateSiteConfiguration(_configFilesLocations);
	}

	@After
	public void tearDown() throws NoSuchFieldException, IllegalAccessException {
		// Clear out the contents of the service. It caches things for performance, but this causes data contamination
		// for subsequent tests. The transactional boundaries from the test class @Transactional* annotations will clear
		// out the database.
		_service.resetSiteConfiguration();
		DefaultNamespacePropertyLevelListener.resetInvokedCount();
		FooNamespaceLevelListener.resetInvokedCount();
		FooPropertyLevelListener.resetInvokedCount();
		SiteLevelListener.resetInvokedCount();
	}

	@SuppressWarnings("Duplicates")
	@Test
	public void initSiteConfigurationSuccess() throws SiteConfigurationException {
		final Properties props = _service.getSiteConfiguration();
		assertNotNull(props);
		assertEquals("val1", props.getProperty("prop1"));
		assertEquals("fooval1", props.getProperty("foo.prop1"));
		assertEquals("overrideval2", props.getProperty("foo.prop2"));
		assertEquals("val1", props.getProperty("dontpersist1.dontpersistprop1"));
		assertEquals("true", props.getProperty("boolprop2"));
		assertNotNull(props.getProperty("override.prop1"));
		assertNull(props.getProperty("foo.prop3"));
	}

	@SuppressWarnings("Duplicates")
	@Test
	public void initSiteConfigurationSuccessWithAdditionalPropertiesOnSecondLaunch() throws SiteConfigurationException {
		final Properties props = _service.getSiteConfiguration();
		assertNull(props.getProperty("prop2"));
		List<String> mockConfigFileLocations = _service.getConfigFilesLocations();
		mockConfigFileLocations.set(0, mockConfigFileLocations.get(0).concat("/additionalProperties"));
		_service.updateSiteConfiguration(mockConfigFileLocations);
		props.clear();
		props.putAll(_service.getSiteConfiguration());
		assertNotNull(props.getProperty("prop2"));
		// assertEquals(2, _testDBUtils.countConfigurationDataRows());
	}

	@Test
	public void initSiteConfigurationSuccessWithNoAdditionalPropertiesOnSecondLaunch() throws SiteConfigurationException {
		_service.getSiteConfiguration();
		final List<String> mockConfigFileLocations = _service.getConfigFilesLocations();
		_service.setConfigFilesLocations(mockConfigFileLocations);
		// it shouldn't be writing the config to DB again, since nothing's changed
		_service.getSiteConfiguration();
		// assertEquals(1, _testDBUtils.countConfigurationDataRows());
	}

	@Test(expected = SiteConfigurationFileNotFoundException.class)
	public void initSiteConfigurationFailsWhenNoSiteConfigIsFound() throws SiteConfigurationException {
		final List<String> mockConfigFileLocations = Arrays.asList("/bridge/to/nowhere", "/foo/bar", "/baz");
		_service.updateSiteConfiguration(mockConfigFileLocations);
		_service.getSiteConfiguration();
	}

	@Test(expected = DuplicateConfigurationDetectedException.class)
	public void initSiteConfigurationFailsWhenDuplicateSiteConfigFileIsFound() throws SiteConfigurationException {
		final List<String> mockConfigFileLocations = _service.getConfigFilesLocations();
		mockConfigFileLocations.add(mockConfigFileLocations.get(0));
		_service.updateSiteConfiguration(mockConfigFileLocations);
		_service.getSiteConfiguration();
	}

	@Test(expected = DuplicateConfigurationDetectedException.class)
	public void initSiteConfigurationFailsWhenDuplicateCustomConfigFileIsFound() throws SiteConfigurationException {
		final List<String> mockConfigFileLocations = _service.getConfigFilesLocations();
		mockConfigFileLocations.add(mockConfigFileLocations.get(0).concat("/duplicateFiles"));
		_service.updateSiteConfiguration(mockConfigFileLocations);
	}

	@Test(expected = DuplicateConfigurationDetectedException.class)
	public void initSiteConfigurationFailsWhenDuplicateCustomConfigPropertyIsFound() throws SiteConfigurationException {
		final List<String> mockConfigFileLocations = _service.getConfigFilesLocations();
		mockConfigFileLocations.add(mockConfigFileLocations.get(0).concat("/duplicateProperties"));
		_service.updateSiteConfiguration(mockConfigFileLocations);
	}

	@Test
	public void getSiteConfigurationProperty() throws SiteConfigurationException {
		assertEquals("fooval1", _service.getSiteConfigurationProperty("foo.prop1"));
	}

	@SuppressWarnings("Duplicates")
	@Test
	public void setSiteConfigurationProperty() throws SiteConfigurationException {
		_service.setSiteConfigurationProperty(ADMIN_USER, "prop1", "newprop1Val");
		assertEquals(1, DefaultNamespacePropertyLevelListener.getInvokedCount());
		assertEquals("fooval1", _service.getSiteConfigurationProperty("foo.prop1"));
		_service.setSiteConfigurationProperty(ADMIN_USER, "foo.prop1", "fooval2");
		assertEquals("fooval2", _service.getSiteConfigurationProperty("foo.prop1"));
		assertEquals(1, FooNamespaceLevelListener.getInvokedCount());
		assertEquals(1, FooPropertyLevelListener.getInvokedCount());
		assertNull(_service.getSiteConfigurationProperty("foo.prop3"));
		_service.setSiteConfigurationProperty(ADMIN_USER, "foo.prop3", "fooval3");
		assertEquals("fooval3", _service.getSiteConfigurationProperty("foo.prop3"));
		assertEquals(2, FooNamespaceLevelListener.getInvokedCount());
		assertEquals(1, FooPropertyLevelListener.getInvokedCount());
		assertEquals(1, DefaultNamespacePropertyLevelListener.getInvokedCount());
		assertEquals(3, SiteLevelListener.getInvokedCount());
	}

	@Resource(name="configFilesLocations")
	public void setConfigFilesLocations(final List<String> configFilesLocations) {
		_configFilesLocations.addAll(configFilesLocations);
	}

	@Autowired
	@Qualifier("prefsBasedSiteConfigurationService")
	private SiteConfigurationService _service;

	private List<String> _configFilesLocations = new ArrayList<>();

	private static final String ADMIN_USER = "admin";
}
