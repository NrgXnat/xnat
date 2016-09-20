/*
 * org.nrg.automation.repositories.ScriptRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.repositories;

import org.nrg.automation.entities.Script;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * ScriptRepository class.
 */
@Repository
public class ScriptRepository extends AbstractHibernateDAO<Script> {
}
