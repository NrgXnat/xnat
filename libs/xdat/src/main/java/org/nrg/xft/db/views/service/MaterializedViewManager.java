/*
 * core: org.nrg.xft.db.views.service.MaterializedViewManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db.views.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MaterializedViewManager {
    public static final String MATERIALIZED_VIEWS              = "xs_materialized_views";
    public static final String PARAM_CURRENT_VIEW              = "currentView";
    public static final String PARAM_TABLE_NAME                = "tableName";
    public static final String PARAM_USERNAME                  = "username";
    public static final String PARAM_SEARCH_ID                 = "searchId";
    public static final String PARAM_TAG                       = "tag";
    public static final String PARAM_SEARCH_SQL                = "searchSql";
    public static final String PARAM_SEARCH_XML                = "searchXml";
    public static final String QUALIFIED_MATERIALIZED_VIEWS    = PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS;
    public static final String QUERY_MATERIALIZED_VIEWS_EXISTS = "SELECT EXISTS(SELECT table_name FROM information_schema.tables WHERE table_schema = '" + PoolDBUtils.search_schema_name + "' AND table_name = '" + MATERIALIZED_VIEWS + "')";
    public static final String QUERY_CREATE_MATERIALIZED_VIEWS = "CREATE TABLE " + QUALIFIED_MATERIALIZED_VIEWS + " ( " +
                                                                 "    table_name  VARCHAR(255), " +
                                                                 "    created     TIMESTAMP DEFAULT now(), " +
                                                                 "    last_access TIMESTAMP DEFAULT now(), " +
                                                                 "    username    VARCHAR(255), " +
                                                                 "    search_id   TEXT, " +
                                                                 "    tag         VARCHAR(255), " +
                                                                 "    search_sql  TEXT, " +
                                                                 "    search_xml  TEXT)";
    public static final String QUERY_UPDATE_CURRENT_VIEW       = "UPDATE " + QUALIFIED_MATERIALIZED_VIEWS + " SET last_access = now() WHERE table_name = :" + PARAM_CURRENT_VIEW;
    public static final String QUERY_GET_RECENT_VIEWS          = "SELECT table_name FROM " + QUALIFIED_MATERIALIZED_VIEWS + " WHERE last_access + INTERVAL '1 hour' < now()";
    public static final String QUERY_DROP_TABLE                = "DROP TABLE " + PoolDBUtils.search_schema_name + ".%s";
    public static final String QUERY_DELETE_MATERIALIZED_VIEW  = "DELETE FROM " + QUALIFIED_MATERIALIZED_VIEWS + " WHERE table_name = :" + PARAM_TABLE_NAME;
    public static final String QUERY_GET_USER_VIEWS            = "SELECT * FROM " + QUALIFIED_MATERIALIZED_VIEWS + " WHERE username = '%s'";
    public static final String QUERY_GET_USER_VIEWS_BY_ID      = "SELECT * FROM " + QUALIFIED_MATERIALIZED_VIEWS + " WHERE search_id = '%s' AND username='%s' ORDER BY last_access DESC";
    public static final String QUERY_GET_MATERIALIZED_VIEW     = "SELECT * FROM " + QUALIFIED_MATERIALIZED_VIEWS + " WHERE table_name = '%s' ORDER BY last_access DESC";
    public static final String QUERY_INSERT_MATERIALIZED_VIEW  = "INSERT INTO " + QUALIFIED_MATERIALIZED_VIEWS + " (table_name, created, last_access, username, search_id, tag, search_sql, search_xml) VALUES " +
                                                                 "    (:" + PARAM_TABLE_NAME + ", now(), now(), :" + PARAM_USERNAME + ", :" + PARAM_SEARCH_ID + ", :" + PARAM_TAG + ", :" + PARAM_SEARCH_SQL + ", :" + PARAM_SEARCH_XML + ")";

    private static MaterializedViewManager manager = null;

    private final NamedParameterJdbcTemplate _template;

    public static MaterializedViewManager getMaterializedViewManager() {
        return getMaterializedViewManager(XDAT.getNamedParameterJdbcTemplate());
    }

    public static MaterializedViewManager getMaterializedViewManager(final NamedParameterJdbcTemplate template) {
        try {
            if (manager == null) {
                PoolDBUtils.CreateTempSchema(PoolDBUtils.getDefaultDBName(), null);
                if (!template.queryForObject(QUERY_MATERIALIZED_VIEWS_EXISTS, EmptySqlParameterSource.INSTANCE, Boolean.class)) {
                    template.getJdbcOperations().execute(QUERY_CREATE_MATERIALIZED_VIEWS);
                }
                manager = new MaterializedViewManager(template);
            }
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        return manager;
    }

    private MaterializedViewManager(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    public static class DBMaterializedViewManager extends Thread {
        private final NamedParameterJdbcTemplate _template;
        private final String                     _currentView;

        public DBMaterializedViewManager(final NamedParameterJdbcTemplate template, final String currentView) {
            _template = template;
            _currentView = currentView;
        }

        @Override
        public void run() {
            if (StringUtils.isNotBlank(_currentView)) {
                final int updated = _template.update(QUERY_UPDATE_CURRENT_VIEW, new MapSqlParameterSource(PARAM_CURRENT_VIEW, _currentView));
                log.debug("Updated {} rows for the current view {}", updated, _currentView);
            }
            for (final String tableName : _template.queryForList(QUERY_GET_RECENT_VIEWS, EmptySqlParameterSource.INSTANCE, String.class)) {
                _template.getJdbcOperations().execute(String.format(QUERY_DROP_TABLE, tableName));
                final int deleted = _template.update(QUERY_DELETE_MATERIALIZED_VIEW, new MapSqlParameterSource(PARAM_TABLE_NAME, tableName));
                log.debug("Dropped table {} and deleted {} materialized view entries", tableName, deleted);
            }
        }
    }

    public List<MaterializedViewI> getViewsByUser(final UserI user, final MaterializedViewServiceI service) throws Exception {
        final List<MaterializedViewI> views = new ArrayList<>();
        final XFTTable                table = XFTTable.Execute(String.format(QUERY_GET_USER_VIEWS, user.getUsername()), PoolDBUtils.getDefaultDBName(), user.getUsername());
        if (table.size() > 0) {
            while (table.hasMoreRows()) {
                views.add(service.populateView(table.nextRowHash(), user));
            }
        }
        log.debug("Got {} materialized views for user {}", views.size(), user.getUsername());
        return views;
    }

    public MaterializedViewI getViewBySearchID(final String searchId, final UserI user, final MaterializedViewServiceI service) throws Exception {
        final XFTTable table = XFTTable.Execute(String.format(QUERY_GET_USER_VIEWS_BY_ID, searchId, user.getUsername()), PoolDBUtils.getDefaultDBName(), user.getUsername());
        return table.size() > 0 ? service.populateView(table.nextRowHash(), user) : null;
    }

    public MaterializedViewI getViewByTablename(final String tablename, UserI user, MaterializedViewServiceI service) throws Exception {
        final XFTTable table = XFTTable.Execute(String.format(QUERY_GET_MATERIALIZED_VIEW, tablename), PoolDBUtils.getDefaultDBName(), user.getUsername());
        return table.size() > 0 ? service.populateView(table.nextRowHash(), user) : null;
    }

    public void delete(final MaterializedViewI view) {
        final int changed = _template.update(QUERY_DELETE_MATERIALIZED_VIEW, new MapSqlParameterSource(PARAM_TABLE_NAME, view.getTable_name()));
        log.debug("Deleted materialized view entry {}, got {} changed rows", view.getTable_name(), changed);
    }

    public void register(final MaterializedViewI view) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource(PARAM_TABLE_NAME, view.getTable_name())
                .addValue(PARAM_USERNAME, view.getUser_name())
                .addValue(PARAM_SEARCH_ID, view.getSearch_id())
                .addValue(PARAM_TAG, view.getTag())
                .addValue(PARAM_SEARCH_SQL, view.getSearch_sql())
                .addValue(PARAM_SEARCH_XML, view.getSearch_xml());
        final int inserted = _template.update(QUERY_INSERT_MATERIALIZED_VIEW, parameters);
        log.debug("Inserted {} rows for materialized view entry {}", inserted, view.getTable_name());
    }

    public static void Register(final MaterializedViewI view) {
        getMaterializedViewManager().register(view);
    }

    public static void Delete(final MaterializedViewI view) {
        getMaterializedViewManager().delete(view);
    }
}
