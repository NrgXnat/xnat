/*
 * org.nrg.prefs.tests.ToolServiceTests
 *
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 */
package org.nrg.prefs.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.entities.PreferenceInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the NRG Hibernate tool service. This is a sanity test of the plumbing for the tool entity management. All
 * end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Rollback
@Transactional
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
        final Map<String, PreferenceInfo> prefs = new HashMap<>();
        prefs.put("pref1", new PreferenceInfo("pref1", "value1"));
        final Tool tool = _service.newEntity();
        tool.setToolId("tool1");
        tool.setToolName("Tool 1");
        tool.setToolDescription("This is the first tool of them all!");
        _service.create(tool);

        final List<Tool> tools = _service.getAll();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("tool1", tools.get(0).getToolId());
        assertEquals("Tool 1", tools.get(0).getToolName());
        assertEquals("This is the first tool of them all!", tools.get(0).getToolDescription());
//        assertNotNull(tools.get(0).getToolPreferences());
//        assertEquals(1, tools.get(0).getToolPreferences().size());
    }

    private static final Logger _log = LoggerFactory.getLogger(ToolServiceTests.class);

    @Inject
    private ToolService _service;
}
