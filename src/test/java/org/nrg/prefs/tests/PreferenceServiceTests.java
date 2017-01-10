/*
 * prefs: org.nrg.prefs.tests.PreferenceServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
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

import static org.junit.Assert.*;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Rollback
@Transactional
public class PreferenceServiceTests {
    public PreferenceServiceTests() {
        _log.info("Creating test class");
    }

    @Before
    public void initialize() {
        _log.info("Initializing test class");
    }

    @After
    public void teardown() {
        _log.info("Tearing down test class");
    }

    @Test
    public void testSimplePreference() throws NrgServiceException {
        final Tool tool = _toolService.newEntity();
        tool.setToolId("tool1");
        tool.setToolName("Tool 1");
        _toolService.create(tool);

        final List<Tool> tools = _toolService.getAll();
        final Tool retrieved = _toolService.retrieve(tool.getId());
        assertNotNull(tools);
        assertNotNull(retrieved);
        assertEquals("tool1", retrieved.getToolId());
        assertEquals("Tool 1", retrieved.getToolName());

        final Preference preference = _prefService.newEntity();
        preference.setTool(tool);
        preference.setName("Preference 1");
        preference.setValue("Value 1");
        _prefService.create(preference);

        final List<Preference> all = _prefService.getAll();
        final Preference retrievedPreference = _prefService.retrieve(preference.getId());
        assertNotNull(all);
        assertEquals(tool, retrievedPreference.getTool());
        assertEquals("Preference 1", retrievedPreference.getName());
        assertEquals("Value 1", retrievedPreference.getValue());

        final Properties properties = _prefService.getToolProperties("tool1", EntityId.Default.getScope(), EntityId.Default.getEntityId());
        assertNotNull(properties);
        assertTrue(properties.containsKey("Preference 1"));
        assertEquals("Value 1", properties.getProperty("Preference 1"));
    }

    private static final Logger _log = LoggerFactory.getLogger(PreferenceServiceTests.class);

    @Inject
    private ToolService       _toolService;
    @Inject
    private PreferenceService _prefService;
}
