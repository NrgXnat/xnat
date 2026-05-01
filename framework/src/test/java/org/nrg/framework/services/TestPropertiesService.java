/*
 * framework: org.nrg.framework.services.TestPropertiesService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertiesServiceConfiguration.class)
public class TestPropertiesService {
    private final PropertiesService _service;

    @Autowired
    public TestPropertiesService(final PropertiesService service) {
        _service     = service;
    }

    @Test
    public void testPropertiesRepository() {
        List<File> repositories = _service.getRepositories();
        assertThat(repositories).isNotNull().isNotEmpty().allMatch(File::exists);
    }

    @Test
    public void testPropertiesBundles() {
        Map<String, Properties> bundles = _service.getBundles();
        assertThat(bundles).isNotNull()
                           .isNotEmpty()
                           .hasSize(2)
                           .containsKeys("module1.test1", "test2.test2");
    }

    @Test
    public void testPropertiesLoad() {
        Properties module1 = _service.getProperties("module1", "test1");
        assertThat(module1).isNotNull()
                           .isNotEmpty()
                           .containsOnly(entry("module", "module1"),
                                         entry("property1", "test1.property1"),
                                         entry("property2", "test1.property2"));

        Properties test2 = _service.getProperties("test2");
        assertThat(test2).isNotNull()
                         .isNotEmpty()
                         .containsOnly(entry("property1", "test2.property1"),
                                       entry("property2", "test2.property2"));
    }
}
