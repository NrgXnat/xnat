/*
 * core: org.nrg.xdat.daos.XdatUserAuthDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.XdatUserAuth;
import org.springframework.stereotype.Repository;

@Repository
public class XdatUserAuthDAO extends AbstractHibernateDAO<XdatUserAuth> {
    public boolean hasUserByNameAndAuth(final String user, final String authMethod) {
        return hasUserByNameAndAuth(user, authMethod, null);
    }

    public boolean hasUserByNameAndAuth(final String user, final String authMethod, final String authMethodId) {
        final Query query = getSession().createQuery(StringUtils.isBlank(authMethodId) ? QUERY_HAS_USER_AND_AUTH : QUERY_HAS_USER_AND_AUTH_AND_ID);
        query.setParameter("authUser", user);
        query.setParameter("authMethod", authMethod);
        if (StringUtils.isNotBlank(authMethodId)) {
            query.setParameter("authMethodId", authMethodId);
        }
        return (boolean) query.uniqueResult();
    }

    public String getUsernameByNameAndAuth(final String user, final String authMethod, final String authMethodId) {
        final Query query = getSession().createQuery(QUERY_GET_USERNAME_BY_USER_AND_AUTH_AND_ID);
        query.setParameter("authUser", user);
        query.setParameter("authMethod", authMethod);
        query.setParameter("authMethodId", authMethodId);
        return (String) query.uniqueResult();
    }

    private static final String QUERY_HAS_USER_AND_AUTH                    = "SELECT COUNT(*) > 0 FROM XdatUserAuth where enabled = true AND authUser = :authUser AND authMethod = :authMethod";
    private static final String QUERY_HAS_USER_AND_AUTH_AND_ID             = QUERY_HAS_USER_AND_AUTH + " AND authMethodId = :authMethodId";
    private static final String QUERY_GET_USERNAME_BY_USER_AND_AUTH_AND_ID = "SELECT xdatUsername FROM XdatUserAuth where enabled = true AND authUser = :authUser AND authMethod = :authMethod AND authMethodId = :authMethodId";
}
