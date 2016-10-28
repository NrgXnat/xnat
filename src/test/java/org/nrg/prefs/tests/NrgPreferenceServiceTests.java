/*
 * org.nrg.prefs.tests.NrgPreferenceServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Rollback
@Transactional
public class NrgPreferenceServiceTests {
    public NrgPreferenceServiceTests() {
        _log.info("Creating test class");
    }

    @Before
    public void initialize() {
        _log.info("Initializing test class");
        final Tool tool = _toolService.newEntity();
        tool.setToolId("tool2");
        tool.setToolName("Tool 2");
        _toolService.create(tool);

        _prefService.create("tool2", "Preference1", Scope.Site, "", "Value1_1");
        _prefService.create("tool2", "Preference2", Scope.Site, "", "Value2_1");
        _prefService.create("tool2", "Preference3", Scope.Site, "", "Value3_1");

        _prefService.create("tool2", "Preference1", Scope.Project, "project1", "Value1_2");
        _prefService.create("tool2", "Preference2", Scope.Project, "project1", "Value2_2");
        _prefService.create("tool2", "Preference4", Scope.Project, "project1", "Value4_2");
    }

    @After
    public void teardown() {
        _log.info("Tearing down test class");
    }

    @Test
    public void getSitePreference() throws NrgServiceException {
        final Preference retrievedPreference = _prefService.getPreference("tool2", "Preference1", Scope.Site, "");
        assertEquals("tool2", retrievedPreference.getTool().getToolId());
        assertEquals("Tool 2", retrievedPreference.getTool().getToolName());
        assertEquals("Preference1", retrievedPreference.getName());
        assertEquals("Value1_1", retrievedPreference.getValue());

        final Preference retrievedPreference2 = _prefService.getPreference("tool2", "Preference1");
        assertEquals("tool2", retrievedPreference2.getTool().getToolId());
        assertEquals("Tool 2", retrievedPreference2.getTool().getToolName());
        assertEquals("Preference1", retrievedPreference2.getName());
        assertEquals("Value1_1", retrievedPreference2.getValue());
    }

    @Test
    public void getProjectPreferenceWithStoredProjectPreference() throws NrgServiceException {
        final Preference retrievedPreference = _prefService.getPreference("tool2", "Preference1", Scope.Project, "project1");
        assertEquals("tool2", retrievedPreference.getTool().getToolId());
        assertEquals("Tool 2", retrievedPreference.getTool().getToolName());
        assertEquals("Preference1", retrievedPreference.getName());
        assertEquals("Value1_2", retrievedPreference.getValue());
    }

    @Test
    public void getProjectPreferenceWithStoredProjectPreferenceButNotThisOne() throws NrgServiceException {
        final Preference retrievedPreference = _prefService.getPreference("tool2", "Preference3", Scope.Project, "project1");
        assertEquals("tool2", retrievedPreference.getTool().getToolId());
        assertEquals("Tool 2", retrievedPreference.getTool().getToolName());
        assertEquals("Preference3", retrievedPreference.getName());
        assertEquals("Value3_1", retrievedPreference.getValue());
    }

    @Test
    public void getProjectPreferenceWithNoProjectPreference() throws NrgServiceException {
        final Preference retrievedPreference = _prefService.getPreference("tool2", "Preference1", Scope.Project, "project2");
        assertEquals("tool2", retrievedPreference.getTool().getToolId());
        assertEquals("Tool 2", retrievedPreference.getTool().getToolName());
        assertEquals("Preference1", retrievedPreference.getName());
        assertEquals("Value1_1", retrievedPreference.getValue());
    }

    @Test
    public void getSitePreferenceValue() throws NrgServiceException {
        final String prefValue = _prefService.getPreferenceValue("tool2", "Preference1", Scope.Site, "");
        assertEquals("Value1_1", prefValue);

        final String prefValue2 = _prefService.getPreferenceValue("tool2", "Preference1");
        assertEquals("Value1_1", prefValue2);
    }

    @Test
    public void getProjectPreferenceValueWithStoredProjectPreference() throws NrgServiceException {
        final String prefValue = _prefService.getPreferenceValue("tool2", "Preference1", Scope.Project, "project1");
        assertEquals("Value1_2", prefValue);
    }

    @Test
    public void getProjectPreferenceValueWithNoProjectPreference() throws NrgServiceException {
        final String prefValue = _prefService.getPreferenceValue("tool2", "Preference1", Scope.Project, "project2");
        assertEquals("Value1_1", prefValue);
    }

    @Test
    public void getSiteToolPropertyNames() throws NrgServiceException {
        final Set<String> toolNames = _prefService.getToolPropertyNames("tool2", Scope.Site, "");
        final Set<String> compareSet = Sets.newHashSet("Preference1", "Preference2", "Preference3");
        assertEquals(compareSet.size(), toolNames.size());
        assertTrue(toolNames.containsAll(compareSet));
    }

    @Test
    public void getProjectToolPropertyNamesWithStoredProjectPreference() throws NrgServiceException {
        final Set<String> toolNames = _prefService.getToolPropertyNames("tool2", Scope.Project, "project1");
        final Set<String> compareSet = Sets.newHashSet("Preference1", "Preference2", "Preference4");
        assertEquals(compareSet.size(), toolNames.size());
        assertTrue(toolNames.containsAll(compareSet));
    }

    @Test
    public void getProjectToolPropertyNamesWithNoProjectPreference() throws NrgServiceException {
        final Set<String> toolNames = _prefService.getToolPropertyNames("tool2", Scope.Project, "project2");
        final Set<String> compareSet = Sets.newHashSet();
        assertEquals(compareSet.size(), toolNames.size());
        assertTrue(toolNames.containsAll(compareSet));
    }

    @Test
    public void getSiteToolProperties() throws NrgServiceException {
        Properties compareProps = new Properties();
        compareProps.setProperty("Preference1", "Value1_1");
        compareProps.setProperty("Preference2", "Value2_1");
        compareProps.setProperty("Preference3", "Value3_1");

        final Properties props = _prefService.getToolProperties("tool2", Scope.Site, "");
        assertEquals(compareProps.size(), props.size());
        assertTrue(compareProps.keySet().containsAll(props.keySet()));
        assertTrue(compareProps.entrySet().containsAll(props.entrySet()));
    }

    @Test
    public void getProjectToolPropertiesWithStoredProjectPreference() throws NrgServiceException {
        final Properties compareProps = new Properties();
        compareProps.setProperty("Preference1", "Value1_2");
        compareProps.setProperty("Preference2", "Value2_2");
        compareProps.setProperty("Preference4", "Value4_2");

        final Properties props = _prefService.getToolProperties("tool2", Scope.Project, "project1");
        assertEquals(compareProps.size(), props.size());
        assertTrue(compareProps.keySet().containsAll(props.keySet()));
        assertTrue(compareProps.entrySet().containsAll(props.entrySet()));
    }

    @Test
    public void getProjectToolPropertiesWithNoProjectPreference() throws NrgServiceException {
        final Properties props = _prefService.getToolProperties("tool2", Scope.Project, "project2");
        assertEquals(0, props.size());
        assertTrue(emptySet().containsAll(props.keySet()));
        assertTrue(emptySet().containsAll(props.entrySet()));
    }

    @Test
    public void getSpecificSiteToolProperties() throws NrgServiceException {
        final Properties compareProps = new Properties();
        compareProps.setProperty("Preference1", "Value1_1");
        compareProps.setProperty("Preference3", "Value3_1");
        List<String> requestList = Lists.newArrayList("Preference1", "Preference3");
        final Properties props = _prefService.getToolProperties("tool2", Scope.Site, "", requestList);
        assertEquals(compareProps.size(), props.size());
        assertTrue(compareProps.keySet().containsAll(props.keySet()));
        assertTrue(compareProps.entrySet().containsAll(props.entrySet()));
    }

    @Test
    public void getSpecificProjectToolPropertiesWithStoredProjectPreference() throws NrgServiceException {
        final Properties compareProps = new Properties();
        compareProps.setProperty("Preference1", "Value1_2");
        final List<String> requestList = Lists.newArrayList("Preference1", "Preference3");
        final Properties props = _prefService.getToolProperties("tool2", Scope.Project, "project1", requestList);
        assertEquals(compareProps.size(), props.size());
        assertTrue(compareProps.keySet().containsAll(props.keySet()));
        assertTrue(compareProps.entrySet().containsAll(props.entrySet()));
    }

    @Test
    public void getSpecificProjectToolPropertiesWithNoProjectPreference() throws NrgServiceException {
        final List<String> requestList = Lists.newArrayList("Preference1", "Preference3");
        final Properties props = _prefService.getToolProperties("tool2", Scope.Project, "project2", requestList);
        assertEquals(0, props.size());
        assertTrue(emptySet().containsAll(props.keySet()));
        assertTrue(emptySet().containsAll(props.entrySet()));
    }

    private static final Logger _log = LoggerFactory.getLogger(NrgPreferenceServiceTests.class);

    @Inject
    private ToolService          _toolService;
    @Inject
    private NrgPreferenceService _prefService;
}
