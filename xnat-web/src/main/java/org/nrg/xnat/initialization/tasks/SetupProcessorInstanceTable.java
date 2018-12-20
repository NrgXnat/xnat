/*
 * web: org.nrg.xnat.initialization.tasks.UpdateUserAuthTable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import net.sf.ehcache.hibernate.HibernateUtil;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.archive.operations.ProcessorGradualDicomImportOperation;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.processors.MizerArchiveProcessor;
import org.nrg.xnat.processors.StudyRemappingArchiveProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("SqlDialectInspection")
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
    protected void callImpl() throws InitializingTaskException {

        final Long processorChanges = _template.queryForObject("SELECT COUNT(*) FROM xhbm_archive_processor_instance_aud WHERE processor_class='"+MizerArchiveProcessor.class.getCanonicalName()+"' OR processor_class='"+StudyRemappingArchiveProcessor.class.getCanonicalName()+"'", Long.class);
        if(processorChanges==0){
            //The processor instances table is new. Add default processor instances.
            ArchiveProcessorInstance defaultSiteAnonProcessor = new ArchiveProcessorInstance();
            defaultSiteAnonProcessor.setLocation(ProcessorGradualDicomImportOperation.NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE);
            defaultSiteAnonProcessor.setLabel("Site Anonymization");
            defaultSiteAnonProcessor.setPriority(10);
            defaultSiteAnonProcessor.setProcessorClass(MizerArchiveProcessor.class.getCanonicalName());
            defaultSiteAnonProcessor.setScope("site");
            defaultSiteAnonProcessor.setParameters(new HashMap<String, String>());
            defaultSiteAnonProcessor.setScpBlacklist(new HashSet<String>());
            defaultSiteAnonProcessor.setScpWhitelist(new HashSet<String>());
            _archiveProcessorInstanceService.create(defaultSiteAnonProcessor);

            ArchiveProcessorInstance defaultRemappingProcessor = new ArchiveProcessorInstance();
            defaultRemappingProcessor.setLocation(ProcessorGradualDicomImportOperation.NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED);
            defaultRemappingProcessor.setLabel("Remapping");
            defaultRemappingProcessor.setPriority(10);
            defaultRemappingProcessor.setProcessorClass(StudyRemappingArchiveProcessor.class.getCanonicalName());
            defaultRemappingProcessor.setScope("site");
            defaultRemappingProcessor.setParameters(new HashMap<String, String>());
            defaultRemappingProcessor.setScpBlacklist(new HashSet<String>());
            defaultRemappingProcessor.setScpWhitelist(new HashSet<String>());
            _archiveProcessorInstanceService.create(defaultRemappingProcessor);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(SetupProcessorInstanceTable.class);

    private final JdbcTemplate _template;
    private final ArchiveProcessorInstanceService _archiveProcessorInstanceService;
}
