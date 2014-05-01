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
import org.nrg.prefs.entities.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the NRG preferences service framework.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PreferencesServiceTests {
    public PreferencesServiceTests() {
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
    public void testSimplePreference() {
        Preference preference = _service.newEntity();
        preference.setName("Preference 1");
        _service.create(preference);
        List<Preference> preferences = _service.getAll();
        assertNotNull(preferences);
        assertEquals(1, preferences.size());
        assertEquals("Preference 1", preferences.get(0).getName());
    }

    private static final Logger _log = LoggerFactory.getLogger(PreferencesServiceTests.class);

    @Inject
    private PreferencesService _service;
}
