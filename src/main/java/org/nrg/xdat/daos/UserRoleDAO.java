/*
 * core: org.nrg.xdat.daos.UserRoleDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("unchecked")
@Repository
public class UserRoleDAO extends AbstractHibernateDAO<UserRole> {
    public List<UserRole> findByRole(final String role) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("role", role));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<UserRole>) criteria.list();
    }

    public List<UserRole> findByUser(final String username) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("username", username));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<UserRole>) criteria.list();
    }

    public UserRole findByUserRole(final String username, final String role) {
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("username", username));
        criteria.add(Restrictions.eq("role", role));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        if (criteria.list().isEmpty()) {
            return null;
        }
        return (UserRole) criteria.list().get(0);
    }

    public boolean isUserRole(final String username, final String role) {
        return exists(parameters("username", username, "role", role));
    }

}
