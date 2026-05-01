/*
 * spawner: org.nrg.xnat.spawner.tests.BasicJsonTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.tests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.nrg.xnat.spawner.configuration.BasicTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BasicTestConfiguration.class)
public class BasicJsonTests {
    private SerializerService _serializer;
    private ResourceLoader _loader;

    @Autowired
    public void setSerializerService(final SerializerService serializer) {
        _serializer  = serializer;
    }

    @Autowired
    public void setResourceLoader(final ResourceLoader loader) {
        _loader = loader;
    }

    @Test
    public void processMockJson() throws IOException {
        final Resource siteAdminJson = _loader.getResource("classpath:site-admin.json");
        try (final InputStream inputStream = siteAdminJson.getInputStream()) {
            final JsonNode tree = _serializer.deserializeJson(inputStream);
            assertNotNull(tree);
        }
    }
}
