/*
 * notify: org.nrg.notify.daos.ChannelDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.notify.entities.Channel;
import org.springframework.stereotype.Repository;

@Repository
public class ChannelDAO extends AbstractHibernateDAO<Channel> {
    /**
     * Returns the channel with the given name.
     *
     * @param name The name of the channel to retrieve.
     *
     * @return The retrieved channel, or <b>null</b> if the channel isn't found.
     */
    public Channel getChannelByName(String name) {
        return findByUniqueProperty("name", name);
    }
}
