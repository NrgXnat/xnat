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
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
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

/**
 * Adds users from /old xdat_user table to new user authentication table if they are not already there. New local
 * database users now get added to both automatically, but this is necessary so that those who upgrade from an earlier
 * version still have their users be able to log in. Password expiry times are also added so that pre-existing users
 * still have their passwords expire.
 */
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
        final Long processorChanges = _template.queryForObject("SELECT COUNT(*) FROM xhbm_archive_processor_instance_aud", Long.class);
        if(processorChanges==0){
            //The processor instances table is new. Add default processor instances.
            ArchiveProcessorInstance defaultSiteAnonProcessor = new ArchiveProcessorInstance();
            defaultSiteAnonProcessor.setLocation(1);
            defaultSiteAnonProcessor.setLabel("Site Anonymization");
            defaultSiteAnonProcessor.setPriority(1);
            defaultSiteAnonProcessor.setProcessorClass("org.nrg.xnat.processors.MizerArchiveProcessor");
            defaultSiteAnonProcessor.setScope("site");
            defaultSiteAnonProcessor.setParameters(new HashMap<String, String>());
            defaultSiteAnonProcessor.setScpBlacklist(new HashSet<String>());
            defaultSiteAnonProcessor.setScpWhitelist(new HashSet<String>());
            _archiveProcessorInstanceService.create(defaultSiteAnonProcessor);

            ArchiveProcessorInstance defaultRemappingProcessor = new ArchiveProcessorInstance();
            defaultRemappingProcessor.setLocation(0);
            defaultRemappingProcessor.setLabel("Remapping");
            defaultRemappingProcessor.setPriority(1);
            defaultRemappingProcessor.setProcessorClass("org.nrg.xnat.processors.StudyRemappingArchiveProcessor");
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
