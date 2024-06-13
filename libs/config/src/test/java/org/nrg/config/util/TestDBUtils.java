/*
 * config: org.nrg.config.util.TestDBUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.util;

import org.nrg.framework.constants.Scope;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public final class TestDBUtils {
    private static final String QUERY_COUNT_ALL_CONFIGS      = "SELECT count(*) FROM xhbm_configuration";
    private static final String QUERY_COUNT_ALL_DATA         = "SELECT count(*) FROM xhbm_configuration_data";
    private static final String QUERY_COUNT_SITEWIDE_CONFIGS = "SELECT count(*) FROM xhbm_configuration WHERE tool = :tool AND path = :path AND scope = 0 AND entity_id IS NULL";
    private static final String QUERY_COUNT_SITEWIDE_DATA    = "SELECT count(*) FROM (SELECT DISTINCT d.id FROM xhbm_configuration_data d LEFT JOIN xhbm_configuration c ON c.config_data = d.id WHERE c.tool = :tool AND c.path = :path AND c.scope = 0 AND c.entity_id IS NULL) configs";
    private static final String QUERY_COUNT_PROJECT_CONFIGS  = "SELECT count(*) FROM xhbm_configuration WHERE tool = :tool AND path = :path AND scope = 1 AND entity_id = :project";
    private static final String QUERY_COUNT_PROJECT_DATA     = "SELECT count(*) FROM (SELECT DISTINCT d.id FROM xhbm_configuration_data d LEFT JOIN xhbm_configuration c ON c.config_data = d.id WHERE c.tool = :tool AND c.path = :path AND c.scope = 1 AND c.entity_id = :project) configs";
    private static final String QUERY_DELETE_CONFIGS         = "DELETE FROM xhbm_configuration";
    private static final String QUERY_DELETE_DATA            = "DELETE FROM xhbm_configuration_data";

    private final NamedParameterJdbcTemplate _template;

    public TestDBUtils(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    //used for tests that would rather have an empty DB.
    public void cleanDb() {
        _template.getJdbcOperations().execute(QUERY_DELETE_CONFIGS);
        _template.getJdbcOperations().execute(QUERY_DELETE_DATA);
    }

    public int countConfigurationRows() {
        return _template.queryForObject(QUERY_COUNT_ALL_CONFIGS, EmptySqlParameterSource.INSTANCE, Integer.class);
    }

    public int countConfigurationDataRows() {
        return _template.queryForObject(QUERY_COUNT_ALL_DATA, EmptySqlParameterSource.INSTANCE, Integer.class);
    }

    public int countConfigurationRows(final String tool, final String path, final Scope scope, final String project) {
        return _template.queryForObject(QUERY_COUNT_PROJECT_CONFIGS, new MapSqlParameterSource("tool", tool).addValue("path", path).addValue("scope", scope.ordinal()).addValue("project", project), Integer.class);
    }

    public int countConfigurationDataRows(final String tool, final String path, final Scope scope, final String project) {
        return _template.queryForObject(QUERY_COUNT_PROJECT_DATA, new MapSqlParameterSource("tool", tool).addValue("path", path).addValue("scope", scope.ordinal()).addValue("project", project), Integer.class);
    }
}
