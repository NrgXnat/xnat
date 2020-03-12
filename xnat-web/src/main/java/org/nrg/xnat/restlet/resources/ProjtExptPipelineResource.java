/*
 * web: org.nrg.xnat.restlet.resources.ProjtExptPipelineResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import java.util.HashMap;
import java.util.Map;

import org.nrg.pipeline.PipelineLaunchHandler;
import org.nrg.xdat.om.ArcPipelinedata;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.ValidationException;
import org.nrg.xnat.restlet.actions.FixScanTypes;
import org.nrg.xnat.restlet.actions.PullSessionDataFromHeaders;
import org.nrg.xnat.restlet.actions.TriggerPipelines;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.xml.sax.SAXException;

public class ProjtExptPipelineResource extends SecureResource {
	XnatProjectdata proj=null;
	XnatExperimentdata expt=null;
    String step = null;

	public ProjtExptPipelineResource(Context context, Request request, Response response) {
		super(context, request, response);

		String pID = (String) getParameter(request,"PROJECT_ID");
		if (pID != null) {
			final UserI user = getUser();

			proj = XnatProjectdata.getXnatProjectdatasById(pID, user, false);
			step = (String) getParameter(request,"STEP_ID");
			if (step != null) {
				String exptID = (String) getParameter(request,"EXPT_ID");
				if (exptID != null) {
					expt = XnatExperimentdata.getXnatExperimentdatasById(
							exptID, user, false);

					if (expt == null) {
						if (proj == null) {
							response.setStatus(Status.CLIENT_ERROR_GONE);
							return;
						}
						expt = XnatExperimentdata.GetExptByProjectIdentifier(
								proj.getId(), exptID, user, false);
					}
				}
				this.getVariants().add(new Variant(MediaType.TEXT_XML));

			} else {
				response.setStatus(Status.CLIENT_ERROR_GONE);
			}
		} else {
			response.setStatus(Status.CLIENT_ERROR_GONE);
		}
	}


    public Representation represent(Variant variant) {
		if(proj!=null && step!=null){
			ArcPipelinedata arcPipeline = null;
			ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(proj.getId());
			//arcProject.setItem(arcProject.getCurrentDBVersion());
			try {
				if (expt == null) { // Look for Project level pipeline
					arcPipeline = (ArcPipelinedata)arcProject.getPipeline(step);
				}else { //Look for experiment level pipeline
					arcPipeline = (ArcPipelinedata)arcProject.getPipelineForDescendant(expt.getXSIType(), step);
				}
				MediaType mt = overrideVariant(variant);
				if (mt.equals(MediaType.TEXT_XML)) {
					return representItem(arcPipeline.getItem(), mt, null,false, true);
				}else {
					return representItem(arcPipeline.getItem(), mt);
				}
			}catch(Exception e) {
				e.printStackTrace();
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		return null;
	}

	@Override
	public boolean allowPost() {
		return true;
	}


	@Override
	public void handlePost() {
		if(proj!=null && step!=null && expt != null){
			try {
				final UserI user = getUser();
				if(step.equals(XNATRestConstants.TRIGGER_PIPELINES)){
					if(Permissions.canEdit(user, expt)){

						PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TRIGGER_PIPELINES));
						EventMetaI c=wrk.buildEvent();

						try {
							FixScanTypes fst=new FixScanTypes(expt, user, proj, true, c);
							fst.call();

							TriggerPipelines tp = new TriggerPipelines(expt, this.isQueryVariableTrue(XNATRestConstants.SUPRESS_EMAIL), user);
							tp.call();
							PersistentWorkflowUtils.complete(wrk,c);
						} catch (Exception e) {
							WorkflowUtils.fail(wrk, c);
							throw e;
						}
					}
				}else if(step.equals(XNATRestConstants.PULL_DATA_FROM_HEADERS) && expt instanceof XnatImagesessiondata){
					if(Permissions.canEdit(user, expt)){
						try {
							PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.DICOM_PULL));
							EventMetaI c=wrk.buildEvent();
							try {
								PullSessionDataFromHeaders pull=new PullSessionDataFromHeaders((XnatImagesessiondata)expt, user, this.isQueryVariableTrue("allowDataDeletion"), this.isQueryVariableTrue("overwrite"), false, c);
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
					}else{
						getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					}
				}else if(step.equals(XNATRestConstants.FIX_SCAN_TYPES) && expt instanceof XnatImagesessiondata){
					if(Permissions.canEdit(user, expt)){

						PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, expt.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TRIGGER_PIPELINES));
						EventMetaI c=wrk.buildEvent();
						PersistentWorkflowUtils.save(wrk,c);

						try {
							FixScanTypes fst=new FixScanTypes(expt, user, proj, true, c);
							fst.call();
							WorkflowUtils.complete(wrk, c);
						} catch (Exception e) {
							WorkflowUtils.fail(wrk, c);
							throw e;
						}
					}else{
						getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
					}
				}else{
					Map<String,String> bodyParams = getBodyVariableMap();
					Map<String,String> queryParams = getQueryVariableMap();
					Map<String,String> xmlDocumentParams = new HashMap<String,String>();
	                String XMLbody = getRequest().getEntity().getText();
	                
					PipelineLaunchHandler launchHandler = new PipelineLaunchHandler(proj, expt, step);
					boolean launchSuccess = launchHandler.handleLaunch(bodyParams, queryParams, xmlDocumentParams, XMLbody, user);
				}
			} catch (Exception e) {
				logger.error(e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			}
		}else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

 }
