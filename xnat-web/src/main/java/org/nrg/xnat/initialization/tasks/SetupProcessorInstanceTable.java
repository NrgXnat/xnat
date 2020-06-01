/*
 * web: org.nrg.xnat.initialization.tasks.UpdateUserAuthTable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.xnat.archive.operations.ProcessorGradualDicomImportOperation;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.processors.MizerArchiveProcessor;
import org.nrg.xnat.processors.StudyRemappingArchiveProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;

@Component
public class SetupProcessorInstanceTable extends AbstractInitializingTask {
    @Autowired
    public SetupProcessorInstanceTable(final JdbcTemplate template, final ArchiveProcessorInstanceService archiveProcessorInstanceService) {
        super();
        _template = template;
        _archiveProcessorInstanceService = archiveProcessorInstanceService;
    }

    @Override
    public String getTaskName() {
        return "Update the user authentication table";
    }

    @Override
    protected void callImpl() {
        final long processorChanges = _template.queryForObject("SELECT COUNT(*) FROM xhbm_archive_processor_instance_aud WHERE processor_class='" + MizerArchiveProcessor.class.getCanonicalName() + "' OR processor_class='" + StudyRemappingArchiveProcessor.class.getCanonicalName() + "'", Long.class);
        if (processorChanges == 0) {
            //The processor instances table is new. Add default processor instances.
            final ArchiveProcessorInstance defaultSiteAnonProcessor = new ArchiveProcessorInstance();
            defaultSiteAnonProcessor.setLocation(ProcessorGradualDicomImportOperation.NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE);
            defaultSiteAnonProcessor.setLabel("Site Anonymization");
            defaultSiteAnonProcessor.setPriority(10);
            defaultSiteAnonProcessor.setProcessorClass(MizerArchiveProcessor.class.getCanonicalName());
            defaultSiteAnonProcessor.setScope("site");
            defaultSiteAnonProcessor.setParameters(new HashMap<>());
            defaultSiteAnonProcessor.setScpBlacklist(new HashSet<>());
            defaultSiteAnonProcessor.setScpWhitelist(new HashSet<>());
            _archiveProcessorInstanceService.create(defaultSiteAnonProcessor);

            final ArchiveProcessorInstance defaultRemappingProcessor = new ArchiveProcessorInstance();
            defaultRemappingProcessor.setLocation(ProcessorGradualDicomImportOperation.NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED);
            defaultRemappingProcessor.setLabel("Remapping");
            defaultRemappingProcessor.setPriority(10);
            defaultRemappingProcessor.setProcessorClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
            defaultRemappingProcessor.setScope("site");
            defaultRemappingProcessor.setParameters(new HashMap<>());
            defaultRemappingProcessor.setScpBlacklist(new HashSet<>());
            defaultRemappingProcessor.setScpWhitelist(new HashSet<>());
            _archiveProcessorInstanceService.create(defaultRemappingProcessor);
        }
    }

    private final JdbcTemplate                    _template;
    private final ArchiveProcessorInstanceService _archiveProcessorInstanceService;
}
