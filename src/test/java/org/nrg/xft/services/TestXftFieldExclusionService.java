package org.nrg.xft.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xft.entities.XftFieldExclusion;
import org.nrg.xft.entities.XftFieldExclusionScope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestXftFieldExclusionService {
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
        assertTrue(retrieved == null);
    }

    @Test
    @ExpectedException(DataIntegrityViolationException.class)
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
        
        List<XftFieldExclusion> system_excls = _service.getSystemExclusions();
        List<XftFieldExclusion> target_excls = _service.getExclusionsForScopedTarget(XftFieldExclusionScope.Project, "exclusion1");
        assertNotNull(system_excls);
        assertTrue(system_excls.contains(exclusion1));
        assertNotNull(target_excls);
        assertEquals(2, target_excls.size());
        assertTrue(target_excls.contains(exclusion2));
        assertTrue(target_excls.contains(exclusion3));
    }

    @Inject
    private XftFieldExclusionService _service;
}
