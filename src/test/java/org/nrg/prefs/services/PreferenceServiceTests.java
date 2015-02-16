/*
 * org.nrg.ddict.services.TestDataDictionaryServices
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 10:44 AM
 */

package org.nrg.prefs.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPrefsService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
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
        Tool tool = _toolService.newEntity();
        tool.setToolId("tool1");
        tool.setToolName("Tool 1");
        _toolService.create(tool);
        List<Tool> tools = _toolService.getAll();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("tool1", tools.get(0).getToolId());
        assertEquals("Tool 1", tools.get(0).getToolName());

        Preference preference = _prefService.newEntity();
        preference.setTool(tool);
        preference.setName("Preference 1");
        preference.setValue("Value 1");
        _prefService.create(preference);
        List<Preference> preferences = _prefService.getAll();
        assertNotNull(preferences);
        assertEquals(1, preferences.size());
        assertEquals(tool, preference.getTool());
        assertEquals("Preference 1", preferences.get(0).getName());
        assertEquals("Value 1", preferences.get(0).getValue());
    }

    private static final Logger _log = LoggerFactory.getLogger(PreferenceServiceTests.class);

    @Inject
    private ToolService _toolService;
    @Inject
    private PreferenceService _prefService;
}
