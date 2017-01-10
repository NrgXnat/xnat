/*
 * web: org.nrg.xnat.initialization.tasks.transforms.ConvertProjectDataInfoToId
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks.transforms;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Converts a column from a bigint containing a project's <b>projectdata_info</b> value to a varchar(255) with the
 * project's ID. If the column is already a varchar(255), no action is taken. This function will probably fail if the
 * target column has a foreign key constraint on it!
 */
@SuppressWarnings("unused")
public class ConvertProjectDataInfoToId implements Callable<String> {
    public ConvertProjectDataInfoToId(final DatabaseHelper helper, final String table, final String column) {
        _helper = helper;
        _table = table;
        _column = column;
    }

    @Override
    public String call() {
        try {
            final String typeName = _helper.columnExists(_table, _column);

            // No conversion necessary.
            if (StringUtils.startsWithIgnoreCase(typeName, "varchar")) {
                return null;
            }

            // If typeName isn't some kind of int, we don't really know what to do with it.
            if (!INT_DATA_TYPES.contains(typeName)) {
                final String message = String.format("Request to transform column %s.%s failed: column is type %s, should be one of %s.", _table, _column, typeName, Joiner.on(", ").join(INT_DATA_TYPES));
                _log.warn(message);
                return message;
            }

            // Get the template and let's see if there are any values in the table.
            final JdbcTemplate template = _helper.getJdbcTemplate();
            final int count = template.queryForObject(COUNT_ASSOC_ENTITIES_QUERY, Integer.class);

            // If there are no values in the table, there's no conversion to do so we can just do the change the data type.
            if (count == 0) {
                _helper.setColumnDatatype(_table, _column, "varchar(255)");
                return null;
            }

            // Add the column to contain the project IDs if it doesn't already exist.
            final String existing = _helper.columnExists(_table, _column + "_temp");
            if (StringUtils.isBlank(existing)) {
                template.execute("ALTER TABLE " + _table + " ADD COLUMN " + _column + "_temp VARCHAR(255)");
            }

            // Get all of the project data info values.
            final List<Long> projectDataInfos = template.query(FIND_ASSOC_ENTITIES_QUERY, new RowMapper<Long>() {
                @Override
                public Long mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
                    return resultSet.getLong(1);
                }
            });

            // Walk through each project data info, find the corresponding project ID, and insert the ID into the temp column.
            for (final Long projectDataInfo : projectDataInfos) {
                final String projectId = template.queryForObject("SELECT id FROM xnat_projectdata WHERE projectdata_info = ?", String.class, projectDataInfo);
                final int total = template.update("UPDATE " + _table + " SET " + _column + "_temp = ? WHERE " + _column + " = ?", projectId, projectDataInfo);
                _log.info("Converted {} rows from projectdata_info value {} to project ID {}.", total, projectDataInfo, projectId);
            }

            // Drop the old column
            template.execute("ALTER TABLE " + _table + " DROP COLUMN " + _column);

            // Move the new column to the same name as the old.
            template.execute("ALTER TABLE " + _table + " RENAME " + _column + "_temp TO " + _column);

            return null;
        } catch (SQLException e) {
            return "An error occurred trying to migrate from project data info to ID: " + e.getMessage();
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(ConvertProjectDataInfoToId.class);

    private static final String       FIND_ASSOC_ENTITIES_QUERY  = "SELECT DISTINCT(associated_entities) FROM xhbm_script_trigger_template_associated_entities";
    private static final String       COUNT_ASSOC_ENTITIES_QUERY = "SELECT COUNT(*) FROM (" + FIND_ASSOC_ENTITIES_QUERY + ") AS entities";
    private static final List<String> INT_DATA_TYPES             = Arrays.asList("bigint", "int8", "bigserial", "integer", "int4", "serial");

    private final DatabaseHelper _helper;
    private final String         _table;
    private final String         _column;
}
