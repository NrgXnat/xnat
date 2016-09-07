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
    public void testHiddenProperties() throws IOException {
        final User input = new User();
        input.setUsername("name");
        input.setEmail("foo@bar.com");
        input.setPassword("password");
        input.setSalt("salt");
        input.setAdmin(false);
        input.setEnabled(true);

        final String json = _serializer.toJson(input);
        assertNotNull(json);
        assertTrue(StringUtils.isNotBlank(json));

        // Here's where we make sure the password and salt aren't serialized.
        final JsonNode map = _serializer.deserializeJson(json);
        assertNotNull(map);
        assertTrue(map.has("username"));
        assertTrue(map.has("email"));
        assertFalse(map.has("password"));
        assertFalse(map.has("salt"));
        assertTrue(map.has("admin"));
        assertTrue(map.has("enabled"));
        assertFalse(map.has("verified"));

        final User output = _serializer.deserializeJson(json, User.class);
        assertNotNull(output);
        assertTrue(StringUtils.isNotBlank(output.getUsername()));
        assertTrue(StringUtils.isNotBlank(output.getEmail()));
        assertTrue(StringUtils.isBlank(output.getPassword()));
        assertTrue(StringUtils.isBlank(output.getSalt()));
        assertFalse(output.isAdmin());
        assertTrue(output.isEnabled());
        assertNull(output.isVerified());
    }

    private SerializerService _serializer;
}
