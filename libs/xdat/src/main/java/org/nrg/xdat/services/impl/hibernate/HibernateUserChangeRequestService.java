/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateUserRoleService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * H2AliasTokenService
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 4/17/12 by rherri01
 */
package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.UserChangeRequestDAO;
import org.nrg.xdat.entities.UserChangeRequest;
import org.nrg.xdat.services.UserChangeRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HibernateUserChangeRequestService extends AbstractHibernateEntityService<UserChangeRequest, UserChangeRequestDAO> implements UserChangeRequestService {

    /**
     * Finds request to change the specified field for the specified user
     * @param username        The username.
     * @param fieldToChange   The field to change.
     * @return An list of the {@link UserChangeRequest change requests} issued to the indicated user and field.
     */
    @Override
    @Transactional
    public UserChangeRequest findChangeRequestForUserAndField(String username, String fieldToChange){
        List<UserChangeRequest> requests = getDao().findByUserAndField(username, fieldToChange);
        if(requests==null || requests.size()==0) {
            return null;
        }
        else{
            return requests.get(0);//Since there is a unique constraint on username and field, there will be at most one
        }
    }

    /**
     * Finds user change request with specified guid
     * @param guid        The global unique identifier.
     * @return The {@link UserChangeRequest change request} issued to the specified guid.
     */
    @Override
    @Transactional
    public UserChangeRequest findChangeRequestByGuid(String guid){
        List<UserChangeRequest> requests = getDao().findByGuid(guid);
        if(requests==null || requests.size()==0) {
            return null;
        }
        else{
            return requests.get(0);//Since guid is globally unique, there will be at most one
        }
    }

    /**
     * Cancels the request to change the specified field for the specified user
     * @param username  The username.
     * @param field     The field of the request that should be canceled.
     */
    @Override
    @Transactional
    public void cancelRequest(String username, String field){
        getDao().cancelByUserAndField(username, field);
        return;
    }

    private static final Log _log = LogFactory.getLog(HibernateUserChangeRequestService.class);

}
