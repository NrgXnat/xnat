/*
 * core: org.nrg.xdat.security.helpers.TestUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestUsersConfig;
import org.nrg.xdat.configuration.mocks.MockUser;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUsersConfig.class)
public class TestUsers {
    // Tests matching encoded legacy (SHA-256) encoded passwords.
    private static final String LEGACY_ENCODED_PASSWORD = "3185b257d189ae486cbcec68d7e3f698e714860682c2573b8e5182bfcb9616ce{cAIqbuDWDHHSUPvXjpr0nAfQqHgMUahPgF06lkOl2FkrK9FFAuIOKF9CIwWhqi4g}";
    private static final String LEGACY_RAW_PASSWORD     = "admin";

    public TestUsers() {
        _password = Users.createRandomString(32);

        _mockie = new MockUser();
        _mockie.setLogin("mockie");
        _mockie.setPrimaryPassword_encrypt(true);
    }

    @Autowired
    public void setPasswordEncoder(final PasswordEncoder encoder) {
        _encoder = encoder;
        _encoded = _encoder.encode(_password);
    }

    /**
     * Resets the reference user to original password.
     */
    @Before
    public void setup() {
        _mockie.setPassword(_encoded);
    }

    @Test
    public void testPasswordUpdated() throws PasswordComplexityException {
        final UserI user = new MockUser();
        user.setLogin("mockie");
        user.setPassword("newPassword");

        final String password = Users.getUpdatedPassword(_mockie, user);
        assertThat(_encoder.matches("newPassword", password)).isTrue();
    }

    @Test
    public void testLegacyPassword() {
        assertThat(_encoder.matches(LEGACY_RAW_PASSWORD, LEGACY_ENCODED_PASSWORD)).isTrue();
    }

    @Test
    public void testPasswordNotSpecified() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");

        final String password = Users.getUpdatedPassword(_mockie, updated);
        assertThat(_encoder.matches(_password, password)).isTrue();
    }

    @Test
    public void testPasswordNotUpdated() throws PasswordComplexityException {
        final UserI updated = new MockUser();
        updated.setLogin("mockie");
        updated.setPassword(_encoded);

        final String password = Users.getUpdatedPassword(_mockie, updated);
        assertThat(_encoder.matches(_password, password)).isTrue();
    }

    private final UserI  _mockie;
    private final String _password;

    private String          _encoded;
    private PasswordEncoder _encoder;
}
