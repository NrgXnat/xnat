/*
 * core: org.nrg.xdat.services.TestAliasTokenService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.configuration.TestAliasTokenServiceConfig;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.EventMetaI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestAliasTokenServiceConfig.class)
public class TestAliasTokenService {
    private static final Map<String, ?> ADMIN = createUser("admin", "Admin", "Istrator", "admin@xnat.org", "admin", false);
    private static final Map<String, ?> USER  = createUser("user", "Normal", "User", "user@xnat.org", "user", false);
    private static final Map<String, ?> GUEST = createUser(Users.DEFAULT_GUEST_USERNAME, "Guest", "User", "info@xnat.org", Users.DEFAULT_GUEST_USERNAME, true);

    private AliasTokenService      _aliasTokenService;
    private UserManagementServiceI _userService;
    private SerializerService      _serializer;

    @Autowired
    public void setAliasTokenService(final AliasTokenService aliasTokenService) {
        _aliasTokenService = aliasTokenService;
    }

    @Autowired
    public void setUserService(final UserManagementServiceI userService) {
        _userService = userService;
    }

    @Autowired
    public void setSerializer(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Before
    public void setup() throws Exception {
        if (!_userService.exists("admin")) {
            _userService.save(_userService.createUser(ADMIN), null, true, (EventMetaI) null);
        }
        if (!_userService.exists("user")) {
            _userService.save(_userService.createUser(USER), null, true, (EventMetaI) null);
        }
        if (_userService.getGuestUser() == null) {
            _userService.save(_userService.createUser(GUEST), null, true, (EventMetaI) null);
        }
    }

    @Test
    public void testStandardTokenOperations() throws Exception {
        final AliasToken token1 = _aliasTokenService.issueTokenForUser("admin");
        assertNotNull(token1);
        assertNotEquals("", token1.getAlias());
        assertNotEquals("", token1.getSecret());
        assertNotNull(token1.getEstimatedExpirationTime());
        assertNull(token1.getValidIPAddresses());

        final String userId = _aliasTokenService.validateToken(token1.getAlias(), token1.getSecret());
        assertNotNull(userId);
        assertEquals("admin", userId);

        _aliasTokenService.invalidateToken(token1);
        final String notUserId = _aliasTokenService.validate(token1);
        assertNull(notUserId);
    }

    @Test
    public void testSerializeToken() throws Exception {
        final AliasToken token = _aliasTokenService.issueTokenForUser("user");
        assertNotNull(token);
        assertNotEquals("", token.getAlias());
        assertNotEquals("", token.getSecret());
        assertNotNull(token.getEstimatedExpirationTime());
        final String json = _serializer.toJson(token);
        assertTrue(StringUtils.isNotBlank(json));
        final AliasToken deserialized = _serializer.deserializeJson(json, AliasToken.class);
        assertNotNull(deserialized);
        assertEquals(token, deserialized);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testDuplicateConstraint() {
        final AliasToken token1 = _aliasTokenService.newEntity();
        token1.setXdatUserId("user");
        _aliasTokenService.create(token1);

        final AliasToken token2 = _aliasTokenService.newEntity();
        token2.setXdatUserId("user");
        token2.setAlias(token1.getAlias());
        token2.setSecret(token1.getSecret());
        _aliasTokenService.create(token2);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testXdatUserIdNotNullConstraint() {
        final AliasToken token = _aliasTokenService.newEntity();
        _aliasTokenService.create(token);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAliasNotNullConstraint() {
        final AliasToken token = _aliasTokenService.newEntity();
        token.setXdatUserId("admin");
        token.setAlias(null);
        _aliasTokenService.create(token);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testSecretNotNullConstraint() {
        final AliasToken token = _aliasTokenService.newEntity();
        token.setXdatUserId("admin");
        token.setSecret(null);
        _aliasTokenService.create(token);
    }

    private static Map<String, ?> createUser(final String username, final String firstName, final String lastName, final String email, final String password, final boolean isGuest) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put(Users.DEFAULT_GUEST_USERNAME, isGuest);
        properties.put("firstname", firstName);
        properties.put("lastname", lastName);
        properties.put("email", email);
        properties.put("dBName", null);
        properties.put("password", password);
        properties.put("enabled", true);
        properties.put("verified", true);
        properties.put("active", true);
        return properties;
    }
}
