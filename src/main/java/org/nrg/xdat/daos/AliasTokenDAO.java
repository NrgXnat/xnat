/*
 * core: org.nrg.xdat.daos.AliasTokenDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.AliasToken;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToSeconds;

@SuppressWarnings("unchecked")
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

    public List<AliasToken> findByExpired(String interval) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.lt("created", new Date(System.currentTimeMillis() - (1000L * convertPGIntervalToSeconds(interval)))));
        if (criteria.list().size() == 0) {
            return null;
        }
        return criteria.list();
    }
}
