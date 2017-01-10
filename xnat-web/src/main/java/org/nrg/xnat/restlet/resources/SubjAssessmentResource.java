/*
 * web: org.nrg.xnat.restlet.resources.SubjAssessmentResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.transaction.TransactionException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.archive.Rename;
import org.nrg.xnat.archive.Rename.DuplicateLabelException;
import org.nrg.xnat.archive.Rename.FolderConflictException;
import org.nrg.xnat.archive.Rename.LabelConflictException;
import org.nrg.xnat.archive.Rename.ProcessingInProgress;
import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.actions.FixScanTypes;
import org.nrg.xnat.restlet.actions.PullSessionDataFromHeaders;
import org.nrg.xnat.restlet.actions.TriggerPipelines;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.xml.sax.SAXException;

import java.net.URISyntaxException;
import java.util.*;

public class SubjAssessmentResource extends SubjAssessmentAbst {
	XnatProjectdata proj=null;
	XnatSubjectdata subject=null;
	XnatSubjectassessordata expt = null;
	String exptID=null;
	XnatSubjectassessordata existing;
	String subID= null;
	
	public SubjAssessmentResource(Context context, Request request, Response response) {
		super(context, request, response);

		final UserI  user = getUser();
		final String pID  = (String) getParameter(request, "PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}
		
		if(proj==null){
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}

			subID= (String)getParameter(request,"SUBJECT_ID");
			if(subID!=null){
			subject = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
					.getId(), subID, user, false);
				
				if(subject==null){
				subject = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
						false);
				if (subject != null
						&& (proj != null && !subject.hasProject(proj.getId()))) {
					subject = null;
				}
				}
			}
			
			exptID= (String)getParameter(request,"EXPT_ID");
			if(exptID!=null){
			if (proj != null) {
				if (existing == null) {
					existing = (XnatSubjectassessordata) XnatExperimentdata
							.GetExptByProjectIdentifier(proj.getId(), exptID,
									user, false);
			}
			}

			if (existing == null) {
				existing = (XnatSubjectassessordata) XnatExperimentdata
						.getXnatExperimentdatasById(exptID, user, false);
				if (existing != null
						&& (proj != null && !existing.hasProject(proj.getId()))) {
					existing = null;
				}
			}

			this.getVariants().add(new Variant(MediaType.TEXT_HTML));
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
		}else{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.EXPERIMENT_DATA,false));
	}

	@Override
	public boolean allowPut() {
		return true;
	}
	
	private XnatSubjectdata getExistingSubject(XnatProjectdata proj, String subjectId){
		final UserI user = getUser();
		// First check if the subject is associated with the project,
		// if that fails check the global pool.
		XnatSubjectdata s = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjectId, user, false);
		if(s==null){
			s = XnatSubjectdata.getXnatSubjectdatasById(subID, user,false);
		}
		return s;
	}
	
	private XnatSubjectassessordata getExistingExperiment(XnatSubjectassessordata currExp){
		final UserI user = getUser();
		XnatSubjectassessordata retExp = null;
		if(currExp.getId()!=null){
			retExp = (XnatSubjectassessordata)XnatExperimentdata.getXnatExperimentdatasById(currExp.getId(), null, completeDocument);
		}

		if(retExp==null && currExp.getProject()!=null && currExp.getLabel()!=null){
			retExp = (XnatSubjectassessordata)XnatExperimentdata.GetExptByProjectIdentifier(currExp.getProject(), currExp.getLabel(),user, completeDocument);
		}

		if(retExp==null){
			for(XnatExperimentdataShareI pp : currExp.getSharing_share()){
				retExp = (XnatSubjectassessordata)XnatExperimentdata.GetExptByProjectIdentifier(pp.getProject(), pp.getLabel(),user, completeDocument);
				if(retExp != null){
					break;
				}
			}
		}
		return retExp;
	}

	@Override
	public void handlePut() {
        XFTItem item = null;

		final UserI user = getUser();
		try {
			final boolean allowDataDeletion = isQueryVariableTrue("allowDataDeletion");

			XFTItem template=null;
			if (existing!=null && !allowDataDeletion){
				template=existing.getItem().getCurrentDBVersion();
			}

			item=this.loadItem(null,true,template);
			
				if(item==null){
					String xsiType=this.getQueryVariable("xsiType");
					if(xsiType!=null){
						item=XFTItem.NewItem(xsiType, user);
					}
				}
				
				if(item==null){
				if(proj!=null){
					XnatSubjectassessordata om =(XnatSubjectassessordata)XnatSubjectassessordata.GetExptByProjectIdentifier(proj.getId(), this.exptID,user, false);
					if(om!=null){
						item=om.getItem();
					}
				}

				if(item==null){
					XnatSubjectassessordata om =(XnatSubjectassessordata)XnatSubjectassessordata.getXnatExperimentdatasById(this.exptID, null, false);
					if(om!=null){
						item=om.getItem();
					}
				}
			}

			if(item==null){
				this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
					return;
				}
				
				if(item.instanceOf("xnat:subjectAssessorData")){
					expt = (XnatSubjectassessordata)BaseElement.GetGeneratedItem(item);
					
				if(filepath!=null && !filepath.equals("")){
					if(filepath.startsWith("projects/")){
						String newProjectS= filepath.substring(9);
						XnatProjectdata newProject=XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
						String newLabel = this.getQueryVariable("label");
						if(newProject!=null){
							if(expt.getProject().equals(newProject.getId())){
								this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Already assigned to project:"+ newProject.getId());
								return;
							}
							
							if(!Permissions.canRead(user,expt)){
								this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Specified user account has insufficient privileges for experiments in this project.");
								return;
							}

							int index=0;
							XnatExperimentdataShare matched=null;
							for(XnatExperimentdataShareI pp : expt.getSharing_share()){
								if(pp.getProject().equals(newProject.getId())){
									matched=(XnatExperimentdataShare)pp;
									if(newLabel!=null && !pp.getLabel().equals(newLabel)){										
										((XnatExperimentdataShare)pp).setLabel(newLabel);
										BaseXnatExperimentdata.SaveSharedProject((XnatExperimentdataShare)pp, expt, user,newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.RENAME_IN_SHARED_PROJECT));
									}
									break;
								}
								index++;
							}

							if(this.getQueryVariable("primary")!=null && this.getQueryVariable("primary").equals("true")){
								if(newLabel==null || newLabel.equals(""))newLabel=expt.getLabel();
								if(newLabel==null || newLabel.equals(""))newLabel=expt.getId();

								
								if(!Permissions.canDelete(user,expt)){
									this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient privileges for experiments in this project.");
									return;
								}
								
								XnatExperimentdata match=XnatExperimentdata.GetExptByProjectIdentifier(newProject.getId(), newLabel,user, false);
								if(match!=null){
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified label is already in use.");
									return;
								}

                                List<String> assessorList = null;
                                if(this.getQueryVariable("moveAssessors")!=null) {
                                    String moveAssessors = this.getQueryVariable("moveAssessors");
                                    assessorList = Arrays.asList(moveAssessors.split(","));
                                }

								EventMetaI c=BaseXnatExperimentdata.ChangePrimaryProject(user, expt, newProject, newLabel, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.MODIFY_PROJECT), assessorList);

								if(matched!=null){
									SaveItemHelper.authorizedRemoveChild(expt.getItem(), "xnat:experimentData/sharing/share", matched.getItem(), user,c);
									expt.removeSharing_share(index);
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
										if(Permissions.canCreate(user,expt.getXSIType()+"/project", newProject.getId())){
											XnatExperimentdataShare pp= new XnatExperimentdataShare((UserI)user);
											pp.setProject(newProject.getId());
											if(newLabel!=null)pp.setLabel(newLabel);
											pp.setProperty("sharing_share_xnat_experimentda_id", expt.getId());
											BaseXnatExperimentdata.SaveSharedProject((XnatExperimentdataShare)pp, expt, user,newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.CONFIGURED_PROJECT_SHARING));
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
							return;
						}
					}else{
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
							return;
						}
				}else{
					if(expt.getLabel()==null){
						expt.setLabel(this.exptID);
					}
					//MATCH PROJECT
					if(this.proj==null && expt.getProject()!=null){
						proj = XnatProjectdata.getXnatProjectdatasById(expt.getProject(), user, false);
					}
					
					if(this.proj!=null){
						if(expt.getProject()==null || expt.getProject().equals("")){
							expt.setProject(this.proj.getId());
						}else if(expt.getProject().equals(this.proj.getId())){
						}else{
							boolean matched=false;
							for(XnatExperimentdataShareI pp : expt.getSharing_share()){
								if(pp.getProject().equals(this.proj.getId())){
									matched=true;
									break;
								}
							}
							
							if(!matched){
								XnatExperimentdataShare pp= new XnatExperimentdataShare((UserI)user);
								pp.setProject(this.proj.getId());
								expt.setSharing_share(pp);
							}
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Submitted experiment record must include the project attribute.");
						return;
					}
					
					// Find the pre-existing experiment
					if(existing==null){ existing = getExistingExperiment(expt); }

					//MATCH SUBJECT
					if(this.subject!=null){
							expt.setSubjectId(this.subject.getId());
					}else{
						if(StringUtils.isBlank(expt.getSubjectId()) && StringUtils.isNotEmpty(subID)){
							expt.setSubjectId(subID);
						}

						if(expt.getSubjectId()!=null && !expt.getSubjectId().equals("")){
							this.subject=XnatSubjectdata.getXnatSubjectdatasById(expt.getSubjectId(), user, false);
							
							if(this.subject==null && expt.getProject()!=null && expt.getLabel()!=null){
								this.subject=XnatSubjectdata.GetSubjectByProjectIdentifier(expt.getProject(), expt.getSubjectId(),user, false);
							}
							
							if(this.subject==null){
								for(XnatExperimentdataShareI pp : expt.getSharing_share()){
									this.subject=XnatSubjectdata.GetSubjectByProjectIdentifier(pp.getProject(), expt.getSubjectId(),user, false);
									if(this.subject!=null){
										break;
									}
								}
							}

							if(subject==null && existing!=null){
								this.subject=existing.getSubjectData();
								expt.setSubjectId(subject.getId());
							}
					
							if(this.subject==null){
								
								this.subject = new XnatSubjectdata((UserI)user);
								this.subject.setProject(this.proj.getId());
								this.subject.setLabel(expt.getSubjectId());
								this.subject.setId(XnatSubjectdata.CreateNewID());
								if(!Permissions.canCreate(user,this.subject)){
									this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for subjects in this project.");
									return;
								}
								BaseXnatSubjectdata.save(this.subject, false, true,user,newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.AUTO_CREATE_SUBJECT));
								expt.setSubjectId(this.subject.getId());
							}
						}
					}

					
					if(existing==null){
						if(!Permissions.canCreate(user,expt)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for experiments in this project.");
							return;
						}
						//IS NEW
						if(expt.getId()==null || expt.getId().equals("")){
							expt.setId(XnatExperimentdata.CreateNewID());
						}
						
					}else{
						if(expt.getId()==null || expt.getId().equals("")){
							expt.setId(existing.getId());
						}
						
						//MATCHED
						if(!existing.getProject().equals(expt.getProject())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Project must be modified through separate URI.");
							return;
						}

						if(!Permissions.canEdit(user,expt)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient edit privileges for experiments in this project.");
							return;
						}
						
						if(this.getQueryVariable("subject_ID")!=null && !this.getQueryVariable("subject_ID").equals("") ){
							if (!expt.getSubjectId().equals(this.getQueryVariable("subject_ID"))) {
								XnatSubjectdata s = this.getExistingSubject(proj,
																			this.getQueryVariable("subject_ID"));
								if (s != null) {
									// \"subject_ID\" can be overloaded on both the subject's label
									// and XNAT unique subject identifier
									if (!expt.getSubjectId().equals(s.getId())) {
										// only accept subjects that are associated with this project
										if (s.hasProject(proj.getId())){
											expt.setSubjectId(s.getId());
										}
									}
								}
								else {
									try {
										this.subject = new XnatSubjectdata((UserI)user);
										this.subject.setProject(this.proj.getId());
										this.subject.setLabel(this.getQueryVariable("subject_ID"));
										this.subject.setId(XnatSubjectdata.CreateNewID());
										if(!Permissions.canCreate(user,this.subject)){
											this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for subjects in this project.");
											return;
										}
										BaseXnatSubjectdata.save(this.subject, false, true,user,newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.AUTO_CREATE_SUBJECT));
										expt.setSubjectId(this.subject.getId());
									} 
									catch (ResourceException e) {
										this.getResponse().setStatus(e.getStatus(), "Specified user account has insufficient create privileges for subjects in this project.");
									} 	
								}
							}						
						}
						
						if(this.getQueryVariable("label")!=null && !this.getQueryVariable("label").equals("") ){
							if(!expt.getLabel().equals(existing.getLabel())){
								expt.setLabel(existing.getLabel());
							}
							String label=this.getQueryVariable("label");

							if(!label.equals(existing.getLabel())){
								XnatExperimentdata match=XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), label,user, false);
								if(match!=null){
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified label is already in use.");
									return;
								}

								Rename renamer = new Rename(proj,existing,label,user,getReason(),getEventType());
								try {
									renamer.call();
								} catch (ProcessingInProgress e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified session is being processed (" + e.getPipeline_name() +").");
									return;
								} catch (DuplicateLabelException e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified label is already in use.");
									return;
								} catch (LabelConflictException e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified label is already in use.");
									return;
								} catch (FolderConflictException e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"File system destination contains pre-existing files");
									return;
								} catch (InvalidArchiveStructure e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Non-standard archive structure in existing experiment directory.");
									return;
								} catch (URISyntaxException e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Non-standard archive structure in existing experiment directory.");
									return;
								} catch (Exception e) {
									logger.error("", e);
									this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
									return;
								}
							}
							return;
						}
					}

					if(this.subject==null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Submitted experiment record must include the subject.");
						return;
					}
					
					PersistentWorkflowI wrk= WorkflowUtils.buildOpenWorkflow(user, expt.getItem(),newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(expt.getXSIType(), (existing==null))));
					EventMetaI c=wrk.buildEvent();
					
					if(this.isQueryVariableTrue(XNATRestConstants.FIX_SCAN_TYPES) || this.containsAction(XNATRestConstants.FIX_SCAN_TYPES)){
						if(expt instanceof XnatImagesessiondata){
							FixScanTypes fst=new FixScanTypes(expt,user,proj,false,c);
							fst.call();
						}
					}


					if(StringUtils.isNotBlank(expt.getLabel()) && !XftStringUtils.isValidId(expt.getId())){
						this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED,"Invalid character in experiment label.");
						return;
					}
					
					final ValidationResults vr = expt.validate();
		            
		            if (vr != null && !vr.isValid())
		            {
		            	this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,vr.toFullString());
						return;
		            }
					
					try {
						// Preserve the previous version of the experiment before we save it. 
						XnatSubjectassessordata previous  = getExistingExperiment(expt);

						

						//check for unexpected modifications of ID, Project and label
						if(existing !=null && !StringUtils.equals(existing.getId(),expt.getId())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"ID cannot be modified");
							return;
						}
						
						if(existing !=null && !StringUtils.equals(existing.getProject(),expt.getProject())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Project must be modified through separate URI.");
							return;
						}
						
						//MATCHED
						if(existing !=null && !StringUtils.equals(existing.getLabel(),expt.getLabel())){
							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Label must be modified through separate URI.");
							return;
						}
			            
						
						if(SaveItemHelper.authorizedSave(expt,user,false,allowDataDeletion,c)){
							WorkflowUtils.complete(wrk, c);
							Users.clearCache(user);
							MaterializedView.deleteByUser(user);

							if(this.proj.getArcSpecification().getQuarantineCode()!=null && this.proj.getArcSpecification().getQuarantineCode().equals(1)){
								expt.quarantine(user);
							}
						
							if (previous != null && expt != null && expt instanceof XnatImagesessiondata && expt.getSubjectId() != null && !expt.getSubjectId().equals(previous.getSubjectId())) {
								try {
									// re-apply this project's edit script
									expt.applyAnonymizationScript(new ProjectAnonymizer((XnatImagesessiondata) expt, expt.getProject(), expt.getArchiveRootPath()));
								}
								catch (TransactionException e) {
									this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
								}
							}
						}
					} catch (Exception e1) {
						WorkflowUtils.fail(wrk, c);
						throw e1;
					}

					postSaveManageStatus(expt);

					if(Permissions.canEdit(user,expt.getItem())){
						if((this.isQueryVariableTrue(XNATRestConstants.PULL_DATA_FROM_HEADERS) || this.containsAction(XNATRestConstants.PULL_DATA_FROM_HEADERS) ) && expt instanceof XnatImagesessiondata){
							try {
								wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.DICOM_PULL));
								c=wrk.buildEvent();
								try {
									PullSessionDataFromHeaders pull=new PullSessionDataFromHeaders((XnatImagesessiondata)expt,user,this.allowDataDeletion(),this.isQueryVariableTrue("overwrite"),false,c);
									pull.call();
									WorkflowUtils.complete(wrk, c);
								} catch (Exception e) {
									WorkflowUtils.fail(wrk, c);
									throw e;
								}
								
							} catch (SAXException e){
								logger.error("",e);
								this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
							} catch (ValidationException e){
								logger.error("",e);
								this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
							} catch (Exception e) {
								logger.error("",e);
								this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
								return;
							}
						}
						
						if(this.isQueryVariableTrue(XNATRestConstants.TRIGGER_PIPELINES) || this.containsAction(XNATRestConstants.TRIGGER_PIPELINES)){
							TriggerPipelines tp=new TriggerPipelines(expt,this.isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL),user);
							tp.call();
						}
					}
				}

				this.returnString(expt.getId(),(existing==null)?Status.SUCCESS_CREATED:Status.SUCCESS_OK);
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only xnat:Subject documents can be PUT to this address.");
				}
		} catch (InvalidValueException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			logger.error("",e);
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
			return;
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
			if(expt==null&& exptID!=null){
				expt=(XnatSubjectassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
				
				if(expt==null && this.proj!=null){
				expt=(XnatSubjectassessordata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), exptID,user, false);
				}
			}
			
			if(expt==null){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified experiment.");
				return;
			}
			
		XnatProjectdata newProject=null;
			
		if(filepath!=null && !filepath.equals("")){
			if(filepath.startsWith("projects/")){
				String newProjectS= filepath.substring(9);
				newProject=XnatProjectdata.getXnatProjectdatasById(newProjectS, user, false);
				if(newProject==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to identify project: " + newProjectS);
					return;
				}
			}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return;
	                }
		}else if(!expt.getProject().equals(proj.getId())){
			newProject=proj;
	            }
	            
		PersistentWorkflowI wrk;
		try {
			wrk = WorkflowUtils.buildOpenWorkflow(user, expt.getItem(),newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.getDeleteAction(expt.getXSIType())));
			EventMetaI c=wrk.buildEvent();
			
			try {
				String msg=expt.delete((newProject!=null)?newProject:proj, user, this.isQueryVariableTrue("removeFiles"),c);
				if(msg!=null){
					WorkflowUtils.fail(wrk, c);
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,msg);
					return;
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
				return;
			}
		} catch (EventRequirementAbsent e1) {
			logger.error("",e1);
			this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,e1.getMessage());
			return;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Representation represent(Variant variant) {
		MediaType mt = overrideVariant(variant);

		final UserI user = getUser();
		if(expt==null&& exptID!=null){
			expt=(XnatSubjectassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, user, false);
			
			if(expt==null && this.proj!=null){
				expt=(XnatSubjectassessordata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), exptID,user, false);
			}
		}
		
		if(expt!=null){
			if(filepath!=null && !filepath.equals("") && filepath.equals("status")){

				return returnStatus(expt,mt);
			}else if(filepath!=null && !filepath.equals("") && filepath.equals("history")){
				try {
					return buildChangesets(expt.getItem(),expt.getStringProperty("ID"), mt);
				} catch (Exception e) {
					logger.error("",e);
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
					return null;
				}
			}else if(filepath!=null && !filepath.equals("") && filepath.startsWith("projects")){
				XFTTable t = new XFTTable();
				ArrayList al = new ArrayList();
				al.add("label");
				al.add("ID");
				al.add("Secondary_ID");
				al.add("Name");
				t.initTable(al);

				Object[] row=new Object[4];
				row[0]=expt.getLabel();
				XnatProjectdata primary = expt.getPrimaryProject(false);
				row[1]=primary.getId();
				row[2]=primary.getSecondaryId();
				row[3]=primary.getName();
				t.rows().add(row);

				for(Map.Entry<XnatProjectdataI, String> entry: expt.getProjectDatas().entrySet()){
					row=new Object[4];
					row[0]=entry.getValue();
					row[1]=entry.getKey().getId();
					row[2]=entry.getKey().getSecondaryId();
					row[3]=entry.getKey().getName();
					t.rows().add(row);
				}

				return representTable(t, mt, new Hashtable<String,Object>());
			}else{
				return this.representItem(expt.getItem(),mt);
			}
		} else {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"Unable to find the specified experiment.");
			return null;
		}

	}

}
