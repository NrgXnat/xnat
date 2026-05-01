/*
 * spawner: org.nrg.xnat.spawner.tests.SpawnerServiceFunctionTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.spawner.configuration.SpawnerServiceFunctionTestConfiguration;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.nrg.xnat.spawner.services.impl.constants.TestSpawnerService.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpawnerServiceFunctionTestConfiguration.class)
public class SpawnerServiceFunctionTests {
    private SpawnerService _testSpawnerService;

    @Autowired
    public void setSpawnerService(final SpawnerService testSpawnerService) {
        _testSpawnerService = testSpawnerService;
    }

    @Test
    public void testSimpleResolution() throws CircularReferenceException, InvalidElementIdException {
        final String fetched = _testSpawnerService.resolve(SIMPLE_ID_ROOT);
        assertNotNull(fetched);
        assertEquals(SIMPLE_YAML_RESOLVED, fetched);
    }

    @Test
    public void testComplexResolution() throws CircularReferenceException, InvalidElementIdException {
        final String fetched = _testSpawnerService.resolve(COMPLEX_ID_ROOT);
        assertNotNull(fetched);
        assertEquals(COMPLEX_YAML_RESOLVED, fetched);
    }

    @Test
    public void testSiteInfoResolution() throws CircularReferenceException, InvalidElementIdException {
        final String fetched = _testSpawnerService.resolve(SITE_ADMIN_ID_SITE_INFO);
        assertNotNull(fetched);
        assertEquals(SITE_INFO_YAML_RESOLVED, fetched);
    }
}
