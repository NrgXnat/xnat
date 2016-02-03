package org.nrg.xnat.configuration;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Sets up the database configuration for XNAT.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(_driver);
        dataSource.setUrl(_url);
        dataSource.setUsername(_username);
        dataSource.setPassword(_password);
        return dataSource;
    }

    @Value("${datasource.driver}")
    private String _driver;
    @Value("${datasource.url}")
    private String _url;
    @Value("${datasource.username}")
    private String _username;
    @Value("${datasource.password}")
    private String _password;
}
