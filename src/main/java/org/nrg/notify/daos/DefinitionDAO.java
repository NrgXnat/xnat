/**
 * DefinitionDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 19, 2011
 */
package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.BaseHibernateDAO;
import org.nrg.notify.api.Definition;
import org.springframework.stereotype.Repository;

/**
 * Implements the DAO class for the {@link Definition} entity type.
 * 
 * @see BaseHibernateDAO
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
@Repository
public class DefinitionDAO extends BaseHibernateDAO<Definition> {
}
