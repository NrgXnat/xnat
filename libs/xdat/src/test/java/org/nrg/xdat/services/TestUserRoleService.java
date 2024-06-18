/*
 * core: org.nrg.xdat.services.TestUserRoleService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestUserRoleServiceConfig;
import org.nrg.xdat.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUserRoleServiceConfig.class)
public class TestUserRoleService {
    @Autowired
    public void setUserRoleService(final UserRoleService service) {
        _service = service;
    }

    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCreation() {
        final UserRole created = _service.newEntity();
        created.setUsername("key1");
        created.setRole("name1");
        _service.create(created);

        final UserRole retrieved = _service.findUserRole("key1", "name1");
        assertNotNull(retrieved);
        assertEquals(created, retrieved);

        _service.delete(created);
        final UserRole deleted = _service.retrieve(created.getId());
        assertNull(deleted);
    }

    @Test
    public void testReCreation() {
        final UserRole created1 = _service.newEntity();
        created1.setUsername("key2");
        created1.setRole("name2");
        _service.create(created1);

        final UserRole retrieved1 = _service.findUserRole("key2", "name2");
        assertNotNull(retrieved1);
        assertEquals(created1, retrieved1);

        _service.delete(created1);
        final UserRole deleted1 = _service.retrieve(created1.getId());
        assertNull(deleted1);

        final UserRole created2 = _service.newEntity();
        created2.setUsername("key2");
        created2.setRole("name2");
        _service.create(created2);

        final UserRole retrieved2 = _service.findUserRole("key2", "name2");
        assertNotNull(retrieved2);
        assertEquals(created2, retrieved2);

        _service.delete(created2);
        final UserRole deleted2 = _service.retrieve(created2.getId());
        assertNull(deleted2);
    }

    private UserRoleService _service;
}
