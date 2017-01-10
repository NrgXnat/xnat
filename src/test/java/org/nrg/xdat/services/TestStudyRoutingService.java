/*
 * core: org.nrg.xdat.services.TestStudyRoutingService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestStudyRoutingServiceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestStudyRoutingServiceConfig.class)
public class TestStudyRoutingService {
    private static final String UID1 = "1.2.3.4.5.6.7";
    private static final String UID2 = "2.3.4.5.6.7.8";
    private static final String UID3 = "3.4.5.6.7.8.9";
    private static final String UID4 = "4.5.6.7.8.9.0";
    private static final String UID5 = "5.6.7.8.9.0.1";
    private static final String PRJ1 = "prj1";
    private static final String PRJ2 = "prj2";
    private static final String USR1 = "user1";
    private static final String USR2 = "user2";
    private static final String USR3 = "user3";

    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testAssignmentLifecycle() {
        boolean success = _service.assign(UID1, PRJ1, USR1);
        assertTrue(success);

        Map<String, String> assignment = _service.findStudyRouting(UID1);
        assertNotNull(assignment);
        assertEquals(PRJ1, assignment.get(StudyRoutingService.PROJECT));
        assertEquals(USR1, assignment.get(StudyRoutingService.USER));
        _service.close(UID1);
        Map<String, String> notFound = _service.findStudyRouting(UID1);
        assertNull(notFound);
    }

    @Test
    public void testLotsOfAssignments() {
        _service.assign(UID2, PRJ1, USR1);
        _service.assign(UID3, PRJ1, USR2);
        _service.assign(UID4, PRJ2, USR2);
        _service.assign(UID5, PRJ1, USR1);
        Map<String, Map<String, String>> assignments = _service.findProjectRoutings(PRJ1);
        assertEquals(3, assignments.size());
        assignments = _service.findProjectRoutings(PRJ2);
        assertEquals(1, assignments.size());
        assignments = _service.findUserRoutings(USR1);
        assertEquals(2, assignments.size());
        assignments = _service.findUserRoutings(USR2);
        assertEquals(2, assignments.size());
        assignments = _service.findUserRoutings(USR3);
        assertEquals(0, assignments.size());
    }

    @Test
    public void testFindByAttribute() {
        _service.assign(UID2, PRJ1, USR1);
        _service.assign(UID3, PRJ1, USR2);
        _service.assign(UID4, PRJ2, USR2);
        _service.assign(UID5, PRJ1, USR1);
        Map<String, Map<String, String>> assignments = _service.findRoutingsByAttribute(StudyRoutingService.PROJECT, PRJ1);
        assertEquals(3, assignments.size());
        assignments = _service.findRoutingsByAttribute(StudyRoutingService.USER, USR1);
        assertEquals(2, assignments.size());
    }

    @Inject
    private StudyRoutingService _service;
}
