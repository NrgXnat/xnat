package org.nrg.config.daos;

import java.util.List;
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
    	throw new UnsupportedOperationException();
    }
    
    @Override
    public void update(Configuration entity) {
    	throw new UnsupportedOperationException();
    }
    
	public void deleteAll(){
		throw new UnsupportedOperationException();
	}
	
	//FIX THESE COMMENTS. Be sure to put a comment on everything that says
	//this method returns null if nothing found... even for LIST<T> return
	//types. that makes it easy to know if nothing came back... no need to test the size.
	
	@SuppressWarnings("unchecked")
	public List<Configuration> getAll(){		
		Criteria criteria = getSession().createCriteria(getParameterizedType());
        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
	
	//this sort of method just has to be implemented in Hibernate somewhere... faster to code than find, i guess
	private void addNullableCriteria(Criteria c, String name, String value) {
		if(value == null){
			c.add(Restrictions.isNull(name));
		} else {
			c.add(Restrictions.eq(name, value));
		}
	}
	
	//I wanted to use hibernate's findByExample, but it ignores null parameters... so I had to write findBy.... methods:
	@SuppressWarnings("unchecked")
	public List<Configuration> findByToolPathProject(String tool, String path, String project){
		Criteria criteria = getSession().createCriteria(getParameterizedType());
		addNullableCriteria(criteria, "tool", tool);
		addNullableCriteria(criteria, "path", path);
		addNullableCriteria(criteria, "project", project);
		@SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
		
	@SuppressWarnings("unchecked")
	public List<Configuration> findByToolPathProjectStatus(String tool, String path, String project, String status){
		Criteria criteria = getSession().createCriteria(getParameterizedType());
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
        Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("path", path));

        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (Configuration) list.get(0);
	}
	
	public List<String> getTools(){
		return getTools(null);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getTools(String project){
		Query sql;
		if(project == null || "".equals(project)){
			sql = this.getSession().createQuery("SELECT distinct tool from Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT distinct tool from Configuration where project = :project ").setParameter("project", project);
		}
        @SuppressWarnings("rawtypes")
        List list = sql.list();
        return (list == null || list.size() == 0) ? null : list;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getProjects(String toolName){
		Query sql;
		if(toolName == null || "".equals(toolName)){
			sql = this.getSession().createQuery("SELECT distinct project from Configuration");
		} else {
			sql = this.getSession().createQuery("SELECT distinct project from Configuration where tool = :tool ").setParameter("tool", toolName);
		}
        @SuppressWarnings("rawtypes")
        List list = sql.list();
        return (list == null || list.size() == 0) ? null : list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Configuration> getConfigurationsByTool(String toolName, String projectID){
		Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("tool", toolName));
        if(!(projectID == null || "".equals(projectID))){
        	criteria.add(Restrictions.eq("project", projectID));
        }
        @SuppressWarnings("rawtypes")
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : list;
	}
}
