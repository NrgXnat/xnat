/*
 * web: org.nrg.xnat.restlet.resources.SubjectListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import static org.nrg.xdat.security.helpers.Permissions.getUserProjectAccess;

public class SubjectListResource extends QueryOrganizerResource {
	public SubjectListResource(Context context, Request request, Response response) {
		super(context, request, response);
		
		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));
		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.SUBJECT_DATA,true));
	}
	
	
	
	@Override
	public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
		ArrayList<String> al= new ArrayList<>();
		
		al.add("ID");
		al.add("project");
		al.add("label");
		al.add("insert_date");
		al.add("insert_user");
		
		return al;
	}

	public String getDefaultElementName(){
		return "xnat:subjectData";
	}

	@Override
	public Representation represent(Variant variant) {
		Representation rep=super.represent(variant);
		if(rep!=null)return rep;
			
		XFTTable table;
		try {
			final UserI user = getUser();

			List<String> readableProjects = Permissions.getReadableProjects(user);
           	List<String> protectedProjects = Permissions.getAllProtectedProjects(XDAT.getJdbcTemplate());
			Collection<String> readableExcludingProtected = CollectionUtils.subtract(readableProjects,protectedProjects);
			if(readableExcludingProtected.size()<=0){//Projects user can see, excluding those that they might only be seeing because they are protected
				boolean hasExplicitAccessToAtLeastOneProtectedProject = false;
				for(String protectedProject: protectedProjects){
					if(StringUtils.isNotBlank(getUserProjectAccess(user, protectedProject))){
						hasExplicitAccessToAtLeastOneProtectedProject = true;
					}
				}
				if(!hasExplicitAccessToAtLeastOneProtectedProject) {
					throw new IllegalAccessException("The user is trying to search for data, but does not have access to any projects.");
				}
			}

			QueryOrganizer qo = QueryOrganizer.buildXFTQueryOrganizerWithClause(this.getRootElementName(), user);

			this.populateQuery(qo);

			//inject paging
			final String query = qo.buildFullQuery() + " " + this.buildOffsetFromParams();

			table = XFTTable.Execute(query, user.getDBName(), userName);

			table = formatHeaders(table, qo, "xnat:subjectData/ID",
					"/data/subjects/");
		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}

		MediaType mt = overrideVariant(variant);
		Hashtable<String, Object> params = new Hashtable<>();
		if (table != null && !hasOffset)
			params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}
