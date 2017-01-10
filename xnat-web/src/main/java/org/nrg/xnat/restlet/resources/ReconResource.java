/*
 * web: org.nrg.xnat.restlet.resources.ReconResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;

public class ReconResource extends ItemResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ScanResource.class);
	XnatProjectdata proj=null;
	XnatImagesessiondata session = null;
	XnatReconstructedimagedata recon=null;
	
	String exptID=null;

	public ReconResource(Context context, Request request, Response response) {
		super(context, request, response);

		String      pID  = (String) getParameter(request, "PROJECT_ID");
		final UserI user = getUser();
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}

		String assessedID = (String) getParameter(request, "ASSESSED_ID");
		if (assessedID != null) {
			if (session == null && assessedID != null) {
				session = (XnatImagesessiondata) XnatExperimentdata
						.getXnatExperimentdatasById(assessedID, user, false);
				if (session != null
					&& (proj != null && !session.hasProject(proj.getId()))) {
					session = null;
				}

				if (session == null && this.proj != null) {
					session = (XnatImagesessiondata) XnatExperimentdata
							.GetExptByProjectIdentifier(this.proj.getId(),
														assessedID, user, false);
				}
			}

			exptID = (String) getParameter(request, "RECON_ID");
			if (exptID != null) {
				this.getVariants().add(new Variant(MediaType.TEXT_HTML));
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			}

		} else {
			response.setStatus(Status.CLIENT_ERROR_GONE,
							   "Unable to find session '" + assessedID + "'");
		}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.RECON_DATA, false));
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void handlePut() {
		XFTItem item = null;			

		try {
			item=this.loadItem("xnat:reconstructedImageData",true);

			final UserI user = getUser();
			if(item == null){
				String xsiType=this.getQueryVariable("xsiType");
				if(xsiType!=null){
					item=XFTItem.NewItem(xsiType, user);
				}
			}

			if(item==null){
				this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
				return;
			}

			if(filepath!=null && !filepath.equals("")){
				this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return;
			}
			if(item.instanceOf("xnat:reconstructedImageData")){
				recon = (XnatReconstructedimagedata)BaseElement.GetGeneratedItem(item);

				//MATCH SESSION
				if(this.session!=null){
					recon.setImageSessionId(this.session.getId());
				}else{
					if(recon.getImageSessionId()!=null && !recon.getImageSessionId().equals("")){
						this.session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(recon.getImageSessionId(), user, false);

						if(this.session==null && this.proj!=null){
							this.session=(XnatImagesessiondata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), recon.getImageSessionId(), user, false);
						}
						if(this.session!=null){
							recon.setImageSessionId(this.session.getId());
						}
					}
				}

				if(recon.getImageSessionId()==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED,"Specified reconstruction must reference a valid image session.");
					return;
				}

				if(this.session==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Specified image session doesn't exist.");
					return;
				}

				if(recon.getId()==null){
					recon.setId(exptID);
				}

				if(this.getQueryVariable("type")!=null){
					recon.setType(this.getQueryVariable("type"));
				}

				//FIND PRE-EXISTING
				XnatReconstructedimagedata existing=null;

				if(recon.getXnatReconstructedimagedataId()!=null){						
					existing=(XnatReconstructedimagedata)XnatReconstructedimagedata.getXnatReconstructedimagedatasByXnatReconstructedimagedataId(recon.getXnatReconstructedimagedataId(), user, completeDocument);
				}					

				if(recon.getId()!=null){						
					existing=(XnatReconstructedimagedata)XnatReconstructedimagedata.getXnatReconstructedimagedatasById(recon.getId(), user, completeDocument);
				}		

				if(existing==null){
					if(!Permissions.canEdit(user, this.session)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for sessions in this project.");
						return;
					}
					//IS NEW
					if(recon.getId()==null || recon.getId().equals("")){
						String query = "SELECT count(id) AS id_count FROM xnat_reconstructedimagedata WHERE id='";

						String login = null;
						if (user != null){
							login= user.getUsername();
						}
						try {
							int i=1;
							Long idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + this.session.getId() + "_RECON_" +i + "';", "id_count", user.getDBName(), login);
							while (idCOUNT > 0){
								i++;
								idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query +this.session.getId() + "_RECON_" + i + "';", "id_count", user.getDBName(), login);
							}

							recon.setId("" + i);
						} catch (Exception e) {
							logger.error("",e);
						}
					}
				}else{
					if(!Permissions.canEdit(user, session)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient edit privileges for sessions in this project.");
						return;
					}
					//MATCHED
				}

				boolean allowDataDeletion=false;
				if(this.getQueryVariable("allowDataDeletion")!=null && this.getQueryVariable("allowDataDeletion").equals("true")){
					allowDataDeletion=true;
				}



				final ValidationResults vr = recon.validate();

				if (vr != null && !vr.isValid())
				{
					this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,vr.toFullString());
					return;
				}

				create(session,recon,false,allowDataDeletion,newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(recon.getXSIType(), recon==null)));
			}else{
				this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only reconstruction documents can be PUT to this address.");
			}
		} catch (ActionException e) {
			this.getResponse().setStatus(e.getStatus(),e.getMessage());
			return;
		} catch (InvalidValueException e) {
			logger.error("",e);
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
	public void handleDelete() {
		final UserI user = getUser();
		if (recon == null && exptID != null) {
			recon = XnatReconstructedimagedata.getXnatReconstructedimagedatasById(exptID, user, completeDocument);
		}

		if (recon == null) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified reconstruction.");
			return;
		}

		if (filepath != null && !filepath.equals("")) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return;
		}
		try {

			if (!Permissions.canDelete(user, session) || XDAT.getBoolSiteConfigurationProperty("security.prevent-data-deletion", false)) {
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
				return;
			}

			final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, session.getXSIType(), session.getId(), (proj == null) ? session.getProject() : proj.getId(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getDeleteAction(recon.getXSIType())));
			final EventMetaI          ci       = workflow.buildEvent();
			PersistentWorkflowUtils.save(workflow, ci);

			try {
				String removeFiles = this.getQueryVariable("removeFiles");
				if (removeFiles != null) {
					for (XnatAbstractresourceI om : recon.getOut_file()) {
						XnatAbstractresource resourceA = (XnatAbstractresource) om;
						resourceA.deleteWithBackup(session.getArchiveRootPath(), user, ci);
					}
				}
				SaveItemHelper.authorizedDelete(recon.getItem().getCurrentDBVersion(), user, ci);

				WorkflowUtils.complete(workflow, ci);

				Users.clearCache(user);
				MaterializedView.deleteByUser(user);
			} catch (Exception e) {
				WorkflowUtils.fail(workflow, ci);
				throw e;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
		} catch (Exception e) {
			e.printStackTrace();
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	public Representation represent(Variant variant) {
		if(recon==null&& exptID!=null){
				recon= XnatReconstructedimagedata.getXnatReconstructedimagedatasById(exptID, getUser(), completeDocument);
			}
		
		if(recon!=null){
			return this.representItem(recon.getItem(),overrideVariant(variant));
	
		}else{
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"Unable to find the specified reconstruction.");
			return null;
		}

	}
}
