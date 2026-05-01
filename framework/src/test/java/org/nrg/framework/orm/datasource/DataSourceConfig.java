/*
 * framework: org.nrg.framework.orm.datasource.DataSourceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.datasource;

import org.nrg.framework.beans.Beans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.*;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

@Configuration
@Import(PropertiesConfig.class)
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        final Properties properties = Beans.getNamespacedProperties(_environment, "datasource", true);
        final Class<? extends DataSource> clazz = Class.forName(properties.getProperty("class", SimpleDriverDataSource.class.getName())).asSubclass(DataSource.class);
        if (properties.containsKey("driver")) {
            final String driver = properties.getProperty("driver");
            properties.put("driver", Class.forName(driver).newInstance());
        }
        return Beans.getInitializedBean(properties, clazz);
    }

    @Autowired
    private Environment _environment;
}
