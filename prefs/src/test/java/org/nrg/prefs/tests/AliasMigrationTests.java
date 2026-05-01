/*
 * prefs: org.nrg.prefs.tests.AliasMigrationTests
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
import org.nrg.prefs.configuration.AliasMigrationTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_A;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_A_ALIAS;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_A_IMPORT_VALUE;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_B;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_B_IMPORT_VALUE;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_C;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_C_ALIAS;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.PREF_C_IMPORT_VALUE;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.TOOL_ID;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AliasMigrationTestsConfiguration.class)
@Slf4j
public class AliasMigrationTests {
    private final PreferenceService _prefService;

    @Autowired
    public AliasMigrationTests(final PreferenceService prefService) {
        _prefService = prefService;
    }

    @Test
    public void testMigratedPrefs() {
        // The SQL initialization script creates preferences with alias names, but the start-up will migrate them, so
        // this will return null.
        final Preference retrieveByAliasA = _prefService.getPreference(TOOL_ID, PREF_A_ALIAS);
        assertThat(retrieveByAliasA).isNull();
        final Preference retrieveByAliasC = _prefService.getPreference(TOOL_ID, PREF_C_ALIAS);
        assertThat(retrieveByAliasC).isNull();

        // But the import values will be there, not the default preference value. This is what tells us that these
        // preferences were initialized in the database BEFORE the preference bean was created, since the bean default
        // values are not being used.
        final Preference retrievedA = _prefService.getPreference(TOOL_ID, PREF_A);
        assertThat(retrievedA).isNotNull()
                              .hasFieldOrPropertyWithValue("value", PREF_A_IMPORT_VALUE);

        final Preference retrievedB = _prefService.getPreference(TOOL_ID, PREF_B);
        assertThat(retrievedB).isNotNull()
                              .hasFieldOrPropertyWithValue("value", PREF_B_IMPORT_VALUE);

        final Preference retrievedC = _prefService.getPreference(TOOL_ID, PREF_C);
        assertThat(retrievedC).isNotNull()
                              .hasFieldOrPropertyWithValue("value", PREF_C_IMPORT_VALUE);
    }
}
