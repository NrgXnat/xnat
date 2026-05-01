/*
 * framework: org.nrg.framework.orm.auditable.HibernateAuditableEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.auditable;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;

/**
 * HibernateAuditableEntityService class.
 *
 * @author Rick Herrick
 */
@Service
@Slf4j
public class HibernateAuditableEntityService extends AbstractHibernateEntityService<AuditableEntity, AuditableEntityDAO> implements AuditableEntityService {
    public HibernateAuditableEntityService() {
        log.debug("Created a new instance of the HibernateAuditableEntityService class.");
    }
}
