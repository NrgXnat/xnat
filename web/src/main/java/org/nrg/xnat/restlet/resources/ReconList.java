/*
 * web: org.nrg.xnat.restlet.resources.ReconList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.action.ActionException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;

public class ReconList extends QueryOrganizerResource {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ReconList.class);

	XnatProjectdata proj=null;
	XnatSubjectdata sub=null;
	XnatImagesessiondata session=null;
	XnatReconstructedimagedata recon=null;

	public ReconList(Context context, Request request, Response response) {
		super(context, request, response);
		final UserI user = getUser();

		String pID = (String) getParameter(request, "PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

			String subID = (String) getParameter(request, "SUBJECT_ID");
			if (subID != null) {
				sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
																			.getId(), subID, user, false);

				if (sub == null) {
					sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
																  false);
					if (sub != null
						&& (proj != null && !sub.hasProject(proj.getId()))) {
						sub = null;
					}
				}

				if (sub != null) {
					String exptID = (String) getParameter(request,
														  "ASSESSED_ID");
					session = XnatImagesessiondata
							.getXnatImagesessiondatasById(exptID, user, false);
					if (session != null
						&& (proj != null && !session.hasProject(proj
																		.getId()))) {
						session = null;
					}

					if (session == null) {
						session = (XnatImagesessiondata) XnatImagesessiondata
								.GetExptByProjectIdentifier(proj.getId(),
															exptID, user, false);
					}

					if (session != null) {
						this.getVariants().add(
								new Variant(MediaType.APPLICATION_JSON));
						this.getVariants()
							.add(new Variant(MediaType.TEXT_HTML));
						this.getVariants().add(new Variant(MediaType.TEXT_XML));
					} else {
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					}
				} else {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}
			} else {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		} else {
			String exptID = (String) getParameter(request, "ASSESSED_ID");
			session = XnatImagesessiondata.getXnatImagesessiondatasById(exptID,
																		user, false);

			if (session == null) {
				session = (XnatImagesessiondata) XnatImagesessiondata
						.GetExptByProjectIdentifier(proj.getId(), exptID, user,
													false);
			}

			if (session != null) {
				this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
				this.getVariants().add(new Variant(MediaType.TEXT_HTML));
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			} else {
				response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}

		this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.RECON_DATA, true));

	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		final UserI user = getUser();

			try {
				XFTItem item = null;
				item=this.loadItem("xnat:reconstructedImageData",true);
			
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
				
				if(item.instanceOf("xnat:reconstructedImageData")){
					recon = (XnatReconstructedimagedata)BaseElement.GetGeneratedItem(item);
					
					//MATCH SESSION
					if(this.session!=null){
						recon.setImageSessionId(this.session.getId());
					}else{
						if(recon.getImageSessionId()!=null && !recon.getImageSessionId().equals("")){
							this.session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(recon.getImageSessionId(), user, false);
							
							if(this.session==null && this.proj!=null){
							this.session=(XnatImagesessiondata)XnatExperimentdata.GetExptByProjectIdentifier(this.proj.getId(), recon.getImageSessionId(),user, false);
							}
							
							if(this.session!=null){
								recon.setImageSessionId(this.session.getId());
							}
						}
					}
					
					if(recon.getImageSessionId()==null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED,"Specified scan must reference a valid image session.");
						return;
					}
					
					if(this.session==null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Specified image session doesn't exist.");
						return;
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
						if(!Permissions.canEdit(user,this.session)){
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient create privileges for sessions in this project.");
							return;
						}
						//IS NEW
						if(recon.getId()==null || recon.getId().equals("")){
							String query = "SELECT count(id) AS id_count FROM xnat_reconstructedimagedata WHERE id='";

					        String login = null;
					        if (user!=null){
					            login=user.getUsername();
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
						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified reconstruction already exists.");
					return;
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
				
				this.returnSuccessfulCreateFromList(recon.getId());
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only Scan documents can be PUT to this address.");
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
		al.add("xnat_reconstructedimagedata_id");
		al.add("ID");
		al.add("type");
		al.add("baseScanType");
		
		return al;
	}

	public String getDefaultElementName(){
		return "xnat:reconstructedImageData";
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		if(recon!=null){
			return new ItemXMLRepresentation(recon.getItem(),MediaType.TEXT_XML);
		}else{
			Representation rep=super.getRepresentation(variant);
			if(rep!=null)return rep;
			
			XFTTable table;
			try {
				final UserI user = getUser();
				final String re=this.getRootElementName();
				
				final QueryOrganizer qo = new QueryOrganizer(re, user,
						ViewManager.ALL);

				this.populateQuery(qo);

				CriteriaCollection cc= new CriteriaCollection("AND");
				cc.addClause(re+"/image_session_id", session.getId());
				qo.setWhere(cc);
				
				String query = qo.buildQuery();

					table=XFTTable.Execute(query, user.getDBName(), userName);
					
				table = formatHeaders(table, qo, "xnat:reconstructedImageData/ID",
						String.format("/data/experiments/%s/reconstructions/",session.getId()));
				} catch (Exception e) {
				logger.error("", e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
				}
	
			MediaType mt = overrideVariant(variant);
			Hashtable<String, Object> params = new Hashtable<>();
			if (table != null)
				params.put("totalRecords", table.size());
			return this.representTable(table, mt, params);
		}
	}
}
