/*
 * core: org.nrg.xdat.daos.UserRegistrationDataDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserRegistrationData;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class UserRegistrationDataDAO extends AbstractHibernateDAO<UserRegistrationData> {
    public UserRegistrationData findByLogin(String login) {
        return findByLogin(login, false);
    }

    public UserRegistrationData findByLogin(String login, boolean includeDisabled) {
        log.debug("Looking for {} user with login name of: {}", includeDisabled ? "any" : "enabled", login);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("login", login);
        if (includeDisabled) {
            parameters.put(ENABLED_PROPERTY, ENABLED_ALL);
        }
        return findByUniqueProperties(parameters);
    }
}
