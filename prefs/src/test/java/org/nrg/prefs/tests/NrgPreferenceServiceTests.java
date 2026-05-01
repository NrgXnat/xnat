/*
 * prefs: org.nrg.prefs.tests.NrgPreferenceServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.constants.Scope;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Slf4j
public class NrgPreferenceServiceTests {
    private static final String      TOOL_ID_2           = "tool2";
    private static final String      TOOL_NAME_2         = "Tool 2";
    private static final String      ENTITY_ID_NONE      = "";
    private static final String      ENTITY_ID_PROJECT_1 = "project1";
    private static final String      PREFERENCE_ID_1     = "Preference1";
    private static final String      PREFERENCE_ID_2     = "Preference2";
    private static final String      PREFERENCE_ID_3     = "Preference3";
    private static final String      PREFERENCE_ID_4     = "Preference4";
    private static final String      VALUE_1_1           = "Value1_1";
    private static final String      VALUE_2_1           = "Value2_1";
    private static final String      VALUE_3_1           = "Value3_1";
    private static final String      VALUE_1_2           = "Value1_2";
    private static final String      VALUE_2_2           = "Value2_2";
    private static final String      VALUE_4_2           = "Value4_2";
    private static final Set<String> COMPARE_SET_1       = Stream.of(PREFERENCE_ID_1, PREFERENCE_ID_2, PREFERENCE_ID_3).collect(Collectors.toSet());
    private static final Set<String> COMPARE_SET_2       = Stream.of(PREFERENCE_ID_1, PREFERENCE_ID_2, PREFERENCE_ID_4).collect(Collectors.toSet());

    private final ToolService          _toolService;
    private final NrgPreferenceService _prefService;
    private final PreferenceService    _preferenceService;

    @Autowired
    public NrgPreferenceServiceTests(final ToolService toolService, final NrgPreferenceService prefService, final PreferenceService preferenceService) {
        _toolService       = toolService;
        _prefService       = prefService;
        _preferenceService = preferenceService;
    }

    @BeforeEach
    public void initialize() {
        log.info("Initializing test class");
        final Tool tool = _toolService.newEntity();
        tool.setToolId(TOOL_ID_2);
        tool.setToolName(TOOL_NAME_2);
        _toolService.create(tool);

        _prefService.create(TOOL_ID_2, PREFERENCE_ID_1, Scope.Site, ENTITY_ID_NONE, VALUE_1_1);
        _prefService.create(TOOL_ID_2, PREFERENCE_ID_2, Scope.Site, ENTITY_ID_NONE, VALUE_2_1);
        _prefService.create(TOOL_ID_2, PREFERENCE_ID_3, Scope.Site, ENTITY_ID_NONE, VALUE_3_1);

        _prefService.create(TOOL_ID_2, PREFERENCE_ID_1, Scope.Project, ENTITY_ID_PROJECT_1, VALUE_1_2);
        _prefService.create(TOOL_ID_2, PREFERENCE_ID_2, Scope.Project, ENTITY_ID_PROJECT_1, VALUE_2_2);
        _prefService.create(TOOL_ID_2, PREFERENCE_ID_4, Scope.Project, ENTITY_ID_PROJECT_1, VALUE_4_2);
    }

    @AfterEach
    public void teardown() {
        log.info("Tearing down test class");
        _preferenceService.getAll().forEach(_preferenceService::delete);
        _toolService.getAll().forEach(_toolService::delete);
    }

    @Test
    public void getSitePreference() {
        final Preference retrievedPreference1 = _prefService.getPreference(TOOL_ID_2, PREFERENCE_ID_1, Scope.Site, ENTITY_ID_NONE);
        assertThat(retrievedPreference1).isNotNull()
                                        .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_ID_1)
                                        .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, VALUE_1_1)
                                        .extracting(Preference::getTool)
                                        .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_2)
                                        .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_2);

        final Preference retrievedPreference2 = _prefService.getPreference(TOOL_ID_2, PREFERENCE_ID_1);
        assertThat(retrievedPreference2).isNotNull()
                                        .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_ID_1)
                                        .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, VALUE_1_1)
                                        .extracting(Preference::getTool)
                                        .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_2)
                                        .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_2);
    }

    @Test
    public void getProjectPreferenceWithStoredProjectPreference() {
        final Preference retrievedPreference = _prefService.getPreference(TOOL_ID_2, PREFERENCE_ID_1, Scope.Project, ENTITY_ID_PROJECT_1);
        AssertionsForClassTypes.assertThat(retrievedPreference).isNotNull()
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_ID_1)
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, VALUE_1_2)
                               .extracting(Preference::getTool)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_2)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_2);
    }

    @Test
    public void getProjectPreferenceWithStoredProjectPreferenceButNotThisOne() {
        final Preference retrievedPreference = _prefService.getPreference(TOOL_ID_2, PREFERENCE_ID_3, Scope.Project, ENTITY_ID_PROJECT_1);
        AssertionsForClassTypes.assertThat(retrievedPreference).isNotNull()
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_ID_3)
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, VALUE_3_1)
                               .extracting(Preference::getTool)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_2)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_2);
    }

    @Test
    public void getProjectPreferenceWithNoProjectPreference() {
        final Preference retrievedPreference = _prefService.getPreference(TOOL_ID_2, PREFERENCE_ID_1, Scope.Project, "project2");
        AssertionsForClassTypes.assertThat(retrievedPreference).isNotNull()
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_NAME, PREFERENCE_ID_1)
                               .hasFieldOrPropertyWithValue(Preference.PROPERTY_VALUE, VALUE_1_1)
                               .extracting(Preference::getTool)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_ID, TOOL_ID_2)
                               .hasFieldOrPropertyWithValue(Tool.PROPERTY_TOOL_NAME, TOOL_NAME_2);
    }

    @Test
    public void getSitePreferenceValue() {
        final String prefValue1 = _prefService.getPreferenceValue(TOOL_ID_2, PREFERENCE_ID_1, Scope.Site, ENTITY_ID_NONE);
        assertThat(prefValue1).isNotNull()
                              .isNotBlank()
                              .isEqualTo(VALUE_1_1);

        final String prefValue2 = _prefService.getPreferenceValue(TOOL_ID_2, PREFERENCE_ID_1);
        assertThat(prefValue2).isNotNull()
                              .isNotBlank()
                              .isEqualTo(VALUE_1_1);
    }

    @Test
    public void getProjectPreferenceValueWithStoredProjectPreference() {
        final String prefValue = _prefService.getPreferenceValue(TOOL_ID_2, PREFERENCE_ID_1, Scope.Project, ENTITY_ID_PROJECT_1);
        assertThat(prefValue).isNotNull()
                             .isNotBlank()
                             .isEqualTo(VALUE_1_2);
    }

    @Test
    public void getProjectPreferenceValueWithNoProjectPreference() {
        final String prefValue = _prefService.getPreferenceValue(TOOL_ID_2, PREFERENCE_ID_1, Scope.Project, "project2");
        assertThat(prefValue).isNotNull()
                             .isNotBlank()
                             .isEqualTo(VALUE_1_1);
    }

    @Test
    public void getSiteToolPropertyNames() {
        final Set<String> toolNames = _prefService.getToolPropertyNames(TOOL_ID_2, Scope.Site, ENTITY_ID_NONE);
        assertThat(toolNames).isNotNull()
                             .isNotEmpty()
                             .hasSize(3)
                             .containsAll(COMPARE_SET_1);
    }

    @Test
    public void getProjectToolPropertyNamesWithStoredProjectPreference() {
        final Set<String> toolNames = _prefService.getToolPropertyNames(TOOL_ID_2, Scope.Project, ENTITY_ID_PROJECT_1);
        assertThat(toolNames)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .containsAll(COMPARE_SET_2);
    }

    @Test
    public void getProjectToolPropertyNamesWithNoProjectPreference() {
        final Set<String> toolNames = _prefService.getToolPropertyNames(TOOL_ID_2, Scope.Project, "project2");
        assertThat(toolNames)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void getSiteToolProperties() {
        Properties compareProps = new Properties();
        compareProps.setProperty(PREFERENCE_ID_1, VALUE_1_1);
        compareProps.setProperty(PREFERENCE_ID_2, VALUE_2_1);
        compareProps.setProperty(PREFERENCE_ID_3, VALUE_3_1);

        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Site, ENTITY_ID_NONE);
        assertThat(props).isNotNull()
                         .isNotEmpty()
                         .hasSize(3)
                         .containsExactlyInAnyOrderEntriesOf(compareProps);
    }

    @Test
    public void getProjectToolPropertiesWithStoredProjectPreference() {
        final Properties compareProps = new Properties();
        compareProps.setProperty(PREFERENCE_ID_1, VALUE_1_2);
        compareProps.setProperty(PREFERENCE_ID_2, VALUE_2_2);
        compareProps.setProperty(PREFERENCE_ID_4, VALUE_4_2);

        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Project, ENTITY_ID_PROJECT_1);
        assertThat(props).isNotNull()
                         .isNotEmpty()
                         .hasSize(3)
                         .containsExactlyInAnyOrderEntriesOf(compareProps);
    }

    @Test
    public void getProjectToolPropertiesWithNoProjectPreference() {
        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Project, "project2");
        assertThat(props).isNotNull()
                         .isEmpty();
    }

    @Test
    public void getSpecificSiteToolProperties() {
        final Properties compareProps = new Properties();
        compareProps.setProperty(PREFERENCE_ID_1, VALUE_1_1);
        compareProps.setProperty(PREFERENCE_ID_3, VALUE_3_1);

        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Site, ENTITY_ID_NONE, Arrays.asList(PREFERENCE_ID_1, PREFERENCE_ID_3));
        assertThat(props).isNotNull()
                         .isNotEmpty()
                         .hasSize(2)
                         .containsAllEntriesOf(compareProps);
    }

    @Test
    public void getSpecificProjectToolPropertiesWithStoredProjectPreference() {
        final Properties compareProps = new Properties();
        compareProps.setProperty(PREFERENCE_ID_1, VALUE_1_2);

        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Project, ENTITY_ID_PROJECT_1, Arrays.asList(PREFERENCE_ID_1, PREFERENCE_ID_3));
        assertThat(props).isNotNull()
                         .isNotEmpty()
                         .hasSize(1)
                         .containsAllEntriesOf(compareProps);
    }

    @Test
    public void getSpecificProjectToolPropertiesWithNoProjectPreference() {
        final Properties props = _prefService.getToolProperties(TOOL_ID_2, Scope.Project, "project2", Arrays.asList(PREFERENCE_ID_1, PREFERENCE_ID_3));
        assertThat(props).isNotNull().isEmpty();
    }
}
