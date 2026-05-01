/*
 * prefs: org.nrg.prefs.tests.PreferenceServiceTests
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
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Slf4j
public class PreferenceServiceTests {
    public static final String            TOOL_ID_1          = "tool1";
    public static final String            TOOL_NAME_1        = "Tool 1";
    public static final String            PREFERENCE_NAME_1  = "Preference 1";
    public static final String            PREFERENCE_VALUE_1 = "Value 1";

    private final       ToolService       _toolService;
    private final       PreferenceService _prefService;

    @Autowired
    public PreferenceServiceTests(final ToolService toolService, final PreferenceService prefService) {
        _toolService = toolService;
        _prefService = prefService;
    }

    @Test
    public void testSimplePreference() {
        final Tool tool = _toolService.newEntity();
        tool.setToolId(TOOL_ID_1);
        tool.setToolName(TOOL_NAME_1);
        _toolService.create(tool);

        final List<Tool> tools     = _toolService.getAll();
        final Tool       retrieved = _toolService.retrieve(tool.getId());
        assertThat(tools).isNotNull().isNotEmpty();
        assertThat(retrieved).isNotNull()
                             .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_1)
                             .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_1);

        final Preference preference = _prefService.newEntity();
        preference.setTool(tool);
        preference.setName(PREFERENCE_NAME_1);
        preference.setValue(PREFERENCE_VALUE_1);
        _prefService.create(preference);

        final List<Preference> all                 = _prefService.getAll();
        final Preference       retrievedPreference = _prefService.retrieve(preference.getId());
        assertThat(all).isNotNull().isNotEmpty();
        assertThat(retrievedPreference).isNotNull()
                                       .hasFieldOrPropertyWithValue(Preference.PROPERTY_TOOL, tool)
                                       .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_NAME_1)
                                       .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, PREFERENCE_VALUE_1);

        final Properties properties = _prefService.getToolProperties(TOOL_ID_1, EntityId.Default.getScope(), EntityId.Default.getEntityId());
        assertThat(properties).isNotNull()
                              .isNotEmpty()
                              .containsEntry(PREFERENCE_NAME_1, PREFERENCE_VALUE_1);
    }
}
