/*
 * web: org.nrg.xnat.initialization.tasks.transforms.ConvertProjectDataInfoToId
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks.transforms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Converts a column from a bigint containing a project's <b>projectdata_info</b> value to a varchar(255) with the
 * project's ID. If the column is already a varchar(255), no action is taken. This function will probably fail if the
 * target column has a foreign key constraint on it!
 */
@SuppressWarnings("unused")
@Slf4j
public class PopulateNullImageScanProjectReferences implements Callable<String> {

    private static final String MESSAGE_SQL_ERROR = "An error occurred trying to validate the xnat_imagescandata.project column";

    public PopulateNullImageScanProjectReferences(final DatabaseHelper helper, final String table, final String column) {
        _helper = helper;
        if (!StringUtils.equalsIgnoreCase(table, "xnat_imagescandata") || !StringUtils.equalsIgnoreCase(column, "project")) {
            log.warn("For some reason I'm being invoked for column {}.{} instead of xnat_imagescandata.project", table, column);
        }
        _table = table;
        _column = column;
    }

    @Override
    public String call() {
        try {
            final String typeName = _helper.columnExists(_table, _column);
            if (StringUtils.isBlank(typeName)) {
                log.warn(MESSAGE_NO_PROJECT_COLUMN);
                return MESSAGE_NO_PROJECT_COLUMN;
            }
        } catch (SQLException e) {
            log.error("An error occurred trying to validate the xnat_imagescandata.project column", e);
            return MESSAGE_SQL_ERROR;
        }

        // Get the template and see if there are any null values for project in the xnat_imagescandata table.
        final JdbcTemplate template = _helper.getJdbcTemplate();
        final int          count    = template.queryForObject(QUERY_COUNT_NULL_PROJECT_REFS, Integer.class);

        // If there are no values in the table, there's nothing to do.
        if (count == 0) {
            return null;
        }

        log.info("Found {} rows in the xnat_imagescandata table that have NULL set for project. Populating based on the value of project for the corresponding image session.", count);
        final int affected = template.update(QUERY_UPDATE_NULL_PROJECT_REFS);
        if (affected == count) {
            log.info("Successfully updated {} rows in the xnat_imagescandata table. If there are any NULL project references, it's not my fault.", affected);
        } else {
            log.warn("Tried to update {} rows in the xnat_imagescandata table that have NULL set for project, but only {} seem to have actually been changed.", count, affected);
        }
        return null;
    }

    private static final String MESSAGE_NO_PROJECT_COLUMN      = "Request to validate and populate column xnat_imagescandata.project failed: it doesn't appear to exist.";
    private static final String QUERY_COUNT_NULL_PROJECT_REFS  = "SELECT count(*) FROM xnat_imagescandata WHERE project IS NULL";
    private static final String QUERY_UPDATE_NULL_PROJECT_REFS = "UPDATE xnat_imagescandata s " +
                                                                 "SET " +
                                                                 "    project = x.project " +
                                                                 "FROM " +
                                                                 "    xnat_experimentdata x " +
                                                                 "WHERE " +
                                                                 "    s.image_session_id = x.id AND " +
                                                                 "    s.project IS NULL";

    private final DatabaseHelper _helper;
    private final String         _table;
    private final String         _column;
}
