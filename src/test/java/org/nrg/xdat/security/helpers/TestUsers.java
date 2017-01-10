/*
 * core: org.nrg.xdat.security.helpers.TestUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestUsersConfig;
import org.nrg.xdat.configuration.mocks.MockUser;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xft.security.UserAttributes;
import org.nrg.xft.security.UserI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUsersConfig.class)
public class TestUsers {
    public TestUsers() {
        _salt = Users.createNewSalt();
        _password = Users.createRandomString(32);
        _encoded = Users.encode(_password, _salt);

        _mockie = new MockUser();
        _mockie.setLogin("mockie");
        _mockie.setPassword(_encoded);
        _mockie.setSalt(_salt);
        _mockie.setPrimaryPassword_encrypt(true);
    }

    /**
     * Resets the reference user to original password and salt.
     */
    @Before
    public void setup() {
        _mockie.setPassword(_encoded);
        _mockie.setSalt(_salt);
    }

    @Test
    public void testPasswordUpdated() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");
        updated.setPassword("newPassword");

        final Map<UserAttributes, String> values = Users.getUpdatedPassword(_mockie, updated);

        final String password = values.get(UserAttributes.password);
        final String salt     = values.get(UserAttributes.salt);
        final String encoded  = Users.encode("newPassword", salt);

        assertEquals(password, encoded);
        assertNotEquals(password, "newPassword");
        assertNotEquals(password, _password);
        assertNotEquals(salt, _salt);
    }

    @Test
    public void testPasswordNotSpecified() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");

        final Map<UserAttributes, String> values = Users.getUpdatedPassword(_mockie, updated);

        final String password = values.get(UserAttributes.password);
        final String salt     = values.get(UserAttributes.salt);

        assertEquals(password, _encoded);
        assertEquals(salt, _salt);
    }

    @Test
    public void testPasswordNotUpdated() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");
        updated.setPassword(_encoded);
        updated.setSalt(_salt);

        final Map<UserAttributes, String> values = Users.getUpdatedPassword(_mockie, updated);

        final String password = values.get(UserAttributes.password);
        final String salt     = values.get(UserAttributes.salt);

        assertEquals(password, _encoded);
        assertEquals(salt, _salt);
    }

    @Test
    public void testPasswordUpdatedOriginalInPlainText() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");

        _mockie.setPassword(_password);
        _mockie.setSalt(null);

        final Map<UserAttributes, String> values = Users.getUpdatedPassword(_mockie, updated);

        final String password = values.get(UserAttributes.password);
        final String salt     = values.get(UserAttributes.salt);
        final String encoded  = Users.encode(_password, salt);

        assertNotEquals(password, _encoded);
        assertNotEquals(salt, _salt);
        assertEquals(password, encoded);
    }

    private final UserI  _mockie;
    private final String _salt;
    private final String _password;
    private final String _encoded;
}
