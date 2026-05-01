/*
 * core: org.nrg.xdat.daos.UserChangeRequestDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserChangeRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserChangeRequestDAO extends AbstractHibernateDAO<UserChangeRequest> {

    public static final String USERNAME        = "username";
    public static final String FIELD_TO_CHANGE = "fieldToChange";
    public static final String GUID            = "guid";

    public List<UserChangeRequest> findByUserAndField(String username, String field) {
        return findByProperties(parameters(USERNAME, username, FIELD_TO_CHANGE, field));
    }

    public List<UserChangeRequest> findByGuid(String guid) {
        return findByProperty(GUID, guid);
    }

    public void cancelByUserAndField(String username, String field) {
        findByUserAndField(username, field).forEach(this::delete);
    }
}
