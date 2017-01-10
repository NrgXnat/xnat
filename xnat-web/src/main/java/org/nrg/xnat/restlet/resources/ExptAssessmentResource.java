/*
 * web: org.nrg.xnat.restlet.resources.ExptAssessmentResource
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
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatExperimentdataShare;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.*;

public class ExptAssessmentResource extends ItemResource {
	XnatProjectdata proj=null;
	XnatExperimentdata assesed = null;
	XnatImageassessordata assessor=null;
	XnatImageassessordata existing=null;

	String exptID=null;

	public ExptAssessmentResource(Context context, Request request, Response response) {
		super(context, request, response);

		final UserI user = getUser();

		String pID= (String)getParameter(request,"PROJECT_ID");
		if(pID!=null){
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
			
			if(proj==null){
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return;
			}
		}

		String assessedID= (String)getParameter(request,"ASSESSED_ID");
		if(assessedID!=null){
			if(assesed == null){
				assesed = XnatExperimentdata.getXnatExperimentdatasById(assessedID, user, false);
				if (assesed != null && (proj != null && !assesed.hasProject(proj.getId()))) {
					assesed = null;
				}

				if(assesed==null && this.proj!=null){
					assesed = XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), assessedID, user, false);
				}
			}

			exptID= (String)getParameter(request,"EXPT_ID");
			if(exptID!=null){
				existing = (XnatImageassessordata) XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
				if (existing != null && (proj != null && !existing.hasProject(proj.getId()))) {
					existing = null;
				}

				if (existing == null && proj != null) {
					existing = (XnatImageassessordata) XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID,user, false);
				}

				this.getVariants().add(new Variant(MediaType.TEXT_HTML));
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find assessed experiment, no ID submitted or found.");
		}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.DERIVED_DATA,false));
	}


	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void handlePut() {
		final UserI user = getUser();
		try {
			XFTItem template=null;
			if (existing!=null && !this.isQueryVariableTrue("allowDataDeletion")){
				template=existing.getItem().getCurrentDBVersion();
			}

			XFTItem item=this.loadItem(null,true,template);

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
				assessor = (XnatImageassessordata)BaseElement.GetGeneratedItem(item);

				if(filepath!=null && !filepath.equals("")){
					if(filepath.startsWith("projects/")){
						if(!Permissions.canRead(user,assessor)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient privileges for experiments in this project.");
							return;
						}

						String newProjectS= filepath.substring(9);
						XnatProjectdata newProject=XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
						String newLabel = this.getQueryVariable("label");
						if(newProject!=null){
							XnatExperimentdataShare matched=null;
							int index=0;
							for(XnatExperimentdataShareI pp : assessor.getSharing_share()){
								if(pp.getProject().equals(newProject.getId())){
									matched=(XnatExperimentdataShare)pp;
									if(newLabel!=null && !pp.getLabel().equals(newLabel)){
										pp.setLabel(newLabel);
										BaseXnatExperimentdata.SaveSharedProject((XnatExperimentdataShare)pp, assessor, user,newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.RENAME_IN_SHARED_PROJECT));
									}
									break;
								}
								index++;
							}


							if(this.getQueryVariable("primary")!=null && this.getQueryVariable("primary").equals("true")){
								if(!Permissions.canDelete(user,assessor)){
									this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient privileges for experiments in this project.");
									return;
								}

                                List<String> assessorList = null;
                                if(this.getQueryVariable("moveAssessors")!=null) {
                                    String moveAssessors = this.getQueryVariable("moveAssessors");
                                    assessorList = Arrays.asList(moveAssessors.split(","));
                                }

								EventMetaI c=BaseXnatExperimentdata.ChangePrimaryProject(user, assessor, newProject, newLabel,newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.MODIFY_PROJECT), assessorList);

								if(matched!=null){
									SaveItemHelper.authorizedRemoveChild(assessor.getItem(), "xnat:experimentData/sharing/share", matched.getItem(), user,c);
									assessor.removeSharing_share(index);
								}
							}else{
								if(matched==null){
									if(newLabel!=null){
										XnatExperimentdata temp=XnatExperimentdata.GetExptByProjectIdentifier(newProject.getId(), newLabel, null, false);
										if(temp!=null){
											this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Label already in use:"+ newLabel);
											return;
										}
									}

									if(Permissions.canCreate(user, assessor.getXSIType()+"/project", newProject.getId())){
										XnatExperimentdataShare pp= new XnatExperimentdataShare(user);
										pp.setProject(newProject.getId());
										if(newLabel!=null)pp.setLabel(newLabel);
										pp.setProperty("sharing_share_xnat_experimentda_id", assessor.getId());

										BaseXnatExperimentdata.SaveSharedProject(pp, assessor, user, newEventInstance(EventUtils.CATEGORY.DATA, (getAction() != null) ? getAction() : "Shared into additional project"));
									}else{
										this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for experiments in the " + newProject.getId() + " project.");
										return;
									}
								}else{
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Already assigned to project:"+ newProject.getId());
									return;
								}
							}

							this.returnDefaultRepresentation();
						}else{
							this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify project: " + newProjectS);
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					}
				}else{
					if(assessor.getLabel()==null){
						assessor.setLabel(this.exptID);
					}

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
					if(this.assesed!=null){
						assessor.setImagesessionId(this.assesed.getId());
					}else{
						if(assessor.getImagesessionId()!=null && !assessor.getImagesessionId().equals("")){
							this.assesed=XnatExperimentdata.getXnatExperimentdatasById(assessor.getImagesessionId(), user, false);

							if(this.assesed==null && assessor.getProject()!=null && assessor.getLabel()!=null){
								this.assesed=XnatExperimentdata.GetExptByProjectIdentifier(assessor.getProject(), assessor.getImagesessionId(),user, false);
							}

							if(this.assesed==null){
								for(XnatExperimentdataShareI pp : assessor.getSharing_share()){
									this.assesed=XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), assessor.getImagesessionId(),user, false);
									if(this.assesed!=null){
										break;
									}
								}
							}
						}
					}

					//FIND PRE-EXISTING
					if(existing==null){
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
						if(!existing.getProject().equals(assessor.getProject())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Project must be modified through separate URI.");
							return;
						}

						if(!Permissions.canEdit(user,assessor)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient edit privileges for subjects in this project.");
							return;
						}
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

					//check for unexpected modifications of ID, Project and label
					if(existing !=null && !StringUtils.equals(existing.getId(),assessor.getId())){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"ID cannot be modified");
						return;
					}
					
					if(existing !=null && !StringUtils.equals(existing.getProject(),assessor.getProject())){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Project must be modified through separate URI.");
						return;
					}
					
					//MATCHED
					if(existing !=null && !StringUtils.equals(existing.getLabel(),assessor.getLabel())){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Label must be modified through separate URI.");
						return;
					}

					create(assessor, false, allowDataDeletion, newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.getAddModifyAction(assessor.getXSIType(), (existing==null))));

					postSaveManageStatus(assessor);
					
					this.returnString(assessor.getId(),(existing==null)?Status.SUCCESS_CREATED:Status.SUCCESS_OK);
				}
			}else{
				this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only xnat:Subject documents can be PUT to this address.");
			}
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
		} catch (Exception e) {
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.error("",e);
		}
	}


	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void handleDelete(){
		final UserI user = getUser();

		if(assessor==null&& exptID!=null){
			assessor=(XnatImageassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);

			if(assessor==null && this.proj!=null){
				assessor=(XnatImageassessordata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), exptID,user, false);
			}
		}

		if(assessor==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified experiment.");
			return;
		}

		//if we attempt to delete an assessment from a shared project, the assessment shouldn't be deleted (just unshared)
		//if its the primary project, it should b deleted
		//REST/projects/SHARED/.../experiments/ID should unshare
		//REST/projects/PRIMARY/.../experiments/ID should delete
		XnatProjectdata deleteFromProject=null;

		if(filepath!=null && !filepath.equals("")){
			if(filepath.startsWith("projects/")){
				//check if a specific project is referenced
				String newProjectS= filepath.substring(9);
				deleteFromProject=XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
				if(deleteFromProject==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify project: " + newProjectS);
					return;
				}
			}else{
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return;
			}
		}else if(proj==null){
			//if no project is referenced... assume its the default one (/REST/experiments/ID)
			deleteFromProject=assessor.getPrimaryProject(false);
		}else if(!assessor.getProject().equals(proj.getId())){
			deleteFromProject=proj;
		}

		
		PersistentWorkflowI wrk;
		try {
			wrk = WorkflowUtils.buildOpenWorkflow(user, assessor.getItem(),newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.getDeleteAction(assessor.getXSIType())));
			EventMetaI c=wrk.buildEvent();
			
			try {
				String msg=assessor.delete((deleteFromProject!=null)?deleteFromProject:proj, user, this.isQueryVariableTrue("removeFiles"),c);
				if(msg!=null){
					WorkflowUtils.fail(wrk, c);
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,msg);
				}else{
					WorkflowUtils.complete(wrk, c);
				}
			} catch (Exception e) {
				try {
					WorkflowUtils.fail(wrk, c);
				} catch (Exception e1) {
					logger.error("",e1);
				}
				logger.error("",e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			}
		} catch (EventRequirementAbsent e1) {
			logger.error("",e1);
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,e1.getMessage());
		}
	}

	@Override
	public Representation represent(Variant variant) {
		MediaType mt = overrideVariant(variant);
		final UserI user = getUser();

		if(assessor==null&& exptID!=null){
			assessor=(XnatImageassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);

			if(assessor==null && this.proj!=null){
				assessor=(XnatImageassessordata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), exptID,user, false);
			}
		}

		if(assessor!=null){
			String filepath = this.getRequest().getResourceRef().getRemainingPart();
			if(filepath!=null && filepath.contains("?")){
				filepath = filepath.substring(0,filepath.indexOf("?"));
		}

			if(filepath!=null && filepath.startsWith("/")){
				filepath=filepath.substring(1);
			}
			if(filepath!=null && filepath.equals("status")){
				return returnStatus(assessor,mt);
			}else if (filepath != null && filepath.startsWith("projects")) {
				XFTTable t = new XFTTable();
				ArrayList<String> al = new ArrayList<>();
				al.add("label");
				al.add("ID");
				al.add("Secondary_ID");
				al.add("Name");
				t.initTable(al);

				Object[] row = new Object[4];
				row[0] = assessor.getLabel();
				XnatProjectdata primary = assessor.getPrimaryProject(false);
				row[1] = primary.getId();
				row[2] = primary.getSecondaryId();
				row[3] = primary.getName();
				t.rows().add(row);

				for (Map.Entry<XnatProjectdataI, String> entry : assessor.getProjectDatas().entrySet()) {
					row = new Object[4];
					row[0] = entry.getValue();
					row[1] = entry.getKey().getId();
					row[2] = entry.getKey().getSecondaryId();
					row[3] = entry.getKey().getName();
					t.rows().add(row);
				}

				Hashtable<String, Object> params = new Hashtable<>();
				params.put("totalRecords", t.size());
				return representTable(t, mt, params);
			} else {
				return this.representItem(assessor.getItem(), mt);
			}
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified experiment.");
			return null;
		}
	}
}
