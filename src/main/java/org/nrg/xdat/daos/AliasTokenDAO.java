/**
 * AliasTokenDAO
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 4/17/12 by rherri01
 */
package org.nrg.xdat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.AliasToken;
import org.springframework.stereotype.Repository;

@Repository
public class AliasTokenDAO extends AbstractHibernateDAO<AliasToken> {
    public AliasToken findByAlias(String alias) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("alias", alias));
        if (criteria.list().size() == 0) {
            return null;
        }
        return (AliasToken) criteria.list().get(0);
    }
}
