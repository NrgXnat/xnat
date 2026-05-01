/*
 * config: org.nrg.config.daos.ConfigurationDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ConfigurationDAO extends AbstractHibernateDAO<Configuration> {

    // Configurations are immutable, so override delete; update remains default, since "deleting"
    // a versioned configuration requires updating the existing row to a disabled state.
    @Override
    public void delete(Configuration entity) {
        if (entity.isUnversioned()) {
            super.delete(entity);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    //HEY, YOU. every method in here will return null
    //if nothing found... even for LIST<T> return
    //types. that makes it easy to know if nothing came back... no need to test the size.

    public List<Configuration> findByToolPathProject(String tool, String path, Scope scope, String entityId) {
        return findByProperties(parameters("tool", tool,
                                           "path", path,
                                           "scope", Scope.getEntityScope(scope, entityId),
                                           "entityId", entityId));
    }

    @SuppressWarnings("unused")
    public List<Configuration> findByToolPathProjectStatus(String tool, String path, Scope scope, String entityId, String status) {
        return findByProperties(parameters("tool", tool,
                                           "path", path,
                                           "scope", Scope.getEntityScope(scope, entityId),
                                           "entityId", entityId,
                                           "status", status));
    }

    /**
     * Attempts to find a configuration matching the submitted path values.
     * If no matching configuration is found, this method returns <b>null</b>.
     *
     * @param path The configuration path.
     *
     * @return A matching configuration, if it exists.
     */
    @SuppressWarnings("unused")
    public Configuration getConfigurationByPath(String path) {
        return findByUniqueProperty("path", path);
    }

    /**
     * Gets all the tool names associated with the site scope.
     *
     * @return The tool names associated with the site scope.
     */
    @SuppressWarnings("unused")
    public List<String> getTools() {
        return getTools(null, null);
    }

    /**
     * Gets all the tool names associated with the entity with the indicated ID.
     *
     * @param scope    The scope with which the entity ID is associated.
     * @param entityId The entity ID.
     *
     * @return The tool names associated with the entity with the indicated ID.
     */
    public List<String> getTools(Scope scope, String entityId) {
        Query<String> sql;
        if (StringUtils.isBlank(entityId)) {
            sql = getSession().createQuery("SELECT DISTINCT tool FROM Configuration", String.class);
        } else {
            sql = getSession().createQuery("SELECT DISTINCT tool FROM Configuration WHERE scope = :scope AND entityId = :entityId", String.class).setParameter("scope", scope).setParameter("entityId", entityId);
        }
//        sql.setCacheable(true);
        return sql.getResultList();
    }

    /**
     * Gets all the projects associated with the indicated tool name.
     *
     * @param toolName The tool name for which you want to retrieve a list of associated projects.
     *
     * @return The projects associated with the indicated tool name.
     */
    public List<String> getProjects(String toolName) {
        Query<String> sql;
        if (StringUtils.isBlank(toolName)) {
            sql = getSession().createQuery("SELECT distinct entityId from Configuration", String.class);
        } else {
            sql = getSession().createQuery("SELECT distinct entityId from Configuration where tool = :tool", String.class).setParameter("tool", toolName);
        }
//        sql.setCacheable(true);
        return sql.getResultList();
    }

    public List<Configuration> getConfigurationsByTool(String toolName, Scope scope, String entityId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tool", toolName);
        if (scope != null) {
            parameters.put("scope", scope);
        }
        if (StringUtils.isNotBlank(entityId)) {
            parameters.put("entityId", entityId);
        }
        return findByProperties(parameters);
    }
}
