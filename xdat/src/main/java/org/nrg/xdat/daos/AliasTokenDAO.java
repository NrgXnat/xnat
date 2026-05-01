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

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xdat.entities.AliasToken;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToSeconds;

@Repository
public class AliasTokenDAO extends AbstractHibernateDAO<AliasToken> {
    public AliasToken findByAlias(String alias) {
        return findByAlias(alias, false);
    }

    public AliasToken findByAlias(String alias, boolean includeDisabled) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("alias", alias);
        if (includeDisabled) {
            parameters.put(ENABLED_PROPERTY, ENABLED_ALL);
        }
        return findByUniqueProperties(parameters);
    }

    public List<AliasToken> findByXdatUserId(String xdatUserId) {
        return findByXdatUserId(xdatUserId, false);
    }

    public List<AliasToken> findByXdatUserId(String xdatUserId, boolean includeDisabled) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("xdatUserId", xdatUserId);
        if (includeDisabled) {
            parameters.put(ENABLED_PROPERTY, ENABLED_ALL);
        }
        return findByProperties(parameters);
    }

    public List<AliasToken> findByExpired(String interval) {
        QueryBuilder<AliasToken> builder = newQueryBuilder();
        builder.getQuery().select(builder.getRoot()).where(builder.lt("estimatedExpirationTime", new Date(System.currentTimeMillis())));
        return builder.getResults();
    }
}
