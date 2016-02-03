/*
 * org.nrg.xnat.restlet.resources.SubjectListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;

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
		ArrayList<String> al=new ArrayList<String>();
		
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
	public Representation getRepresentation(Variant variant) {
		Representation rep=super.getRepresentation(variant);
		if(rep!=null)return rep;
			
		XFTTable table;
		try {
			QueryOrganizer qo = new QueryOrganizer(this.getRootElementName(), user,
					ViewManager.ALL);

			this.populateQuery(qo);

			String query = qo.buildQuery();

			table = XFTTable.Execute(query, user.getDBName(), userName);

			table = formatHeaders(table, qo, "xnat:subjectData/ID",
					"/data/subjects/");
		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}

		MediaType mt = overrideVariant(variant);
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if (table != null)
			params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}
