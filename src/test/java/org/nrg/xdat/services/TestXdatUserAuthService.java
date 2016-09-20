/*
 * core: org.nrg.xdat.services.TestXdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestXdatUserAuthServiceConfig;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestXdatUserAuthServiceConfig.class)
public class TestXdatUserAuthService {
    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testUserAuthCreation() {
    	XdatUserAuth created = _service.newEntity();
    	created.setAuthUser("mike");
    	created.setAuthMethod("ldap");
    	created.setAuthMethodId("wustlkey");
    	_service.create(created);
    	
    	UserAuthI retrieved = _service.getUserByNameAndAuth(created.getAuthUser(),created.getAuthMethod(),created.getAuthMethodId());
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }
    
    @Test(expected = ConstraintViolationException.class)
    public void testConstraints() {
    	XdatUserAuth userAuth1 = _service.newEntity();
        userAuth1.setAuthUser("mmckay");
        userAuth1.setAuthMethod("ldap");
        userAuth1.setAuthMethodId("wustlkey");
        userAuth1.setXdatUsername("mike");
        _service.create(userAuth1);
        XdatUserAuth userAuth2 = _service.newEntity();
        userAuth2.setAuthUser("mmckay");
        userAuth2.setAuthMethod("ldap");
        userAuth2.setAuthMethodId("wustlkey");
        userAuth2.setXdatUsername("mike");
        _service.create(userAuth2);
    }

    @Inject
    private XdatUserAuthService _service;
}
