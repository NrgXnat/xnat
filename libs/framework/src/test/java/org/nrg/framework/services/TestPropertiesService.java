/*
 * framework: org.nrg.framework.services.TestPropertiesService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPropertiesServiceConfiguration.class)
public class TestPropertiesService {

    @Test
    public void testPropertiesRepository() {
        List<File> repositories = _service.getRepositories();
        assertNotNull("There were no repositories configured", repositories);
        assertTrue("There were no repositories configured", repositories.size() > 0);
        for (File repository : repositories) {
            assertTrue("Properties repository " + repository.getAbsolutePath() + " doesn't exist", repository.exists());
        }
    }

    @Test
    public void testPropertiesBundles() {
        Map<String, Properties> bundles = _service.getBundles();
        assertNotNull(bundles);
        assertTrue(bundles.size() == 2);
        assertTrue(bundles.containsKey("module1.test1")); // The file's named test1, but it has a module property, which overrides the default.
        assertTrue(bundles.containsKey("test2.test2"));
    }

    @Test
    public void testPropertiesLoad() {
        Properties module1 = _service.getProperties("module1", "test1");
        assertEquals("test1.property1", module1.getProperty("property1"));
        assertEquals("test1.property2", module1.getProperty("property2"));
        Properties test2 = _service.getProperties("test2");
        assertEquals("test2.property1", test2.getProperty("property1"));
        assertEquals("test2.property2", test2.getProperty("property2"));
    }

    @Inject
    private PropertiesService _service;
}
