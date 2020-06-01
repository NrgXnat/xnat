package org.nrg.xnat.processor.services;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.config.TestArchiveProcessorInstanceServiceConfig;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processors.StudyRemappingArchiveProcessor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@Slf4j
// @RunWith(SpringJUnit4ClassRunner.class)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"org.apache.*", "java.*", "javax.*", "org.w3c.*", "com.sun.*", "org.xml.sax.*"})
@ContextConfiguration(classes = TestArchiveProcessorInstanceServiceConfig.class)
public class TestArchiveProcessInstanceService {
    @Test
    public void contextLoads() {
        log.debug("Loaded the context for the ArchiveProcessorInstanceService tests");
    }

    @Test
    public void createBasicEntity() {
        final ArchiveProcessorInstance entity = new ArchiveProcessorInstance();
        entity.setLabel("processor");
        entity.setLocation("here");
        entity.setParameters(ImmutableMap.of("param1", "true"));
        entity.setPriority(1);
        entity.setProcessorClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
        entity.setScope("site");
        entity.setProjectIdsList(Arrays.asList("one", "two", "three"));
        _service.create(entity);
    }

    @Autowired
    private ArchiveProcessorInstanceService _service;
}
