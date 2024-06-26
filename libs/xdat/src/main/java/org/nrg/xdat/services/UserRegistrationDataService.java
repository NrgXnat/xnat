/*
 * core: org.nrg.xdat.services.UserRegistrationDataService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * UserRegistrationDataService
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/26/13 by rherri01
 */
package org.nrg.xdat.services;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xft.security.UserI;

public interface UserRegistrationDataService extends BaseHibernateService<UserRegistrationData> {
    public abstract UserRegistrationData cacheUserRegistrationData(UserI user, String phone, String organization, String comment) throws NrgServiceException;;
    public abstract UserRegistrationData getUserRegistrationData(UserI user);
    public abstract UserRegistrationData getUserRegistrationData(String xdatUserId);
    public abstract void clearUserRegistrationData(UserI user);
}
