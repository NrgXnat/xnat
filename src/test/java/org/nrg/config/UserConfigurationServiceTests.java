/*
 * org.nrg.config.UserConfigurationServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */

package org.nrg.config;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration
public class UserConfigurationServiceTests {
    @Test
    public void testCreateUserConfiguration() throws IOException, ConfigServiceException {
        Map<String, String> configuration = new Hashtable<String, String>();
        for (int i = 0; i < 10; i++) {
            configuration.put("item" + i, "Item value " + i);
        }
        String marshaled = new ObjectMapper().writeValueAsString(configuration);
        _service.setUserConfiguration("foo", "stuff", marshaled);
        String retrieved = _service.getUserConfiguration("foo", "stuff");
        assertNotNull(retrieved);
        assertEquals(marshaled, retrieved);
    }

    @Inject
    private UserConfigurationService _service;
}
