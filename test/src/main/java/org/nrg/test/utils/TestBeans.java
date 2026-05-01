package org.nrg.test.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.Driver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class TestBeans {
    private TestBeans() {}

    /**
     * Creates an in-memory H2 database instance.
     *
     * @return A data source for testing purposes.
     */
    public static DataSource getInMemoryTestDataSource() {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUrl("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    public static JsonNode getDefaultTestSiteMap() throws IOException {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final Resource resource = resolver.getResource("classpath:org/nrg/test/configuration/siteMap.json");
        return new ObjectMapper().readTree(resource.getInputStream());
    }
}
