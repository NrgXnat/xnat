/*
 * web: org.nrg.xnat.restlet.resources.ProjectSubjectList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatProjectparticipant;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
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
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.Hashtable;

public class ProjectSubjectList extends QueryOrganizerResource {

	public static final String SUBJ_OWNED_PROJECT = XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/project";
	public static final String SUBJ_SHARED_PROJECT = XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/sharing/share/project";
	public static final String SUBJ_ID_PATH = XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/ID";

	XnatProjectdata proj=null;
	
	public ProjectSubjectList(Context context, Request request, Response response) {
		super(context, request, response);
		
			String pID= (String)getParameter(request,"PROJECT_ID");
			if(pID!=null){
				proj = XnatProjectdata.getProjectByIDorAlias(pID, getUser(), false);
				

				if(proj!=null){
					this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
					this.getVariants().add(new Variant(MediaType.TEXT_HTML));
					this.getVariants().add(new Variant(MediaType.TEXT_XML));					
				}else{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}else{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	
		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.SUBJECT_DATA,true));
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		XFTItem item;

		try {
			item = this.loadItem("xnat:subjectData", true);

			final UserI user = getUser();
			if (item == null) {
				item = XFTItem.NewItem("xnat:subjectData", user);
			}

			if (item.instanceOf("xnat:subjectData")) {
				XnatSubjectdata sub = new XnatSubjectdata(item);

				if (sub.getExperiments_experiment().size() > 0) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted subject record must not include subject assessors.");
					return;
				}

				if (this.proj == null && sub.getProject() != null) {
					proj = XnatProjectdata.getXnatProjectdatasById(sub.getProject(), user, false);
				}

				if (this.proj != null) {
					if (sub.getProject() == null || sub.getProject().equals("")) {
						sub.setProject(this.proj.getId());
					} else if (sub.getProject().equals(this.proj.getId())) {
					} else {
						boolean matched = false;
						for (XnatProjectparticipantI pp : sub.getSharing_share()) {
							if (pp.getProject().equals(this.proj.getId())) {
								matched = true;
								break;
							}
						}

						if (!matched) {
							final XnatProjectparticipant participant = new XnatProjectparticipant(user);
							participant.setProject(proj.getId());
							sub.setSharing_share(participant);
						}
					}
				} else {
					getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Submitted subject record must include the project attribute.");
					return;
				}

				XnatSubjectdata existing = null;
				if (sub.getId() != null) {
					existing = XnatSubjectdata.getXnatSubjectdatasById(sub.getId(), user, completeDocument);
				}

				if (existing == null && sub.getProject() != null && sub.getLabel() != null) {
					existing = XnatSubjectdata.GetSubjectByProjectIdentifier(sub.getProject(), sub.getLabel(), user, completeDocument);
				}

				if (existing == null) {
					for (XnatProjectparticipantI pp : sub.getSharing_share()) {
						existing = XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), pp.getLabel(), user, completeDocument);
						if (existing != null) {
							break;
						}
					}
				}

				if (existing == null) {
					if (!Permissions.canCreate(user, sub)) {
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient create privileges for subjects in this project.");
						return;
					}
					//IS NEW
					if (StringUtils.isBlank(sub.getId())) {
						sub.setId(XnatSubjectdata.CreateNewID());
					}
				} else {
					this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Subject already exists.");
					return;
				}


				if (!validateSubject(sub)) {
					return;
				}

				create(sub, false, false, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(sub.getXSIType(), true)));

				postSaveManageStatus(sub);

				this.returnSuccessfulCreateFromList(sub.getId());
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Only xnat:Subject documents can be PUT to this address.");
			}
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(), e.getMessage());
		} catch (SAXParseException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.error("", e);
		}
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
		XFTTable table = null;
		if(proj!=null){
			final Representation rep=super.represent(variant);
			if(rep!=null)return rep;
			
			try {
				final UserI user = getUser();

				final SecurityValues values = new SecurityValues();
				values.put(SUBJ_OWNED_PROJECT, proj.getId());
				values.put(SUBJ_SHARED_PROJECT, proj.getId());

				final SchemaElement se= SchemaElement.GetElement(XnatSubjectdata.SCHEMA_ELEMENT_NAME);

				if (!Permissions.canRead(user,se,values))
				{
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Unable to read subjects for Project: " + proj.getId());
					return null;
				}

				final QueryOrganizer qo = QueryOrganizer.buildXFTQueryOrganizerWithClause(this.getRootElementName(), user);
	            
				this.populateQuery(qo);

				final CriteriaCollection cc= new CriteriaCollection("OR");
				cc.addClause(SUBJ_OWNED_PROJECT, proj.getId());
				cc.addClause(SUBJ_SHARED_PROJECT, proj.getId());

				qo.addWhere(cc);

				//inject paging
				final String query = qo.buildFullQuery() + " " + this.buildOffsetFromParams(false);

				table = XFTTable.Execute(query, user.getDBName(), userName);

				table = formatHeaders(table, qo, SUBJ_ID_PATH,
						"/data/subjects/");
				
				final Integer labelI=table.getColumnIndex("label");
				final Integer idI=table.getColumnIndex("ID");
				if(labelI!=null && idI!=null){
					final XFTTable t= XFTTable.Execute("SELECT subject_id,label FROM xnat_projectParticipant WHERE project='"+ proj.getId() + "'", user.getDBName(), user.getUsername());
					final Hashtable lbls=t.toHashtable("subject_id", "label");
					for(Object[] row:table.rows()){
						final String id=(String)row[idI];
						if(lbls.containsKey(id)){
							final String lbl=(String)lbls.get(id);
							if(null!=lbl && !lbl.equals("")){
								row[labelI]=lbl;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}

			final MediaType mt = overrideVariant(variant);
			final Hashtable<String, Object> params = new Hashtable<String, Object>();
			if (table != null)
				params.put("totalRecords", table.size());
			return this.representTable(table, mt, params);
		}
		
		final Hashtable<String,Object> params=new Hashtable<String,Object>();
		params.put("title", "Project Subjects");

		final MediaType mt = overrideVariant(variant);

		if(table!=null)params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}
