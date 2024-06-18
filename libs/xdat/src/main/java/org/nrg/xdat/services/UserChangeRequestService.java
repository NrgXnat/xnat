/*
 * core: org.nrg.xdat.services.UserChangeRequestService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * UserChangeRequestService
 * (C) 2017 Washington University School of Medicine
 * All Rights Reserved
 * <p/>
 * Released under the Simplified BSD License
 * <p/>
 * Created on 8/22/17 by Mike McKay
 */
package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserChangeRequest;

public interface UserChangeRequestService extends BaseHibernateService<UserChangeRequest> {
     /**
     * Finds request to change the specified field for the specified user
     * @param username        The username.
     * @param fieldToChange   The field to change.
     * @return The {@link UserChangeRequest change request} issued to the indicated user and field.
     */
     UserChangeRequest findChangeRequestForUserAndField(String username, String fieldToChange);

     /**
      * Finds user change request with specified guid
      * @param guid        The global unique identifier.
      * @return The {@link UserChangeRequest change request} issued to the specified guid.
      */
     UserChangeRequest findChangeRequestByGuid(String guid);

     /**
      * Cancels the request to change the specified field for the specified user
      * @param username  The username.
      * @param field     The field of the request that should be canceled.
      */
     void cancelRequest(String username, String field);
}
