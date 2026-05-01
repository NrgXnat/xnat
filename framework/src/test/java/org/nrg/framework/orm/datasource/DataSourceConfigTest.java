/*
 * framework: org.nrg.framework.orm.datasource.DataSourceConfigTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.datasource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.orm.DatabaseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataSourceConfig.class)
public class DataSourceConfigTest {
    private final DataSource _dataSource;

    @Autowired
    public DataSourceConfigTest(final DataSource dataSource) {
        _dataSource = dataSource;
    }

    @Test
    public void verifyDataSource() {
        assertThat(_dataSource).isNotNull();

        final Driver driver = ((SimpleDriverDataSource) _dataSource).getDriver();
        assertThat(driver).isNotNull().isOfAnyClassIn(org.postgresql.Driver.class);
    }

    @Test
    @Disabled("Remove @Disabled to run this test. Runs against active local PostgreSQL server")
    public void testBasicDatabaseHelper() {
        // This is a pointless test at this point. These should all be modified to work for both H2 and PostgreSQL.
        final DatabaseHelper helper = new DatabaseHelper(_dataSource);
        assertThat(helper).isNotNull();
    }

    @Test
    @Disabled("Remove @Disabled to run this test. Runs against active local PostgreSQL server")
    public void testDatabaseHelper() throws SQLException {
        final JdbcTemplate   template = new JdbcTemplate(_dataSource);
        final DatabaseHelper helper   = new DatabaseHelper(template);

        template.execute("CREATE TABLE test (id int, last_name varchar(100), first_name varchar(100), address varchar(100), city varchar(100))");

        assertThat(helper.tableExists("test")).isTrue();

        final String datatype = helper.columnExists("test", "last_name");
        assertThat(datatype).isNotNull().isNotEmpty().isEqualTo("varchar(100)");

        helper.setColumnDatatype("test", "last_name", "varchar(255)");
        final String altered = helper.columnExists("test", "last_name");
        assertThat(altered).isNotNull().isNotEmpty().isEqualTo("varchar(255)");

        template.execute("DROP TABLE test");
    }
}
