/*
 * framework: org.nrg.framework.orm.datasource.DataSourceConfigTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.datasource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.orm.DatabaseHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.SQLException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DataSourceConfig.class)
public class DataSourceConfigTest {
    @Test
    public void verifyDataSource() {
        assertNotNull(_dataSource);
        final Driver driver = ((SimpleDriverDataSource) _dataSource).getDriver();
        assertNotNull(driver);
        assertEquals(org.postgresql.Driver.class, driver.getClass());
    }

    @Ignore // Remove @Ignore to run this test. Runs against active local PostgreSQL server.
    @Test
    public void testBasicDatabaseHelper() throws SQLException {
        // This is a pointless test at this point. These should all be modified to work for both H2 and PostgreSQL.
        final DatabaseHelper helper   = new DatabaseHelper(_dataSource);
        assertNotNull(helper);
    }

    @Ignore // Remove @Ignore to run this test. Runs against active local PostgreSQL server.
    @Test
    public void testDatabaseHelper() throws SQLException {
        final JdbcTemplate   template = new JdbcTemplate(_dataSource);
        final DatabaseHelper helper   = new DatabaseHelper(template);

        template.execute("CREATE TABLE test (id int, last_name varchar(100), first_name varchar(100), address varchar(100), city varchar(100))");

        assertTrue(helper.tableExists("test"));
        final String datatype = helper.columnExists("test", "last_name");
        assertNotNull(datatype);
        assertEquals("varchar(100)", datatype);
        helper.setColumnDatatype("test", "last_name", "varchar(255)");
        final String altered = helper.columnExists("test", "last_name");
        assertNotNull(altered);
        assertEquals("varchar(255)", altered);

        template.execute("DROP TABLE test");
    }

    @Inject
    private DataSource _dataSource;
}
