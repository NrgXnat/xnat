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
import org.nrg.prefs.entities.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the NRG Hibernate tool service. This is a sanity test of the plumbing for the tool entity management. All
 * end-use operations should use an implementation of the {@link NrgPrefsService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ToolServiceTests {
    public ToolServiceTests() {
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
    public void testSimpleTool() throws NrgServiceException {
        final Map<String, String> prefs = new HashMap<>();
        prefs.put("pref1", "value1");
        final Tool tool = _service.newEntity();
        tool.setToolId("tool1");
        tool.setToolName("Tool 1");
        tool.setToolDescription("This is the first tool of them all!");
        tool.setToolPreferences(prefs);
        _service.create(tool);

        final List<Tool> tools = _service.getAll();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("tool1", tools.get(0).getToolId());
        assertEquals("Tool 1", tools.get(0).getToolName());
        assertEquals("This is the first tool of them all!", tool.getToolDescription());
        assertNotNull(tool.getToolPreferences());
        assertEquals(1, tool.getToolPreferences().size());

    }

    private static final Logger _log = LoggerFactory.getLogger(ToolServiceTests.class);

    @Inject
    private ToolService _service;
}
