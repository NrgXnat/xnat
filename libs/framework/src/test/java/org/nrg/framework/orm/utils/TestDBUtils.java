/*
 * framework: org.nrg.framework.orm.utils.TestDBUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.utils;

import org.h2.api.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public final class TestDBUtils {
    @Autowired
    public TestDBUtils(final DataSource dataSource) {
        _dataSource = dataSource;
    }

    public void cleanDb(final String table) throws SQLException {
        final Connection connection = _dataSource.getConnection();
        try (final Statement statement = connection.createStatement()) {
            //noinspection SqlWithoutWhere
            statement.execute("DELETE FROM " + table);
        } catch (SQLException exception) {
            // If we didn't find the table, that's OK, because it just may not have been created yet.
            if (exception.getErrorCode() != ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1) {
                throw exception;
            }
        }
    }

    private final DataSource _dataSource;
}
