/*
 * framework: org.nrg.framework.ajax.SimpleEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;


import org.nrg.framework.orm.hibernate.BaseHibernateService;

public interface SimpleEntityService extends BaseHibernateService<SimpleEntity> {
    SimpleEntity findByName(final String name);
}
