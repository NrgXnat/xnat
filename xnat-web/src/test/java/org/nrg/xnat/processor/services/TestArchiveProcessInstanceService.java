package org.nrg.xnat.processor.services;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.config.TestArchiveProcessorInstanceServiceConfig;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processors.MizerArchiveProcessor;
import org.nrg.xnat.processors.StudyRemappingArchiveProcessor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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

    @Test
    public void getAllSiteProcessorsForClass() {
        final ArchiveProcessorInstance entity1 = new ArchiveProcessorInstance();
        entity1.setLabel("processor1");
        entity1.setLocation("here");
        entity1.setParameters(ImmutableMap.of("param1", "true"));
        entity1.setPriority(1);
        entity1.setProcessorClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
        entity1.setScope("site");
        entity1.setProjectIdsList(Arrays.asList("one", "two", "three"));
        _service.create(entity1);
        final ArchiveProcessorInstance entity2 = new ArchiveProcessorInstance();
        entity2.setLabel("processor2");
        entity2.setLocation("there");
        entity2.setParameters(ImmutableMap.of("param1", "false"));
        entity2.setPriority(2);
        entity2.setProcessorClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
        entity2.setScope("site");
        entity2.setProjectIdsList(Arrays.asList("one", "two", "three"));
        _service.create(entity2);
        final ArchiveProcessorInstance entity3 = new ArchiveProcessorInstance();
        entity3.setLabel("processor3");
        entity3.setLocation("there");
        entity3.setParameters(ImmutableMap.of("param1", "false"));
        entity3.setPriority(3);
        entity3.setProcessorClass(MizerArchiveProcessor.class.getCanonicalName());
        entity3.setScope("site");
        entity3.setProjectIdsList(Arrays.asList("one", "two", "three"));
        _service.create(entity3);

        final List<ArchiveProcessorInstance> allProcessors = _service.getAllSiteProcessors();
        assertThat(allProcessors).isNotNull().isNotEmpty().hasSize(3);

        final List<ArchiveProcessorInstance> studyRemappingArchiveProcessors = _service.getAllSiteProcessorsForClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
        assertThat(studyRemappingArchiveProcessors).isNotNull().isNotEmpty().hasSize(2);
    }

    @Autowired
    private ArchiveProcessorInstanceService _service;
}
