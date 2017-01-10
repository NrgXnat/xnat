/*
 * web: org.nrg.xnat.restlet.resources.ProtocolResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.action.ActionException;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.om.XnatDatatypeprotocol;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.xml.sax.SAXParseException;

public class ProtocolResource extends ItemResource {
	XnatProjectdata proj=null;

	XnatDatatypeprotocol protocol = null;
	String protID=null;
	
	XnatDatatypeprotocol existing =null;
	
	public ProtocolResource(Context context, Request request, Response response) {
		super(context, request, response);

		final UserI user = getUser();

		final String pID = (String) getParameter(request,"PROJECT_ID");
		if (pID != null) {
			proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);
		}

		protID = (String) getParameter(request,"PROTOCOL_ID");

		if (proj != null)
			existing = (XnatDatatypeprotocol) XnatAbstractprotocol
					.getXnatAbstractprotocolsById(protID, user, true);

		this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public void handlePut() {
		try {
			final UserI user = getUser();
			XFTItem template=null;
			if (existing!=null){
				template=existing.getItem().getCurrentDBVersion();
			}
			
			XFTItem item=this.loadItem("xnat:datatypeProtocol",true,template);
			
			if(item.instanceOf("xnat:datatypeProtocol")){
				protocol = new XnatDatatypeprotocol(item);
					
					if(this.proj==null && protocol.getProject()!=null){
						proj = XnatProjectdata.getXnatProjectdatasById(protocol.getProject(), user, false);
					}

					if(this.proj!=null){
						if(protocol.getProject()==null || protocol.getProject().equals("")){
							protocol.setProperty("xnat_projectdata_id", proj.getId());
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Submitted subject record must include the project attribute.");
						return;
					}
					
					if(existing==null){
						if(!Permissions.canEdit(user,proj)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient edit privileges for this project.");
							return;
						}
						//IS NEW
						if(protocol.getId()==null || protocol.getId().equals("")){
							protocol.setId(protocol.getDataType());
						}
					}else{							
						if(!Permissions.canEdit(user,proj)){
							this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Specified user account has insufficient edit privileges for this project.");
							return;
						}
						if(protocol.getId()==null || protocol.getId().equals("")){
							protocol.setId(existing.getId());
						}
					}
					
					if(this.getQueryVariable("gender")!=null){
						protocol.setProperty("xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender",this.getQueryVariable("gender"));
					}
											
					PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, proj.getItem(), this.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Modified event data-type protocol."));
				    try {
						if(SaveItemHelper.authorizedSave(protocol,user,false,true,wrk.buildEvent())){
							PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
							MaterializedView.deleteByUser(user);
						}
						
						this.returnXML(protocol.getItem());
					} catch (Exception e) {
						PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
						throw e;
					}
				}
		} catch (SAXParseException e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,e.getMessage());
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
		if(existing!=null){
			protocol=existing;
		}

		final UserI user = getUser();

		try {
		
			if(!Permissions.canEdit(user,proj)){
				this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"User account doesn't have permission to delete this subject.");
				return;
			}
		
			if(protocol!=null){
				if (protocol!=null){				        
					PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, proj.getItem(), this.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Deleted event data-type protocol."));
				    try {
						SaveItemHelper.authorizedDelete(protocol.getItem().getCurrentDBVersion(), user,wrk.buildEvent());
						PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
					} catch (Exception e1) {
						PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
						throw e1;
					}
			    }
				Users.clearCache(user);
				MaterializedView.deleteByUser(user);
			}
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
		}
	}

	@Override
	public Representation represent(Variant variant) {
		MediaType mt = overrideVariant(variant);
		final UserI user = getUser();

		if(protocol!=null){
			return this.representItem(protocol.getItem(),mt);
		}else{
			if(this.getQueryVariable("dataType")!=null && proj!=null){
				String dataType=this.getQueryVariable("dataType");
				XnatDatatypeprotocol temp = (XnatDatatypeprotocol)proj.getProtocolByDataType(dataType);
				
				try {
					ElementSecurity ess = ElementSecurity.GetElementSecurity(dataType);
					
					if(temp==null && ess!=null){
						GenericWrapperElement e=GenericWrapperElement.GetElement(dataType);
						temp=new XnatDatatypeprotocol(user);
						temp.setProperty("xnat_projectdata_id", proj.getId());
						temp.setDataType(e.getXSIType());
						
						temp.setId(proj.getId() + "_" + e.getSQLName());
					    if (temp.getProperty("name")==null){
					    	temp.setProperty("name",ess.getPluralDescription());
					    }
					    
					    if(temp.getXSIType().equals("xnat:datatypeProtocol")){
					    	temp.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/data-type", temp.getProperty("data-type"));
					    	temp.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/project-specific", "false");
					    }
					    PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, proj.getItem(), this.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Modified event data-type protocol."));
					    try {
							SaveItemHelper.authorizedSave(temp,user, false, false,wrk.buildEvent());
							PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
						} catch (Exception e1) {
							PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
							throw e1;
						}
					}
				} catch (Exception e) {
					logger.error("", e);
				}
				
				if(temp==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified protocol.");
					return null;
				}else{
					return this.representItem(temp.getItem(),mt);
				}
			}else{
				this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified protocol.");
				return null;
			}
		}

	}
}
