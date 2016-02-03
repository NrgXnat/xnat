/*
 * org.nrg.user.services.UserService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.user.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.user.entities.User;

public interface UserService extends BaseHibernateService<User> {
    abstract public User getUserByName(String name);
}
