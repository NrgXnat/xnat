/*
 * prefs: org.nrg.prefs.repositories.PreferenceRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.repositories;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.springframework.stereotype.Repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages preferences within the preferences service framework.
 */
@Repository
@Slf4j
public class PreferenceRepository extends AbstractHibernateDAO<Preference> {
    public Preference findByToolIdNameAndEntity(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        try {
            return getPreferenceQuery(toolId, preferenceName, scope, entityId).getSingleResult();
        } catch (NoResultException e) {
            log.info("No preference found for tool ID {}, preference {}, scope {}, and entity ID {}", toolId, preferenceName, scope, entityId);
            return null;
        }
    }

    @SuppressWarnings("unused")
    public Preference findByToolNameAndEntity(final Tool tool, final String preferenceName, final Scope scope, final String entityId) {
        return findByToolIdNameAndEntity(tool.getToolId(), preferenceName, scope, entityId);
    }

    public List<Preference> findByToolIdAndEntity(final String toolId, final Scope scope, final String entityId) {
        return getPreferenceQuery(toolId, null, scope, entityId).getResultList();
    }

    private TypedQuery<Preference> getPreferenceQuery(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        int resolvedScope = scope == null ? EntityId.Default.getScope().ordinal() : scope.ordinal();

        CriteriaBuilder           criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<Preference> criteriaQuery   = criteriaBuilder.createQuery(Preference.class);

        Root<Preference>       root = criteriaQuery.from(Preference.class);
        Join<Preference, Tool> join = root.join("tool", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get(Preference.PROPERTY_SCOPE), resolvedScope));
        predicates.add(criteriaBuilder.equal(join.get(Tool.PROPERTY_TOOL_ID), toolId));
        if (StringUtils.isNotBlank(preferenceName)) {
            predicates.add(criteriaBuilder.equal(root.get(Preference.PROPERTY_NAME), preferenceName));
        }
        if (StringUtils.isNotBlank(entityId)) {
            predicates.add(criteriaBuilder.equal(root.get(Preference.PROPERTY_ENTITY_ID), resolveEntityId(entityId)));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0]))));
        return createQuery(criteriaQuery);
    }

    private static String resolveEntityId(final String entityId) {
        return StringUtils.isBlank(entityId) ? EntityId.Default.getEntityId() : entityId;
    }
}
