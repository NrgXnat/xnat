/*
 * web: org.nrg.xnat.initialization.tasks.UpdateConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SessionFactory;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.constants.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Slf4j
public class UpdateConfigurationService extends AbstractInitializingTask {
    @Autowired
    public UpdateConfigurationService(final NamedParameterJdbcTemplate template, final SessionFactory sessionFactory) {
        super();
        _template = template;
        _sessionFactory = sessionFactory;
    }

    @Override
    public String getTaskName() {
        return "Update configuration service to convert long IDs to project IDs.";
    }

    @Override
    protected void callImpl() {
        final Set<Long> projectDataInfoIds = new HashSet<>(_template.queryForList(QUERY_CONFIGS_WITH_METADATA_IDS, EmptySqlParameterSource.INSTANCE, Long.class));
        if (projectDataInfoIds.isEmpty()) {
            log.info("No suspect configuration entries found.");
            return;
        }

        log.info("Found {} configurations that use project metadata IDs instead of project scope and entity ID. Preparing to convert.", projectDataInfoIds.size());
        final List<Pair<Long, String>> projects = _template.query(QUERY_GET_IDS_FROM_METADATA_IDS, new MapSqlParameterSource("projectDataInfoIds", projectDataInfoIds), new RowMapper<Pair<Long, String>>() {
            @Override
            public Pair<Long, String> mapRow(final ResultSet resultSet, final int i) throws SQLException {
                return ImmutablePair.of(resultSet.getLong("projectdata_info"), resultSet.getString("id"));
            }
        });

        final Map<Long, String>     processed  = new HashMap<>();
        final MapSqlParameterSource parameters = new MapSqlParameterSource("scope", Scope.Project.ordinal());
        for (final Pair<Long, String> project: projects) {
            final Long   projectDataInfoId = project.getLeft();
            final String projectId         = project.getRight();
            final int    affected          = _template.update(UPDATE_CONFIGS_WITH_METADATA_ID, parameters.addValue("projectId", projectId).addValue("projectDataInfoId", projectDataInfoId));
            log.info("Updated {} configurations with project set to {} metadata ID to use project ID '{}' as entity ID.", affected, projectDataInfoId, projectId);
            processed.put(projectDataInfoId, projectId);
        }

        // If the two are the same size, we processed all configurations.
        if (projectDataInfoIds.size() == processed.size()) {
            log.warn("Converted {} configurations from using project metadata ID to project ID and scope.\n\n ***** Processed:\n{}", processed.size(), StringUtils.join(processed, ", "));
        } else {
            final Sets.SetView<Long> missing = Sets.difference(projectDataInfoIds, processed.keySet());
            log.warn("Converted {} configurations from using project metadata ID to project ID and scope. {} configurations were found that used the metadata ID but didn't have corresponding projects.\n ***** Processed:\n{}\n ***** Missing:\n{}",
                     processed.size(), missing.size(), StringUtils.join(processed, ", "), StringUtils.join(missing, ", "));
        }

        _sessionFactory.getCache().evictEntityRegion(Configuration.class);
    }

    private static final String QUERY_CONFIGS_WITH_METADATA_IDS = "SELECT DISTINCT project FROM xhbm_configuration WHERE entity_id IS null AND project IS NOT NULL AND enabled = TRUE";
    private static final String QUERY_GET_IDS_FROM_METADATA_IDS = "SELECT " +
                                                                  "  id, " +
                                                                  "  projectdata_info " +
                                                                  "FROM xnat_projectdata " +
                                                                  "WHERE projectdata_info IN (:projectDataInfoIds)";
    private static final String UPDATE_CONFIGS_WITH_METADATA_ID = "UPDATE xhbm_configuration SET entity_id = :projectId, scope = :scope, project = NULL WHERE project = :projectDataInfoId AND enabled = TRUE";

    private final NamedParameterJdbcTemplate _template;
    private final SessionFactory             _sessionFactory;
}
