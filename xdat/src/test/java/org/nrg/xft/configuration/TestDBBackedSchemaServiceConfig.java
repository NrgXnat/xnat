/*
 * core: org.nrg.xft.configuration.TestDBBackedSchemaServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xft.schema.db.dao.DBBackedSchemaDAO;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.nrg.xft.schema.db.services.impl.HibernateDBBackedSchemaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class})
public class TestDBBackedSchemaServiceConfig {
    @Bean
    public HibernateEntityPackageList dbBackedSchemaEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xft.schema.db.entities");
    }

    @Bean
    public DBBackedSchemaDAO dbBackedSchemaDAO() {
        return new DBBackedSchemaDAO();
    }

    @Bean
    public DBBackedSchemaService dbBackedSchemaService(final DBBackedSchemaDAO dbBackedSchemaDAO) {
        return new HibernateDBBackedSchemaServiceImpl();
    }
}
