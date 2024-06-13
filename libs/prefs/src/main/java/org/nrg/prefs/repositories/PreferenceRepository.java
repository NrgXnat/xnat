/*
 * prefs: org.nrg.prefs.repositories.PreferenceRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.repositories;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages preferences within the preferences service framework.
 */
@SuppressWarnings("JpaQlInspection")
@Repository
public class PreferenceRepository extends AbstractHibernateDAO<Preference> {

    public static final String PREFS_BY_TOOL_AND_ENTITY      = "SELECT pref FROM Preference pref LEFT JOIN pref.tool Tool WHERE Tool.toolId = :toolId AND pref.scope = :scope";
    public static final String PREFS_BY_TOOL_PREF_AND_ENTITY = "SELECT pref FROM Preference pref LEFT JOIN pref.tool Tool WHERE Tool.toolId = :toolId AND pref.name = :name AND pref.scope = :scope";
    public static final String PREF_ENTITY_WHERE             = " AND pref.entityId = :entityId";

    public Preference findByToolIdNameAndEntity(final String toolId, final String preferenceName, final Scope scope, final String entityId) {
        final boolean hasEntityId = StringUtils.isNotBlank(entityId);
        final Query query = getSession().createQuery(PREFS_BY_TOOL_PREF_AND_ENTITY + getEntityWhere(hasEntityId))
                                        .setString("toolId", toolId)
                                        .setString("name", preferenceName)
                                        .setInteger("scope", scope == null ? EntityId.Default.getScope().ordinal() : scope.ordinal()).setCacheable(true);
        if (hasEntityId) {
            query.setString("entityId", resolveEntityId(entityId));
        }

        return getEntityFromResult(query.uniqueResult());
    }

    @SuppressWarnings("unused")
    public Preference findByToolNameAndEntity(final Tool tool, final String preferenceName, final Scope scope, final String entityId) {
        // TODO: This doesn't work. It seems like a bug. With two tools, this will find existing preferences for the first tool when trying to create preferences for the second, even though the tool is set to the second instance.
        // private static final String[] EXCLUDE_PROPERTY = new String[]{"id", "enabled", "created", "timestamp", "disabled", "value"};
        // final Preference example = new Preference();
        // example.setTool(tool);
        // example.setName(preferenceName);
        // final List<Preference> found = findByExample(example, EXCLUDE_PROPERTY);
        // if (found.size() == 0) {
        //     return null;
        // }
        // return found.get(0);
        return findByToolIdNameAndEntity(tool.getToolId(), preferenceName, scope, entityId);
    }

    public List<Preference> findByToolIdAndEntity(final String toolId, final Scope scope, final String entityId) {
        final boolean hasEntityId = StringUtils.isNotBlank(entityId);
        final Query query = getSession().createQuery(PREFS_BY_TOOL_AND_ENTITY + (getEntityWhere(hasEntityId)))
                                        .setString("toolId", toolId)
                                        .setInteger("scope", scope == null ? EntityId.Default.getScope().ordinal() : scope.ordinal());
        if (hasEntityId) {
            query.setString("entityId", resolveEntityId(entityId));
        }

        @SuppressWarnings("all")
        final List results = query.list();
        if (results == null || results.size() == 0) {
            return new ArrayList<>();
        }
        final List<Preference> preferences = new ArrayList<>();
        for (final Object result : results) {
            final Preference preference = getEntityFromResult(result);
            if (preference != null) {
                preferences.add(preference);
            }
        }
        return preferences;
    }

    private static String resolveEntityId(final String entityId) {
        return StringUtils.isBlank(entityId) ? EntityId.Default.getEntityId() : entityId;
    }

    private static String getEntityWhere(final boolean hasEntityId) {
        return hasEntityId ? PREF_ENTITY_WHERE : "";
    }
}
