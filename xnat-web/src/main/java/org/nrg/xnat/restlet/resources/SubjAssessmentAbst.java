/*
 * org.nrg.xnat.restlet.resources.SubjAssessmentAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.apache.log4j.Logger;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.ArrayList;

public class SubjAssessmentAbst extends QueryOrganizerResource {
    static Logger logger = Logger.getLogger(SubjAssessmentAbst.class);
    
	public SubjAssessmentAbst(Context context, Request request,
			Response response) {
		super(context, request, response);
	}

                
	@Override
	public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
		ArrayList<String> al=new ArrayList<String>();
		
		al.add("ID");
		al.add("project");
		al.add("date");
		al.add("xsiType");
		al.add("label");
		al.add("insert_date");
		
		return al;
	}

	public String getDefaultElementName(){
		return "xnat:subjectAssessorData";
	}
}