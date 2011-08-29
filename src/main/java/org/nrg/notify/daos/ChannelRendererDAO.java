/**
 * ChannelRendererDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.ChannelRenderer;
import org.springframework.stereotype.Repository;


/**
 * Implements the DAO class for the {@link ChannelRenderer} entity type.
 * 
 * @see AbstractHibernateDAO
 * @author Rick Herrick <rick.herrick@wustl.edu>

 */
@Repository
public class ChannelRendererDAO extends AbstractHibernateDAO<ChannelRenderer> {
}
