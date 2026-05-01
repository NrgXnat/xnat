/*
 * core: org.nrg.xft.services.TestXftFieldExclusionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.services;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xft.configuration.TestXftFieldExclusionServiceConfig;
import org.nrg.xft.entities.XftFieldExclusion;
import org.nrg.xft.entities.XftFieldExclusionScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestXftFieldExclusionServiceConfig.class)
@Slf4j
public class TestXftFieldExclusionService {
    private XftFieldExclusionService   _service;

    @Autowired
    public void setXftFieldExclusionService(final XftFieldExclusionService service) {
        _service = service;
    }

    @After
    public void teardown() {
        _service.getAll().forEach(_service::delete);
    }

    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCRUDExclusions() {
        XftFieldExclusion created = _service.newEntity();
        created.setScope(XftFieldExclusionScope.Project);
        created.setTargetId("testCRUDExclusions");
        created.setPattern("dob");
        _service.create(created);

        XftFieldExclusion retrieved = _service.retrieve(created.getId());
        assertNotNull(retrieved);
        assertEquals(created, retrieved);

        created.setPattern("xxx");
        _service.update(created);
        retrieved = _service.retrieve(created.getId());
        assertEquals("xxx", retrieved.getPattern());

        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertNull(retrieved);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testConstraints() {
        XftFieldExclusion exclusion1 = _service.newEntity();
        exclusion1.setScope(XftFieldExclusionScope.Project);
        exclusion1.setTargetId("testConstraints");
        exclusion1.setPattern("dob");
        _service.create(exclusion1);
        XftFieldExclusion exclusion2 = _service.newEntity();
        exclusion2.setScope(XftFieldExclusionScope.Project);
        exclusion2.setTargetId("testConstraints");
        exclusion2.setPattern("dob");
        _service.create(exclusion2);
    }

    @Test
    public void testExclusionsByScope() {
        XftFieldExclusion exclusion1 = _service.newEntity();
        exclusion1.setPattern("name");
        _service.create(exclusion1);
        XftFieldExclusion exclusion2 = _service.newEntity();
        exclusion2.setScope(XftFieldExclusionScope.Project);
        exclusion2.setTargetId("exclusion1");
        exclusion2.setPattern("dob");
        _service.create(exclusion2);
        XftFieldExclusion exclusion3 = _service.newEntity();
        exclusion3.setScope(XftFieldExclusionScope.Project);
        exclusion3.setTargetId("exclusion1");
        exclusion3.setPattern("gender");
        _service.create(exclusion3);

        List<XftFieldExclusion> systemExclusions = _service.getSystemExclusions();
        List<XftFieldExclusion> targetExclusions = _service.getExclusionsForScopedTarget(XftFieldExclusionScope.Project, "exclusion1");
        assertThat(systemExclusions).isNotNull()
                                    .isNotEmpty()
                                    .hasSize(1)
                                    .containsExactly(exclusion1);
        assertThat(targetExclusions).isNotNull()
                                    .isNotEmpty()
                                    .hasSize(2)
                                    .containsExactlyInAnyOrder(exclusion2, exclusion3);
    }
}
