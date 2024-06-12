/*
 * framework: org.nrg.framework.orm.pacs.PacsEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.pacs;


import org.nrg.framework.orm.hibernate.BaseHibernateService;

public interface PacsEntityService extends BaseHibernateService<Pacs> {
    Pacs findByAeTitle(final String aeTitle);
}
