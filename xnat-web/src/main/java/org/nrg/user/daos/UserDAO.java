/*
 * org.nrg.user.daos.UserDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.user.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.user.entities.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO extends AbstractHibernateDAO<User> {

    /**
     * @param name
     * @return
     */
    public User getUserByName(String name) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("name", name));
        criteria.add(Restrictions.eq("enabled", true));
        return (User) criteria.list().get(0);
    }

}
