/*
 * org.nrg.xnat.initialization.tasks.XnatPasswordEncrypter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat.initialization.tasks;

import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class MigrateDatabaseTables extends AbstractInitializingTask {
    @Autowired
    public MigrateDatabaseTables(final JdbcTemplate template) {
        super();
        _template = template;
    }

    @Override
    public String getTaskName() {
        return "Migrate XNAT database tables";
    }

    @Override
    public void run() {
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/migration/**/*-tables.properties")) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BasicXnatResourceLocator.getResources()
    }

    private boolean tableExists(final String name) throws SQLException {
        try (final Connection connection = _template.getDataSource().getConnection();
             final ResultSet results = connection.getMetaData().getTables("catalog", null, name, new String[]{"TABLE"})) {
            if (results.next()) {
                return true;
            }
        }
        return false;
    }

    private static final Logger logger = LoggerFactory.getLogger(MigrateDatabaseTables.class);

    private final JdbcTemplate _template;
}
