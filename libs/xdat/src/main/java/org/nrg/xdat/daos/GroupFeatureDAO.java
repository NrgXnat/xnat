/*
 * core: org.nrg.xdat.daos.GroupFeatureDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.GroupFeature;
import org.springframework.stereotype.Repository;

@Repository
public class GroupFeatureDAO extends AbstractHibernateDAO<GroupFeature> {

    private static final String FEATURE = "feature";

	public List<GroupFeature> findByFeatures(List<String> roles) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.in(FEATURE, roles));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findByFeature(String role) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq(FEATURE, role));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findByGroups(List<String> groupIds) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.in("groupId", groupIds));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findByGroup(String groupId) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("groupId", groupId));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findByTag(String tag) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("tag", tag));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findEnabledByTag(String tag) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("tag", tag));
        criteria.add(Restrictions.eq("onByDefault", Boolean.TRUE));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public List<GroupFeature> findBannedByTag(String tag) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("tag", tag));
        criteria.add(Restrictions.eq("banned", Boolean.TRUE));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        return (List<GroupFeature>) criteria.list();
    }

    public GroupFeature findByGroupFeature(String groupId, String feature) {
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("groupId", groupId));
        criteria.add(Restrictions.eq(FEATURE, feature));
        criteria.add(Restrictions.eq("enabled", Boolean.TRUE));
        if (criteria.list().size() == 0) {
            return null;
        }
        return (GroupFeature)criteria.list().get(0);
    }
}
