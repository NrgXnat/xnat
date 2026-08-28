/*
 * web: org.nrg.dcm.scp.TestDicomSCPInstanceAuditingConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.nrg.dcm.scp.daos.DicomSCPInstanceDAO;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.dcm.scp.services.impl.hibernate.HibernateDicomSCPInstanceService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OrmTestConfiguration.class)
public class TestDicomSCPInstanceAuditingConfig {
    @Bean
    public DicomSCPInstanceService dicomSCPInstanceService() {
        return new HibernateDicomSCPInstanceService();
    }

    @Bean
    public DicomSCPInstanceDAO dicomSCPInstanceDAO() {
        return new DicomSCPInstanceDAO();
    }

    @Bean
    public HibernateEntityPackageList dicomSCPEntities() {
        return new HibernateEntityPackageList("org.nrg.dcm.scp");
    }
}
