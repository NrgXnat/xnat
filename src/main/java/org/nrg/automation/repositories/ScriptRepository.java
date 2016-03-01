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
