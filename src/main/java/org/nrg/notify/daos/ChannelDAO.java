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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Channel;
import org.springframework.stereotype.Repository;


@Repository
public class ChannelDAO extends AbstractHibernateDAO<Channel> {

    /**
     * Returns the channel with the given name. 
     * @param name The name of the channel to retrieve.
     * @return The retrieved channel, or <b>null</b> if the channel isn't found.
     */
    public Channel getChannelByName(String name) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("name", name));
        criteria.add(Restrictions.eq("enabled", true));
        @SuppressWarnings("unchecked")
        List<Channel> channels = criteria.list();
        if (channels == null || channels.size() == 0) {
            return null;
        }
        return channels.get(0);
    }

}
