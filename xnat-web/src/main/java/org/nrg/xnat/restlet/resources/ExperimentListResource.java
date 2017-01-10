/*
 * web: org.nrg.xnat.restlet.resources.ExperimentListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import com.google.common.collect.Lists;

public class ExperimentListResource  extends QueryOrganizerResource {
    
    public ExperimentListResource(org.restlet.Context context, Request request, Response response) {
        super(context, request, response);
            this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
            this.getVariants().add(new Variant(MediaType.TEXT_HTML));
            this.getVariants().add(new Variant(MediaType.TEXT_XML));
        
            this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.EXPERIMENT_DATA,true));
        
        }
    
    
    
    @Override
    public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
        ArrayList<String> al= new ArrayList<>();
        
        al.add("ID");
        al.add("project");
        al.add("date");
        al.add("xsiType");
        al.add("label");
        al.add("insert_date");
        if(e.instanceOf("xnat:subjectAssessorData")){
            al.add("subject_ID");
            al.add("subject_label");
        }

        if(e.instanceOf("xnat:imageAssessorData")){
            al.add("session_ID");
            al.add("session_label");
        }
        
        return al;
    }

    public String getDefaultElementName(){
        return "xnat:experimentData";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Representation represent(Variant variant) {
        Representation rep=super.represent(variant);
        if(rep!=null) {
            return rep;
        }
        
        XFTTable table;
        
        FilteredExptListHandlerI handler=null;
        try {
			for(FilteredExptListHandlerI filter:getHandlers()){
				if(filter.canHandle(this)){
					handler=filter;
				}
			}
		} catch (InstantiationException | IllegalAccessException e1) {
			logger.error("",e1);
		}
		
        Hashtable<String,Object> params= new Hashtable<>();

        try {
	        if(handler!=null){
	        	table=handler.build(this,params);
	        }else{
	        	//unable to identify experiment list filter... this shouldn't happen
	        	table =null;
	        }
	            
        } catch (SQLException e) {
            logger.error("Error occurred executing database query", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        } catch (DBPoolException e) {
            logger.error("Error occurred connecting to database", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        } catch (Exception e) {
            logger.error("Unknown error occurred",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
        
        if(table==null){
        	return null;
        }

        MediaType mt = overrideVariant(variant);
        if(table!=null) {
            params.put("totalRecords", table.size());
        }
        return this.representTable(table, mt, params);
    }
    
    private static List<FilteredExptListHandlerI> handlers=null;
    
    /**
     * Get a list of the possible experiment handlers.  This allows additional handlers to be injected at a later date or via a module.
     * @return The list of handlers.
     * @throws InstantiationException When an error occurs creating an object.
     * @throws IllegalAccessException When an error occurs accessing an object.
     */
    public static List<FilteredExptListHandlerI> getHandlers() throws InstantiationException, IllegalAccessException{
    	if(handlers==null){
	    	handlers=Lists.newArrayList();
	    	
	    	//ordering here is important.  the last match wins
	    	handlers.add(new DefaultExperimentHandler());
	    	handlers.add(new RecentExperiments());
	    	List<Class<?>> classes;
	        try {
	            classes = Reflection.getClassesForPackage("org.nrg.xnat.restlet.experimentsList.extensions");
	        } catch (Exception exception) {
	            throw new RuntimeException(exception);
	        }
	
	        for (Class<?> clazz : classes) {
	            if (clazz.isInstance(FilteredExptListHandlerI.class)) {
	            	handlers.add((FilteredExptListHandlerI)clazz.newInstance());
	            }
	        }
    	}
        
        return handlers;
    }
    
    //FilteredExptListHandlerI allows additional experiment list handlers to be added via modules
    interface FilteredExptListHandlerI {
    	boolean canHandle(SecureResource resource);
    	XFTTable build(ExperimentListResource resource,Hashtable<String,Object> params) throws Exception;
    }

    //handles requests where ?recent=something
    private static class RecentExperiments implements FilteredExptListHandlerI {

		@Override
		public boolean canHandle(SecureResource resource) {
			return (resource.getQueryVariable("recent")!=null);
		}

		@Override
		public XFTTable build(ExperimentListResource resource, Hashtable<String, Object> params) throws Exception {
			XFTTable table;
            params.put("title", "Recent Experiments");
            //this uses an ugly hack to try to enforces security via the SQL statement.  It generates the statement for the subject data type.  It then uses the same permissions on the experimentData type.  This assumes that experiments and subjects have the same permissions defined (which is currently always the case).  But, this could be an issue in the future.

            boolean limit=false;
            int days = 0;
            if(resource.getQueryVariable("recent")!=null){
                String daysS=resource.getQueryVariable("recent");
                try {
                    days= Integer.parseInt(daysS);
                } catch (NumberFormatException e) {
                    limit=true;
                    days=60;
                }
            }

            //experiments
            VelocityContext context= new VelocityContext();
            context.put("time", Calendar.getInstance().getTime());

            final UserI user = resource.getUser();

            StringBuilder builder=new StringBuilder();
            builder.append("SELECT * FROM (SELECT DISTINCT ON (expt.id) expt.id,perm.label,perm.project,date,status, workflow_status, xme.element_name, COALESCE(es.code,es.singular,es.element_name) AS TYPE_DESC,insert_date,activation_date,last_modified,workflow_date,pipeline_name, COALESCE(workflow_date,last_modified,insert_date)  AS action_date FROM xnat_experimentData expt LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id LEFT JOIN xnat_experimentData_meta_data emd ON expt.experimentData_info=emd.meta_data_id LEFT JOIN xdat_element_security es ON xme.element_name=es.element_name LEFT JOIN (   SELECT DISTINCT ON (id) id,launch_time AS workflow_date,CASE pipeline_name WHEN 'Transferred'::text THEN 'Archived'::text ELSE CASE xs_lastposition('/'::text, pipeline_name::text) WHEN 0 THEN pipeline_name ELSE substring(substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1), 1, xs_lastposition('.'::text, substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1)) - 1) END END AS pipeline_name,status AS workflow_status FROM wrk_workflowdata WHERE category!='SIDE_ADMIN' AND launch_time > (NOW() - interval '").append(days).append(" day') AND status!='Failed (Dismissed)' AND pipeline_name NOT LIKE 'xnat_tools%AutoRun.xml' ORDER BY id,launch_time DESC ) wrkflw ON expt.id=wrkflw.id RIGHT JOIN (");
            if(Groups.isMember(user, "ALL_DATA_ACCESS") || Groups.isMember(user, "ALL_DATA_ADMIN")){
            	 builder.append("SELECT DISTINCT ON (isd.ID) isd.ID, label, project FROM xnat_imageSessionData isd LEFT JOIN xnat_experimentData expt ON isd.ID=expt.ID");
            }else{
            	builder.append("SELECT DISTINCT ON (ID) ID, label, project FROM (").append(Permissions.getUserPermissionsSQL(user)).append(") perms INNER JOIN (SELECT isd.id, element_name || '/project' as field, expt.project, expt.label FROM xnat_imageSessionData isd LEFT JOIN xnat_experimentData expt ON isd.id=expt.id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id UNION SELECT expt.id,xme.element_name || '/sharing/share/project', shr.project, shr.label  FROM xnat_experimentData_share shr LEFT JOIN xnat_experimentData expt ON expt.id=shr.sharing_share_xnat_experimentda_id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id) expts ON perms.field=expts.field AND perms.field_value=expts.project");
            }
            builder.append(") perm ON expt.id=perm.id ");

            builder.append(" RIGHT JOIN xnat_imageSessionData isd ON perm.id=isd.id  ");
            builder.append(" WHERE (insert_date > (NOW() - interval '").append(days).append(" day') OR activation_date > (NOW() - interval '").append(days).append(" day') OR last_modified > (NOW() - interval '").append(days).append(" day') OR workflow_date > (NOW() - interval '").append(days).append(" day')) ");
            builder.append(" )SEARCH ORDER BY action_date DESC");
            if(limit) {
            	 builder.append(" LIMIT 60");
            }

            table=XFTTable.Execute(builder.toString(), user.getDBName(), resource.userName);

            return table;
		}
    	
    }
    //handles everything else
    private static class DefaultExperimentHandler implements FilteredExptListHandlerI {

		@Override
		public boolean canHandle(SecureResource resource) {
			return true;
		}

		@Override
		public XFTTable build(ExperimentListResource resource,Hashtable<String, Object> params) throws Exception {
            final UserI user = resource.getUser();
			XFTTable table;
			params.put("title", "Matching experiments");
            String rootElementName=resource.getRootElementName();
            QueryOrganizer qo = new QueryOrganizer(rootElementName,user,ViewManager.ALL);

            resource.populateQuery(qo);

            if(!ElementSecurity.IsSecureElement(rootElementName)){
                qo.addField("xnat:experimentData/extension_item/element_name");
                qo.addField("xnat:experimentData/project");
            }

            String query=qo.buildQuery();

            table=XFTTable.Execute(query, user.getDBName(), resource.userName);

            if(!ElementSecurity.IsSecureElement(rootElementName)){
                List<Object[]> remove= new ArrayList<>();
                Hashtable<String, Boolean> checked = new Hashtable<>();

                String enS=qo.getFieldAlias("xnat:experimentData/extension_item/element_name");
                if(enS==null) {
                    logger.warn("Couldn't find property xnat:experimentData/extension_item/element_name for search");
                    resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    return null;
                }

                Integer en=table.getColumnIndex(enS.toLowerCase());
                Integer p=table.getColumnIndex(qo.getFieldAlias("xnat:experimentData/project").toLowerCase());

                for(Object[] row : table.rows()) {
                    String element_name=(String)row[en];
                    String project=(String)row[p];
                    try{
                        if(project==null || element_name==null){
                            remove.add(row);
                        }else{

                            if(!checked.containsKey(element_name+project)){
                                SchemaElementI secureElement = SchemaElement.GetElement(element_name);

                                SecurityValues values = new SecurityValues();
                                values.put(element_name + "/project",project);

                                if (Permissions.canRead(user,secureElement,values)) {
                                    checked.put(element_name+project, Boolean.TRUE);
                                }else{
                                    checked.put(element_name+project, Boolean.FALSE);
                                }
                            }

                            if(!checked.get(element_name + project)){
                                remove.add(row);
                            }
                        }
                    } catch (Throwable e) {
                        logger.debug("Problem occurred iterating secure elements", e);
                        remove.add(row);
                    }
                }

                table.rows().removeAll(remove);
            }

            if(table.size()>0){
                table=resource.formatHeaders(table,qo,rootElementName+"/ID","/data/experiments/");
            }
        	return table;
        }
		
    }
}
