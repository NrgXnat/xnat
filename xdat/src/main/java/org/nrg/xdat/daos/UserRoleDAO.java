/*
 * core: org.nrg.xdat.daos.UserRoleDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleDAO extends AbstractHibernateDAO<UserRole> {

    public static final String ROLE     = "role";
    public static final String USERNAME = "username";

    public List<UserRole> findByRole(final String role) {
        return findByProperty(ROLE, role);
    }

    public List<UserRole> findByUser(final String username) {
        return findByProperty(USERNAME, username);
    }

    public UserRole findByUserRole(final String username, final String role) {
        return findByUniqueProperties(parameters(USERNAME, username, ROLE, role));
    }

    @SuppressWarnings("unused")
    public boolean isUserRole(final String username, final String role) {
        return exists(parameters(USERNAME, username, ROLE, role));
    }
}
