/*
 * prefs: org.nrg.prefs.tests.ToolServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Tests the NRG Hibernate tool service. This is a sanity test of the plumbing for the tool entity management. All
 * end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Slf4j
public class ToolServiceTests {
    public static final String SIMPLE_TOOL_ID          = "simpleTool";
    public static final String SIMPLE_TOOL_NAME        = "Simple Tool";
    public static final String SIMPLE_TOOL_DESCRIPTION = "This is the simplest tool of them all";

    private final ToolService _service;

    @Autowired
    public ToolServiceTests(final ToolService service) {
        _service = service;
    }

    @Test
    public void testSimpleTool() {
        final Tool tool = _service.newEntity();
        tool.setToolId(SIMPLE_TOOL_ID);
        tool.setToolName(SIMPLE_TOOL_NAME);
        tool.setToolDescription(SIMPLE_TOOL_DESCRIPTION);
        _service.create(tool);

        final List<Tool> tools = _service.getToolIds().stream().map(_service::getByToolId).collect(Collectors.toList());
        assertThat(tools).isNotNull().isNotEmpty();

        final Tool retrieved = _service.getByToolId(tool.getToolId());
        assertThat(retrieved).isNotNull()
                             .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, SIMPLE_TOOL_ID)
                             .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, SIMPLE_TOOL_NAME)
                             .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_DESCRIPTION, SIMPLE_TOOL_DESCRIPTION);
    }
}
