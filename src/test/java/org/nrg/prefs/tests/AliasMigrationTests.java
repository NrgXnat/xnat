/*
 * org.nrg.prefs.tests.PreferenceServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.prefs.configuration.AliasMigrationTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean.*;

/**
 * Tests the NRG Hibernate preference service. This is a sanity test of the plumbing for the preference entity
 * management. All end-use operations should use an implementation of the {@link NrgPreferenceService} interface.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AliasMigrationTestsConfiguration.class)
@Rollback
@Transactional
public class AliasMigrationTests {
    public AliasMigrationTests() {
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
    public void testMigratedPrefs() {
        // The SQL initialization script creates preferences with alias names, but the start-up will migrate them, so
        // this will return null.
        final Preference retrieveByAliasA = _prefService.getPreference(TOOL_ID, PREF_A_ALIAS);
        assertNull(retrieveByAliasA);
        final Preference retrieveByAliasC = _prefService.getPreference(TOOL_ID, PREF_C_ALIAS);
        assertNull(retrieveByAliasC);
        
        // But the import values will be there, not the default preference value. This is what tells us that these
        // preferences were initialized in the database BEFORE the preference bean was created, since the bean default
        // values are not being used.
        final Preference retrievedA = _prefService.getPreference(TOOL_ID, PREF_A);
        assertNotNull(retrievedA);
        assertNotEquals(PREF_A_VALUE, retrievedA.getValue());
        assertEquals(PREF_A_IMPORT_VALUE, retrievedA.getValue());
        final Preference retrievedB = _prefService.getPreference(TOOL_ID, PREF_B);
        assertNotNull(retrievedB);
        assertNotEquals(PREF_B_VALUE, retrievedB.getValue());
        assertEquals(PREF_B_IMPORT_VALUE, retrievedB.getValue());
        final Preference retrievedC = _prefService.getPreference(TOOL_ID, PREF_C);
        assertNotNull(retrievedC);
        assertNotEquals(PREF_C_VALUE, retrievedC.getValue());
        assertEquals(PREF_C_IMPORT_VALUE, retrievedC.getValue());
    }

    private static final Logger _log = LoggerFactory.getLogger(AliasMigrationTests.class);

    @Autowired
    private PreferenceService                    _prefService;
}
