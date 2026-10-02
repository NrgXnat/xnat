package org.nrg.xnat.services.archive.impl.hibernate;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.test.OrmTestConfiguration;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Wires the inbox import request entity, DAO and Hibernate service against the shared ORM test database, so a
 * request can be read the way its callers read it: in a transaction of its own, then used after that has ended.
 * Class-based proxies, as the application's OrmConfig has, since the DAO is injected by its class.
 */
@Configuration
@Import(OrmTestConfiguration.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class DicomInboxImportRequestOrmTestConfig {
    @Bean
    public HibernateEntityPackageList dicomInboxImportRequestEntities() {
        return new HibernateEntityPackageList("org.nrg.xnat.services.messaging.archive");
    }

    @Bean
    public DicomInboxImportRequestDAO dicomInboxImportRequestDAO() {
        return new DicomInboxImportRequestDAO();
    }

    @Bean
    public DicomInboxImportRequestService dicomInboxImportRequestService() {
        return new HibernateDicomInboxImportRequestService();
    }
}
