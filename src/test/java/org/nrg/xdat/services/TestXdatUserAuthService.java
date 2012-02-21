package org.nrg.xdat.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.entities.XdatUserAuth;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestXdatUserAuthService {
    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testUserAuthCreation() {
    	XdatUserAuth created = _service.newEntity();
    	created.setAuthUser("mike");
    	created.setAuthMethod("LDAP");
    	_service.create(created);
    	
    	XdatUserAuth retrieved = _service.getUserByNameAndAuth(created.getAuthUser(),created.getAuthMethod());
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }
    
    @Test
    @ExpectedException(DataIntegrityViolationException.class)
    public void testConstraints() {
    	XdatUserAuth userAuth1 = _service.newEntity();
        userAuth1.setAuthUser("mmckay");
        userAuth1.setAuthMethod("LDAP");
        userAuth1.setXdatUsername("mike");
        _service.create(userAuth1);
        XdatUserAuth userAuth2 = _service.newEntity();
        userAuth2.setAuthUser("mmckay");
        userAuth2.setAuthMethod("LDAP");
        userAuth2.setXdatUsername("mike");
        _service.create(userAuth2);
    }

    @Test
    public void testExclusionsByScope() {
    	XdatUserAuth userAuth1 = _service.newEntity();
        userAuth1.setAuthMethod("LDAP");
        _service.create(userAuth1);
        XdatUserAuth userAuth2 = _service.newEntity();
        userAuth2.setXdatUsername("mike");
        userAuth2.setAuthUser("mmckay2");
        userAuth2.setAuthMethod("LDAP");
        _service.create(userAuth2);
        XdatUserAuth userAuth3 = _service.newEntity();
        userAuth3.setXdatUsername("mike2");
        userAuth3.setAuthUser("mmckay2");
        userAuth3.setAuthMethod("database");
        _service.create(userAuth3);
        
        XdatUserAuth auth = _service.getUserByNameAndAuth("mmckay2","LDAP");
        assertNotNull(auth);
        assertTrue(auth.getXdatUsername().equals("mike"));
        XdatUserAuth auth2 = _service.getUserByNameAndAuth("mmckay2","database");
        assertNotNull(auth2);
        assertTrue(auth2.getXdatUsername().equals("mike2"));
        
        XdatUserAuth auth3 = _service.getUserByNameAndAuth("doesn't","exist");
        assertNull(auth3);
        XdatUserAuth auth4 = _service.getUserByNameAndAuth("doesn't","LDAP");
        assertNull(auth4);
        XdatUserAuth auth5 = _service.getUserByNameAndAuth("mmckay2","exist");
        assertNull(auth5);
    }

    @Inject
    private XdatUserAuthService _service;
}
