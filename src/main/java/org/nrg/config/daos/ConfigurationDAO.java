/*
 * org.nrg.config.daos.ConfigurationDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.nrg.config.entities.Configuration;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationDAO  extends AbstractHibernateDAO<Configuration> {

	//Configurations are immutable, so override delete and update
    @Override
    public void delete(Configuration entity) {
        if (entity.isUnversioned()) {
            super.delete(entity);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void update(Configuration entity) {
    	if (entity.isUnversioned()) {
            super.update(entity);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    //HEY, YOU. every method in here will return null
	//if nothing found... even for LIST<T> return
	//types. that makes it easy to know if nothing came back... no need to test the size.
	
	@SuppressWarnings("unchecked")
	public List<Configuration> getAll(){
        Criteria criteria = getCriteriaForType();
        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}

    //I wanted to use hibernate's findByExample, but it ignores null parameters... so I had to write findBy.... methods:
	@SuppressWarnings("unchecked")
	public List<Configuration> findByToolPathProject(String tool, String path, Long project){
        Criteria criteria = getCriteriaForType();
		addNullableCriteria(criteria, "tool", tool);
		addNullableCriteria(criteria, "path", path);
		addNullableCriteria(criteria, "project", project);
		@SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
		
	@SuppressWarnings("unchecked")
	public List<Configuration> findByToolPathProjectStatus(String tool, String path, Long project, String status){
		Criteria criteria = getCriteriaForType();
		addNullableCriteria(criteria, "tool", tool);
		addNullableCriteria(criteria, "path", path);
		addNullableCriteria(criteria, "project", project);
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
	public Configuration getConfigurationByPath(String path){
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("path", path));

        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (Configuration) list.get(0);
	}

    /**
     * Gets all of the tool names associated with the project with the indicated ID.
     * @param project    The ID of the project for which you want to retrieve a list of tool names.
     * @return The tool names associated with the project with the indicated ID.
     */
	@SuppressWarnings("unchecked")
	public List<String> getTools(Long project){
		Query sql;
		if(project == null){
			sql = this.getSession().createQuery("SELECT distinct tool from Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT distinct tool from Configuration where project = :project ").setParameter("project", project);
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
	public List<Long> getProjects(String toolName){
		Query sql;
		if(StringUtils.isBlank(toolName)){
			sql = this.getSession().createQuery("SELECT distinct project from Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT distinct project from Configuration where tool = :tool ").setParameter("tool", toolName);
		}
        sql.setCacheable(true);
        @SuppressWarnings("rawtypes")
        List list = sql.list();
        return (list == null || list.size() == 0) ? null : list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Configuration> getConfigurationsByTool(String toolName, Long projectID){
		Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("tool", toolName));
        if(!(projectID == null || "".equals(projectID))){
        	criteria.add(Restrictions.eq("project", projectID));
        }
        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
}
