package org.nrg.xdat.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.configuration.TestUserRoleServiceConfig;
import org.nrg.xdat.entities.UserRole;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUserRoleServiceConfig.class)
public class TestUserRoleService {
    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCreation() throws NrgServiceException {
    	UserRole created = _service.newEntity();
    	created.setUsername("key1");
    	created.setRole("name1");
    	_service.create(created);
    	
    	UserRole retrieved = _service.findUserRole("key1", "name1");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }

    @Test
    public void testReCreation() throws NrgServiceException {
    	UserRole created = _service.newEntity();
    	created.setUsername("key2");
    	created.setRole("name2");
    	_service.create(created);
    	
    	UserRole retrieved = _service.findUserRole("key2", "name2");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
        
        created = _service.newEntity();
    	created.setUsername("key2");
    	created.setRole("name2");
    	_service.create(created);
    	
    	retrieved = _service.findUserRole("key2", "name2");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }

    @Inject
    private UserRoleService _service;
}
