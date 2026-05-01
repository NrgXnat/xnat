/*
 * core: org.nrg.xdat.daos.GroupFeatureDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.daos;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.entities.GroupFeature;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Slf4j
public class GroupFeatureDAO extends AbstractHibernateDAO<GroupFeature> {
    private static final String FEATURE       = "feature";
    private static final String GROUP_ID      = "groupId";
    private static final String TAG           = "tag";
    public static final  String ON_BY_DEFAULT = "onByDefault";
    public static final  String BANNED        = "banned";

    public List<GroupFeature> findByFeatures(List<String> roles) {
        return findByProperty(FEATURE, roles);
    }

    public List<GroupFeature> findByFeature(String role) {
        return findByFeatures(Collections.singletonList(role));
    }

    public List<GroupFeature> findByGroups(List<String> groupIds) {
        return findByProperty(GROUP_ID, groupIds);
    }

    public List<GroupFeature> findByGroup(String groupId) {
        return findByGroups(Collections.singletonList(groupId));
    }

    public List<GroupFeature> findByTag(String tag) {
        return findByProperty(TAG, tag);
    }

    public List<GroupFeature> findEnabledByTag(String tag) {
        return findByProperties(parameters(TAG, tag, ON_BY_DEFAULT, Boolean.TRUE));
    }

    public List<GroupFeature> findBannedByTag(String tag) {
        return findByProperties(parameters(TAG, tag, BANNED, Boolean.TRUE));
    }

    public GroupFeature findByGroupFeature(String groupId, String feature) {
        return findByUniqueProperties(parameters(GROUP_ID, groupId, FEATURE, feature));
    }
}
