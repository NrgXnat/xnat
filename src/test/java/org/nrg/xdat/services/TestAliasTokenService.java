/*
 * core: org.nrg.xdat.services.TestXdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.configuration.TestAliasTokenServiceConfig;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.EventMetaI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestAliasTokenServiceConfig.class)
public class TestAliasTokenService {

    private static final Map<String, ?> ADMIN = createUser("admin", "Admin", "User", "admin@xnat.org", "admin", true);
    private static final Map<String, ?> USER  = createUser("user", "Normal", "User", "user@xnat.org", "user", true);
    private static final Map<String, ?> GUEST = createUser("guest", "Guest", "User", "info@xnat.org", "guest", true);

    private static Map<String, ?> createUser(final String username, final String firstname, final String lastname, final String email, final String password, final boolean isGuest) {
        return new HashMap<String, Object>() {{
            put("username", username);
            put("guest", isGuest);
            put("firstname", firstname);
            put("lastname", lastname);
            put("email", email);
            put("dBName", null);
            put("password", password);
            put("enabled", true);
            put("verified", true);
            put("salt", UUID.randomUUID());
            put("active", true);
        }};
    }

    @Before
    public void setup() throws Exception {
        if (_userService.getUser("admin") == null) {
            _userService.save(_userService.createUser(ADMIN), null, true, (EventMetaI) null);
        }
        if (_userService.getUser("user") == null) {
            _userService.save(_userService.createUser(USER), null, true, (EventMetaI) null);
        }
        if (_userService.getUser("guest") == null) {
            _userService.save(_userService.createUser(GUEST), null, true, (EventMetaI) null);
        }
    }

    @Test
    public void testStandardTokenOperations() throws Exception {
        final AliasToken token1 = _service.issueTokenForUser("admin");
        assertNotNull(token1);
        assertNotEquals("", token1.getAlias());
        assertNotEquals("", token1.getSecret());
        assertNotNull(token1.getEstimatedExpirationTime());
        assertNull(token1.getValidIPAddresses());

        final String userId = _service.validateToken(token1.getAlias(), token1.getSecret());
        assertNotNull(userId);
        assertEquals("admin", userId);

        _service.invalidateToken(token1);
        final String notUserId = _service.validate(token1);
        assertNull(notUserId);
    }

    @Test
    public void testSerializeToken() throws Exception {
        final AliasToken token = _service.issueTokenForUser("user");
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
            final AliasToken token1 = _service.newEntity();
            token1.setXdatUserId("user");
            _service.create(token1);

            final AliasToken token2 = _service.newEntity();
            token2.setXdatUserId("user");
            token2.setAlias(token1.getAlias());
            token2.setSecret(token1.getSecret());
            _service.create(token2);
    }

    @Test(expected = PropertyValueException.class)
    public void testXdatUserIdNotNullConstraint() {
        final AliasToken token = _service.newEntity();
        _service.create(token);
    }

    @Test(expected = PropertyValueException.class)
    public void testAliasNotNullConstraint() {
        final AliasToken token = _service.newEntity();
        token.setXdatUserId("admin");
        token.setAlias(null);
        _service.create(token);
    }

    @Test(expected = PropertyValueException.class)
    public void testSecretNotNullConstraint() {
        final AliasToken token = _service.newEntity();
        token.setXdatUserId("admin");
        token.setSecret(null);
        _service.create(token);
    }

    @Inject
    private AliasTokenService _service;

    @Inject
    private UserManagementServiceI _userService;

    @Inject
    private SerializerService _serializer;
}
