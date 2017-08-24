/*
 * core: org.nrg.xdat.daos.UserChangeRequestDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.UserChangeRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserChangeRequestDAO extends AbstractHibernateDAO<UserChangeRequest> {

    public List<UserChangeRequest> findByUserAndField(String username, String field) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("username", username));
        criteria.add(Restrictions.eq("fieldToChange", field));
        criteria.add(Restrictions.eq("enabled", true));
        return (List<UserChangeRequest>) criteria.list();
    }

    public List<UserChangeRequest> findByGuid(String guid) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("guid", guid));
        criteria.add(Restrictions.eq("enabled", true));
        return (List<UserChangeRequest>) criteria.list();
    }

    public void cancelByUserAndField(String username, String field) {
        List<UserChangeRequest> requestsToCancel = findByUserAndField(username, field); //should only be at most one request
        for(UserChangeRequest request: requestsToCancel){
            delete(request);
        }
        return;
    }

}
