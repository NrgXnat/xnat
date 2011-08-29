/**
 * ChannelDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Channel;
import org.springframework.stereotype.Repository;


/**
 * Implements the DAO class for the {@link Channel} entity type.
 * 
 * @see AbstractHibernateDAO
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
@Repository
public class ChannelDAO extends AbstractHibernateDAO<Channel> {
}
