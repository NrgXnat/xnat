/*
 * core: org.nrg.xdat.daos.UserRoleDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * UserRoleDAO
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/20/13 by Tim Olsen
 */
package org.nrg.xdat.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserRole;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleDAO extends AbstractHibernateDAO<UserRole> {

    public List<UserRole> findByRole(String role) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("role", role));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<UserRole>) criteria.list();
    }

    public List<UserRole> findByUser(String username) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("username", username));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<UserRole>) criteria.list();
    }

    public UserRole findByUserRole(String username, String role) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("username", username));
        criteria.add(Restrictions.eq("role", role));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        if (criteria.list().size() == 0) {
            return null;
        }
        return (UserRole)criteria.list().get(0);
    }
}
