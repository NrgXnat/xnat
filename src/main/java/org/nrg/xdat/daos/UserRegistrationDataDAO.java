/**
 * UserRegistrationDataDAO
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/26/13 by rherri01
 */
package org.nrg.xdat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserRegistrationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * UserRegistrationDataDAO class.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Repository
public class UserRegistrationDataDAO extends AbstractHibernateDAO<UserRegistrationData> {

    public UserRegistrationData findByLogin(String login) {
        return findByLogin(login, false);
    }

    public UserRegistrationData findByLogin(String login, boolean includeDisabled) {
        if (_log.isDebugEnabled()) {
            _log.debug("Looking for " + (includeDisabled ? "any" : "enabled") + " user with login name of: " + login);
        }
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("login", login));
        if (!includeDisabled) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        if (criteria.list().size() == 0) {
            return null;
        }
        return (UserRegistrationData) criteria.list().get(0);
    }

    private static final Logger _log = LoggerFactory.getLogger(UserRegistrationDataDAO.class);
}
