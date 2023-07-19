/*
 * web: org.nrg.xnat.restlet.resources.ExperimentListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class ExperimentListResource  extends QueryOrganizerResource {
    public ExperimentListResource(final Context context, final Request request, final Response response) {
        super(context, request, response);
        getVariants().addAll(Arrays.asList(new Variant(MediaType.APPLICATION_JSON), new Variant(MediaType.TEXT_HTML), new Variant(MediaType.TEXT_XML)));
        fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.EXPERIMENT_DATA,true));
    }

    @Override
    public ArrayList<String> getDefaultFields(final GenericWrapperElement e) {
        final ArrayList<String> fields= new ArrayList<>();
        fields.add("ID");
        fields.add("project");
        fields.add("date");
        fields.add("xsiType");
        fields.add("label");
        fields.add("insert_date");
        if(e.instanceOf("xnat:subjectAssessorData")){
            fields.add("subject_ID");
            fields.add("subject_label");
        }
        if(e.instanceOf("xnat:imageAssessorData")){
            fields.add("session_ID");
            fields.add("session_label");
        }
        return fields;
    }

    public String getDefaultElementName(){
        return XnatExperimentdata.SCHEMA_ELEMENT_NAME;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Representation represent(final Variant variant) {
        Representation rep = super.represent(variant);
        if (rep != null) {
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
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        } catch (DBPoolException e) {
            logger.error("Error occurred connecting to database", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        } catch (Exception e) {
            logger.error("Unknown error occurred",e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }

        if(table==null){
        	return null;
        }

        MediaType mt = overrideVariant(variant);
        if(table!=null && !hasOffset) {
            params.put("totalRecords", table.size());
        }
        return representTable(table, mt, params);
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
	    	handlers = new ArrayList<>();

	    	//ordering here is important.  the last match wins
	    	handlers.add(new DefaultExperimentHandler());
	    	handlers.add(new RecentExperiments());
	    	final List<Class<?>> classes;
	        try {
	            classes = Reflection.getClassesForPackage("org.nrg.xnat.restlet.experimentsList.extensions");
	        } catch (Exception exception) {
	            throw new RuntimeException(exception);
	        }

	        for (final Class<?> clazz : classes) {
	            if (FilteredExptListHandlerI.class.isAssignableFrom(clazz)) {
	                handlers.add((FilteredExptListHandlerI)clazz.newInstance());
	            }
	        }
    	}

        return handlers;
    }

    //FilteredExptListHandlerI allows additional experiment list handlers to be added via modules
    public interface FilteredExptListHandlerI {
    	boolean canHandle(SecureResource resource);
    	XFTTable build(ExperimentListResource resource,Hashtable<String,Object> params) throws Exception;
    }

    //handles requests where ?recent=something
    private static class RecentExperiments implements FilteredExptListHandlerI {
		@Override
        public boolean canHandle(final SecureResource resource) {
            return StringUtils.isNotBlank(resource.getQueryVariable("recent"));
        }

		@Override
		public XFTTable build(final ExperimentListResource resource, final Hashtable<String, Object> params) throws Exception {
            params.put("title", "Recent Experiments");
            //this uses an ugly hack to try to enforces security via the SQL statement.  It generates the statement for the subject data type.  It then uses the same permissions on the experimentData type.  This assumes that experiments and subjects have the same permissions defined (which is currently always the case).  But, this could be an issue in the future.

            final int           days    = NumberUtils.toInt(resource.getQueryVariable("recent"), 60);
            final int           limit   = NumberUtils.toInt(resource.getQueryVariable("limit"), 60);
            final UserI         user    = resource.getUser();
            final StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM get_experiment_list('").append(user.getUsername()).append("'");
            if (limit != 60 && days != 60) {
                builder.append(", ").append(limit).append(", ").append(days);
            }
            builder.append(")");

            return XFTTable.Execute(builder.toString(), user.getDBName(), user.getUsername());
		}
    }

    //handles everything else
    private static class DefaultExperimentHandler implements FilteredExptListHandlerI {

		@Override
		public boolean canHandle(final SecureResource resource) {
			return true;
		}

		@Override
		public XFTTable build(final ExperimentListResource resource, final Hashtable<String, Object> params) throws Exception {
            final UserI user = resource.getUser();
            XFTTable table;
            params.put("title", "Matching experiments");
            String rootElementName=resource.getRootElementName();
            QueryOrganizer qo = QueryOrganizer.buildXFTQueryOrganizerWithClause(rootElementName, user);

            resource.populateQuery(qo);

            if(!ElementSecurity.IsSecureElement(rootElementName)){
                qo.addField("xnat:experimentData/extension_item/element_name");
                qo.addField("xnat:experimentData/project");
            }

            //inject paging
            final String query = qo.buildFullQuery() + " " + resource.buildOffsetFromParams(true);

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
                                SecurityValues values = new SecurityValues();
                                values.put(element_name + "/project",project);
                                ArrayList<String> sessionIdList = new ArrayList<>();
                                try{
                                    sessionIdList.add(row[0].toString());
                                } catch(Exception ignored){

                                }
                                if(Permissions.verifyAccessToSessions(XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class), user, sessionIdList).keySet().size() > 0) {
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
