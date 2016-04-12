/*
 * org.nrg.xft.services.impl.hibernate.HibernateXftFieldExclusionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xft.services.impl.hibernate;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xft.daos.XftFieldExclusionDAO;
import org.nrg.xft.entities.XftFieldExclusion;
import org.nrg.xft.entities.XftFieldExclusionScope;
import org.nrg.xft.services.XftFieldExclusionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateXftFieldExclusionService extends AbstractHibernateEntityService<XftFieldExclusion, XftFieldExclusionDAO> implements XftFieldExclusionService {

    private static final String[] EXCLUSION_PROPERTIES_SYSTEM_SCOPE = AbstractHibernateEntity.getExcludedProperties("targetId", "pattern");
    private static final String[] EXCLUSION_PROPERTIES_TARGET_SCOPE = AbstractHibernateEntity.getExcludedProperties("pattern");

    @Override
    @Transactional
    public List<XftFieldExclusion> getSystemExclusions() {
        XftFieldExclusion example = new XftFieldExclusion();
        example.setScope(XftFieldExclusionScope.System);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_SYSTEM_SCOPE);
    }

    @Override
    @Transactional
    public List<XftFieldExclusion> getExclusionsForScopedTarget(XftFieldExclusionScope scope, String targetId) {
        XftFieldExclusion example = new XftFieldExclusion();
        example.setScope(scope);
        example.setTargetId(targetId);
        return _dao.findByExample(example, EXCLUSION_PROPERTIES_TARGET_SCOPE);
    }

    @Override
    public String validate(XftFieldExclusion exclusion) {
        if (exclusion == null) {
            return "Your exclusion object can not be null.";
        }
        switch (exclusion.getScope()) {
            case System:
                if (!StringUtils.isBlank(exclusion.getTargetId())) {
                    return "System-scoped exclusions should not have a target ID.";
                }
                break;
            case Project:
                if (StringUtils.isBlank(exclusion.getTargetId())) {
                    return "Project-scoped exclusions must have a target ID.";
                }
                break;
            case DataType:
                if (StringUtils.isBlank(exclusion.getTargetId())) {
                    return "DataType-scoped exclusions must have a target ID.";
                }
                break;
        }
        return null;
    }

    @Inject
    private XftFieldExclusionDAO _dao;
}
