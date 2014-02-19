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

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.security.XDATUser;

public interface UserRegistrationDataService extends BaseHibernateService<UserRegistrationData> {
    public abstract UserRegistrationData cacheUserRegistrationData(XDATUser user, String phone, String organization, String comment);
    public abstract UserRegistrationData getUserRegistrationData(XDATUser user);
    public abstract UserRegistrationData getUserRegistrationData(String xdatUserId);
    public abstract void clearUserRegistrationData(XDATUser user);
}
