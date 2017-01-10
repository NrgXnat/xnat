/*
 * web: org.nrg.xnat.restlet.resources.ProjSubExptAsstList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
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
import java.util.List;

public class ProjSubExptAsstList extends QueryOrganizerResource {
	XnatProjectdata proj=null;
	XnatSubjectdata sub=null;
	XnatExperimentdata assessed=null;
	
	public ProjSubExptAsstList(Context context, Request request, Response response) {
		super(context, request, response);

		final UserI user = getUser();

		final String pID= (String)getParameter(request,"PROJECT_ID");
		if(pID!=null){
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

			String subID= (String)getParameter(request,"SUBJECT_ID");
			if(proj != null && subID!=null){
				sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID, user, false);

				if(sub==null){
					sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
							false);
					if (sub != null
							&& (proj != null && !sub.hasProject(proj.getId()))) {
						sub = null;
					}
				}

				if(sub!=null){
					String exptID = (String) getParameter(request,
							"ASSESSED_ID");
					assessed = XnatExperimentdata.getXnatExperimentdatasById(
							exptID, user, false);
					if (assessed != null
							&& (proj != null && !assessed.hasProject(proj
									.getId()))) {
						assessed = null;
					}

					if(assessed==null){
						assessed = XnatExperimentdata
						.GetExptByProjectIdentifier(proj.getId(),
								exptID, user, false);
					}

					if(assessed!=null){
						this.getVariants().add(
								new Variant(MediaType.APPLICATION_JSON));
						this.getVariants()
						.add(new Variant(MediaType.TEXT_HTML));
						this.getVariants().add(new Variant(MediaType.TEXT_XML));
					}else{
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
								"Unable to find experiment.");
					}
				}else{
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"Unable to find subject.");
				}
			}else{
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
				"Unable to find project.");
			}
		}else{
			String exptID = (String) getParameter(request,"ASSESSED_ID");
			assessed = XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
			if(assessed!=null){
				this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
				this.getVariants().add(new Variant(MediaType.TEXT_HTML));
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			}else{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.DERIVED_DATA,true));
	}


	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		final UserI user = getUser();
		try {
			XFTItem item = loadItem(null,true);
			
				if(item==null){
					String xsiType=this.getQueryVariable("xsiType");
					if(xsiType!=null){
						item=XFTItem.NewItem(xsiType, user);
					}
				}
				
				if(item==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
					return;
				}
				
				if(item.instanceOf("xnat:imageAssessorData")){
					XnatImageassessordata assessor = (XnatImageassessordata)BaseElement.GetGeneratedItem(item);
					
					//MATCH PROJECT
					if(this.proj==null && assessor.getProject()!=null){
						proj = XnatProjectdata.getXnatProjectdatasById(assessor.getProject(), user, false);
					}
					
					if(this.proj!=null){
						if(assessor.getProject()==null || assessor.getProject().equals("")){
							assessor.setProject(this.proj.getId());
						}else if(assessor.getProject().equals(this.proj.getId())){
						}else{
							boolean matched=false;
							for(XnatExperimentdataShareI pp : assessor.getSharing_share()){
								if(pp.getProject().equals(this.proj.getId())){
									matched=true;
									break;
								}
							}
							
							if(!matched){
								XnatExperimentdataShare pp= new XnatExperimentdataShare(user);
								pp.setProject(this.proj.getId());
								assessor.setSharing_share(pp);
							}
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Submitted subject record must include the project attribute.");
						return;
					}
					
					//MATCH SESSION
					if(this.assessed!=null){
						assessor.setImagesessionId(this.assessed.getId());
					}else{
						if(assessor.getImagesessionId()!=null && !assessor.getImagesessionId().equals("")){
							this.assessed=XnatExperimentdata.getXnatExperimentdatasById(assessor.getImagesessionId(), user, false);
							
							if(this.assessed==null && assessor.getProject()!=null && assessor.getLabel()!=null){
							this.assessed=XnatExperimentdata.GetExptByProjectIdentifier(assessor.getProject(), assessor.getImagesessionId(),user, false);
							}
							
							if(this.assessed==null){
								for(XnatExperimentdataShareI pp : assessor.getSharing_share()){
								this.assessed=XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), assessor.getImagesessionId(),user, false);
									if(this.assessed!=null){
										break;
									}
								}
							}
						}
					}

					//FIND PRE-EXISTING
					XnatImageassessordata existing=null;
					if(assessor.getId()!=null){
						existing=(XnatImageassessordata)XnatExperimentdata.getXnatExperimentdatasById(assessor.getId(), user, completeDocument);
					}
					
					if(existing==null && assessor.getProject()!=null && assessor.getLabel()!=null){
					existing=(XnatImageassessordata)XnatExperimentdata.GetExptByProjectIdentifier(assessor.getProject(), assessor.getLabel(),user, completeDocument);
					}
					
					if(existing==null){
						for(XnatExperimentdataShareI pp : assessor.getSharing_share()){
						existing=(XnatImageassessordata)XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), pp.getLabel(),user, completeDocument);
							if(existing!=null){
								break;
							}
						}
					}
					
					if(existing==null){
						if(!Permissions.canCreate(user,assessor)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for subjects in this project.");
						return;
						}
						//IS NEW
						if(assessor.getId()==null || assessor.getId().equals("")){
						assessor.setId(XnatExperimentdata.CreateNewID());
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified experiment already exists.");
					return;
						//MATCHED
					}
					
					boolean allowDataDeletion=false;
					if(this.getQueryVariable("allowDataDeletion")!=null && this.getQueryVariable("allowDataDeletion").equals("true")){
						allowDataDeletion=true;
					}
					
				if(StringUtils.isNotBlank(assessor.getLabel()) && !XftStringUtils.isValidId(assessor.getId())){
					this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED,"Invalid character in experiment label.");
					return;
				}
				
					
				
				final ValidationResults vr = assessor.validate();
	            
	            if (vr != null && !vr.isValid())
	            {
	            	this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,vr.toFullString());
					return;
	            }
	            
	            create(assessor, false, allowDataDeletion, newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.getAddModifyAction(assessor.getXSIType(), (existing==null))));

	            postSaveManageStatus(assessor);
				
				this.returnSuccessfulCreateFromList(assessor.getId());
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only xnat:Subject documents can be PUT to this address.");
				}
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
			return;
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			logger.error("",e);
		} catch (Exception e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.error("",e);
			}
		}

	
	@Override
	public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
		ArrayList<String> al=new ArrayList<String>();
		
		al.add("ID");
		al.add("project");
		al.add("label");
		al.add("date");
		al.add("xsiType");
		al.add("insert_date");

		if(e.instanceOf("xnat:imageAssessorData")){
			al.add("session_ID");
			al.add("session_label");
	}

		return al;
	}

	public String getDefaultElementName(){
		return "xnat:imageAssessorData";
	}

	@Override
	public Representation represent(Variant variant) {
		final UserI user = getUser();
		XFTTable table = null;
		if(assessed!=null){
			Representation rep=super.represent(variant);
			if(rep!=null)return rep;
			
			try {
				String rootElementName=this.getRootElementName();
				QueryOrganizer qo = new QueryOrganizer(rootElementName,user,ViewManager.ALL);
				
				this.populateQuery(qo);
				
				CriteriaCollection where=new CriteriaCollection("AND");
					
					CriteriaCollection cc= new CriteriaCollection("OR");
					cc.addClause("xnat:imageAssessorData/imagesession_id", assessed.getId());
					where.addClause(cc);
					

                if (Groups.isMember(user, "ALL_DATA_ADMIN")) {
					CriteriaCollection projects=new CriteriaCollection("OR");
					List<Object> ps=Permissions.getAllowedValues(user,"xnat:subjectData", "xnat:subjectData/project", org.nrg.xdat.security.SecurityManager.READ);
					for(Object p:ps){					
						projects.addClause(rootElementName+"/project", p);
						projects.addClause(rootElementName+"/sharing/share/project", p);
					}
					where.addClause(projects);
				}
				
				
				qo.setWhere(where);
				
				String query=qo.buildQuery();
				
				table=XFTTable.Execute(query, user.getDBName(), userName);
				
				if(table.size()>0){
					table=formatHeaders(table,qo,rootElementName+"/ID","/data/experiments/");
					
					final Integer labelI=table.getColumnIndex("label");
					final Integer idI=table.getColumnIndex(rootElementName+"/ID");
					if(labelI!=null && idI!=null){
						final XFTTable t= XFTTable.Execute("SELECT sharing_share_xnat_experimentda_id as id,label FROM xnat_experimentData_share WHERE project='"+ proj.getId() + "'", user.getDBName(), user.getUsername());
						final Hashtable lbls=t.toHashtable("id", "label");
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
				}
			} catch (Exception e) {
				logger.error("", e);
			}

			Hashtable<String, Object> params = new Hashtable<>();
			if (table != null) {
				params.put("totalRecords", table.size());
			}
			return this.representTable(table, overrideVariant(variant), params);
		}
		
		Hashtable<String,Object> params= new Hashtable<>();
		params.put("title", "Project Subject Experiment Assessors");

		return representTable(null, overrideVariant(variant), params);
	}
}
