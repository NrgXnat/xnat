/*
 * org.nrg.config.daos.ConfigurationDataDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.daos;

import org.nrg.config.entities.ConfigurationData;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationDataDAO  extends AbstractHibernateDAO<ConfigurationData>{
}
