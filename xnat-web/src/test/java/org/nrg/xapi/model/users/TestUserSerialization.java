/*
 * web: org.nrg.xapi.model.users.TestUserSerialization
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUserSerializationConfig.class)
public class TestUserSerialization {
    @Autowired
    public void setSerializer(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    public void testSecuredProperties() throws IOException {
        final User secured = new User();
        secured.setUsername("name");
        secured.setEmail("foo@bar.com");
        secured.setPassword("password");
        secured.setSalt("salt");
        secured.setEnabled(true);
        secured.setSecured(true);

        // Users are unsecured by default when created by default constructor.
        final User unsecured = new User();
        unsecured.setUsername("name");
        unsecured.setEmail("foo@bar.com");
        unsecured.setPassword("password");
        unsecured.setSalt("salt");
        unsecured.setEnabled(true);

        final String secureJson = _serializer.toJson(secured);
        assertNotNull(secureJson);
        assertTrue(StringUtils.isNotBlank(secureJson));
        final String unsecureJson = _serializer.toJson(unsecured);
        assertNotNull(unsecureJson);
        assertTrue(StringUtils.isNotBlank(unsecureJson));

        // Here's where we make sure the password and salt aren't serialized.
        final JsonNode securedMap = _serializer.deserializeJson(secureJson);
        assertNotNull(securedMap);
        assertTrue(securedMap.has("username"));
        assertTrue(securedMap.has("email"));
        assertFalse(securedMap.has("password"));
        assertFalse(securedMap.has("salt"));
        assertTrue(securedMap.has("enabled"));
        assertFalse(securedMap.has("verified"));

        // Here's where we make sure the password and salt ARE serialized.
        final JsonNode unsecuredMap = _serializer.deserializeJson(unsecureJson);
        assertNotNull(unsecuredMap);
        assertTrue(unsecuredMap.has("username"));
        assertTrue(unsecuredMap.has("email"));
        assertTrue(unsecuredMap.has("password"));
        assertTrue(unsecuredMap.has("salt"));
        assertTrue(unsecuredMap.has("enabled"));
        assertFalse(unsecuredMap.has("verified"));

        final User securedOutput = _serializer.deserializeJson(secureJson, User.class);
        assertNotNull(securedOutput);
        assertTrue(StringUtils.isNotBlank(securedOutput.getUsername()));
        assertTrue(StringUtils.isNotBlank(securedOutput.getEmail()));
        assertTrue(StringUtils.isBlank(securedOutput.getPassword()));
        assertTrue(StringUtils.isBlank(securedOutput.getSalt()));
        assertTrue(securedOutput.isEnabled());
        assertNull(securedOutput.isVerified());

        final User unsecuredOutput = _serializer.deserializeJson(unsecureJson, User.class);
        assertNotNull(unsecuredOutput);
        assertTrue(StringUtils.isNotBlank(unsecuredOutput.getUsername()));
        assertTrue(StringUtils.isNotBlank(unsecuredOutput.getEmail()));
        assertTrue(StringUtils.isNotBlank(unsecuredOutput.getPassword()));
        assertTrue(StringUtils.isNotBlank(unsecuredOutput.getSalt()));
        assertTrue(unsecuredOutput.isEnabled());
        assertNull(unsecuredOutput.isVerified());
    }

    private SerializerService _serializer;
}
