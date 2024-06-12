/*
 * framework: org.nrg.framework.orm.auditable.AuditableEntityDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.auditable;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
public class AuditableEntityDAO extends AbstractHibernateDAO<AuditableEntity> {

}
