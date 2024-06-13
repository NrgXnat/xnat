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
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Repository
public class ConfigurationDAO  extends AbstractHibernateDAO<Configuration> {

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
	
	@SuppressWarnings("unchecked")
	public List<Configuration> findByToolPathProject(String tool, String path, Scope scope, String entityId){
        Criteria criteria = getCriteriaForType();
		addNullableCriteria(criteria, "tool", tool);
		addNullableCriteria(criteria, "path", path);
		addNullableCriteria(criteria, "scope", scope == null ? null : (StringUtils.isBlank(entityId) ? Scope.Site : scope));
		addNullableCriteria(criteria, "entityId", entityId);
		@SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}

	@SuppressWarnings({"unchecked", "unused"})
	public List<Configuration> findByToolPathProjectStatus(String tool, String path, Scope scope, String entityId, String status){
		Criteria criteria = getCriteriaForType();
		addNullableCriteria(criteria, "tool", tool);
		addNullableCriteria(criteria, "path", path);
		addNullableCriteria(criteria, "scope", scope);
		addNullableCriteria(criteria, "entityId", entityId);
		addNullableCriteria(criteria, "status", status);
		@SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}

	/**
     * Attempts to find a configuration matching the submitted path values.
     * If no matching configuration is found, this method returns <b>null</b>.
     * @param path The configuration path.
     * @return A matching configuration, if it exists.
     */
	@SuppressWarnings("unused")
	public Configuration getConfigurationByPath(String path){
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("path", path));

        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (Configuration) list.get(0);
	}

    /**
     * Gets all of the tool names associated with the site scope.
     * @return The tool names associated with the site scope.
     */
	@SuppressWarnings({"unchecked", "unused"})
	public List<String> getTools(){
		return getTools(null, null);
	}

    /**
     * Gets all of the tool names associated with the entity with the indicated ID.
     * @param scope    The scope with which the entity ID is associated.
	 * @param entityId The entity ID.
     * @return The tool names associated with the entity with the indicated ID.
     */
	@SuppressWarnings("unchecked")
	public List<String> getTools(Scope scope, String entityId){
		Query sql;
		if(StringUtils.isBlank(entityId)){
			sql = this.getSession().createQuery("SELECT DISTINCT tool FROM Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT DISTINCT tool FROM Configuration WHERE scope = :scope AND entityId = :entityId").setParameter("scope", scope).setString("entityId", entityId);
		}
        sql.setCacheable(true);
        @SuppressWarnings("rawtypes")
        List list = sql.list();
        return (list == null || list.size() == 0) ? null : list;
	}

    /**
     * Gets all of the projects associated with the indicated tool name.
     * @param toolName    The tool name for which you want to retrieve a list of associated projects.
     * @return The projects associated with the indicated tool name.
     */
	@SuppressWarnings("unchecked")
	public List<String> getProjects(String toolName){
		Query sql;
		if(StringUtils.isBlank(toolName)){
			sql = this.getSession().createQuery("SELECT distinct entityId from Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT distinct entityId from Configuration where tool = :tool").setParameter("tool", toolName);
		}
        sql.setCacheable(true);
        @SuppressWarnings("rawtypes")
        List list = sql.list();
        return (list == null || list.size() == 0) ? null : list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Configuration> getConfigurationsByTool(String toolName, Scope scope, String entityId){
		Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("tool", toolName));
		if (scope != null) {
			criteria.add(Restrictions.eq("scope", scope));
		}
		if(StringUtils.isNotBlank(entityId)){
        	criteria.add(Restrictions.eq("entityId", entityId));
        }
        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
}
