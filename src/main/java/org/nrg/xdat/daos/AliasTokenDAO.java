/*
 * core: org.nrg.xdat.daos.AliasTokenDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * AliasTokenDAO
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xdat.daos;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AliasTokenDAO extends AbstractHibernateDAO<AliasToken> {
    public AliasToken findByAlias(String alias) {
        return findByAlias(alias, false);
    }

    public AliasToken findByAlias(String alias, boolean includeDisabled) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("alias", alias));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (AliasToken) criteria.list().get(0);
    }

    public List<AliasToken> findByXdatUserId(String xdatUserId) {
        return findByXdatUserId(xdatUserId, false);
    }

    @SuppressWarnings("unchecked")
    public List<AliasToken> findByXdatUserId(String xdatUserId, boolean includeDisabled) {
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("xdatUserId", xdatUserId));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<AliasToken> findByExpired(String interval) {
        Criteria criteria = getCriteriaForType();
        try {
            Date expirationTime = new Date(System.currentTimeMillis() - (1000L*SiteConfigPreferences.convertPGIntervalToSeconds(interval)));
            criteria.add(Restrictions.lt("created", expirationTime));
        } catch (SQLException e) {
            _log.error("Failed to get list of expired tokens",e);
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return criteria.list();
    }

    static Logger _log = Logger.getLogger(AliasTokenDAO.class);
}
