package org.nrg.automation.repositories;

import org.nrg.automation.entities.Script;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * ScriptRepository class.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu> on 9/25/2014.
 */
@Repository
public class ScriptRepository extends AbstractHibernateDAO<Script> {
}
