/*
 * core: org.nrg.xdat.services.TestXdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestXdatUserAuthServiceConfig;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestXdatUserAuthServiceConfig.class)
@Slf4j
public class TestXdatUserAuthService {
    private XdatUserAuthService _service;

    @Autowired
    public void setService(final XdatUserAuthService service) {
        _service = service;
    }

    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testUserAuthCreation() {
        final XdatUserAuth created = _service.newEntity();
        created.setAuthUser("mike");
        created.setAuthMethod("ldap");
        created.setAuthMethodId("wustlkey");
        _service.create(created);

        final UserAuthI retrieved = _service.getUserByNameAndAuth(created.getAuthUser(), created.getAuthMethod(), created.getAuthMethodId());
        assertNotNull(retrieved);

        final boolean hasMike = _service.hasUserByNameAndAuth("mike", "ldap", "wustlkey");
        assertTrue(hasMike);

        assertEquals(created, retrieved);

        _service.delete(created);
        final UserAuthI deleted = _service.retrieve(created.getId());
        assertNull(deleted);

        final boolean hasDeletedMike = _service.hasUserByNameAndAuth("mike", "ldap", "wustlkey");
        assertFalse(hasDeletedMike);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testConstraints() {
        final XdatUserAuth userAuth1 = _service.newEntity();
        userAuth1.setAuthUser("mmckay");
        userAuth1.setAuthMethod("ldap");
        userAuth1.setAuthMethodId("wustlkey");
        userAuth1.setXdatUsername("mike");
        _service.create(userAuth1);

        final XdatUserAuth userAuth2 = _service.newEntity();
        userAuth2.setAuthUser("mmckay");
        userAuth2.setAuthMethod("ldap");
        userAuth2.setAuthMethodId("wustlkey");
        userAuth2.setXdatUsername("mike");
        _service.create(userAuth2);
    }
}
