package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.daos.UserRoleDAO;
import org.nrg.xdat.services.UserRoleService;
import org.nrg.xdat.services.impl.hibernate.HibernateUserRoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class})
public class TestUserRoleServiceConfig {
    @Bean
    public UserRoleService userRoleService() {
        return new HibernateUserRoleService();
    }

    @Bean
    public UserRoleDAO userRoleDAO() {
        return new UserRoleDAO();
    }

    @Bean
    public HibernateEntityPackageList dataCacheEntities() {
        return new HibernateEntityPackageList("org.nrg.xdat.entities");
    }
}
