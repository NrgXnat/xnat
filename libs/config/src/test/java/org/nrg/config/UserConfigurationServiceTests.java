/*
 * config: org.nrg.config.UserConfigurationServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.configuration.NrgConfigTestConfiguration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.UserConfigurationService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgConfigTestConfiguration.class)
public class UserConfigurationServiceTests {
    @Test
    public void testCreateUserConfiguration() throws IOException, ConfigServiceException {
        final Map<String, String> configuration = new Hashtable<>();
        for (int i = 0; i < 10; i++) {
            configuration.put("item" + i, "Item value " + i);
        }
        final String marshaled = new ObjectMapper().writeValueAsString(configuration);
        _service.setUserConfiguration("foo", "stuff", marshaled);
        final String retrieved = _service.getUserConfiguration("foo", "stuff");
        assertNotNull(retrieved);
        assertEquals(marshaled, retrieved);
    }

    @Inject
    private UserConfigurationService _service;
}
