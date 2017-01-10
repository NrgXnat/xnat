/*
 * web: org.nrg.xnat.restlet.resources.files.CatalogResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.restlet.representations.BeanRepresentation;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.nrg.xnat.restlet.resources.ScanResource;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Collection;

public class CatalogResource extends XNATCatalogTemplate {
	final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ScanResource.class);
	
	private boolean filePathIsEmpty;  

	public CatalogResource(Context context, Request request, Response response) {
		super(context, request, response,false);
		
		checkForNonEmptyFilePath(this.getRequest().getResourceRef().getRemainingPart());

			try {
				if(catalogs!=null && catalogs.size()>0){
					for(Object[] row: catalogs.rows()){
						Integer id = (Integer)row[0];
						String label = (String)row[1];
						
						for(String resourceID:this.resource_ids){
							if(id.toString().equals(resourceID) || (label!=null && label.equals(resourceID))){
								resources.add(XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(row[0], getUser(), false));
							}
						}
					
				}
				}
				
				this.getVariants().add(new Variant(MediaType.TEXT_XML));
			} catch (Exception e) {
	            logger.error("",e);
			}
	}

        private void checkForNonEmptyFilePath(String remainingUrlPart) {
        	// we don't care about path separators or query parameters
        	// everything else will be rejected
        	filePathIsEmpty = StringUtils.isBlank(remainingUrlPart)
        		|| remainingUrlPart.matches("^/+") || remainingUrlPart.matches("^/*\\?.*");
        }
	
	/**
	 * See XNAT-1674.  If the client mistakenly passes a file path to us, slap the wrist.
	 * This is better than discarding the file path and naively processing the request 
	 * (and say, deleting the whole catalog instead of the individual file delete that was desired).
	 */
	private boolean failFastDueToNonEmptyFilePath() {
	    if(filePathIsEmpty) {
		return false;
	    }
	    else {
		this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "This resource works at the catalog level only and does not accept file paths.  To work with the resource files, append '/files' after the resource ID.");
		return true;
	    }
	}

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePut() {
        handlePost();
    }

    @Override
    public void handlePost() {
		if(failFastDueToNonEmptyFilePath()) {
			return;
		}

		if(this.parent!=null && this.security!=null){
			XFTItem item;
			final UserI user = getUser();
			try {
				if(Permissions.canEdit(user,this.security)){
					if(this.resources.size()>0){
						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified resource already exists.");
						return;
					}

					item=this.loadItem("xnat:resourceCatalog",true);

					if(item==null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
						return;
					}

					if(item.instanceOf("xnat:resourceCatalog")){
						XnatResourcecatalog catResource = (XnatResourcecatalog)BaseElement.GetGeneratedItem(item);

						if(catResource.getXnatAbstractresourceId()!=null){
							XnatAbstractresource existing=XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(catResource.getXnatAbstractresourceId(), user, false);
							if(existing!=null){
								this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Specified catalog already exists.");
								//MATCHED
								return;
							}else{
								this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Contains erroneous generated fields (xnat_abstractresource_id).");
								//MATCHED
								return;
							}
						}

						setCatalogAttributes(user, catResource);

						catResource.setLabel(resource_ids.get(0));

						PersistentWorkflowI wrk=PersistentWorkflowUtils.getWorkflowByEventId(user,getEventId());
						if(wrk==null && "SNAPSHOTS".equals(catResource.getLabel())){
							if(getSecurityItem() instanceof XnatExperimentdata){
								Collection<? extends PersistentWorkflowI> wrks=PersistentWorkflowUtils.getOpenWorkflows(user,((ArchivableItem)getSecurityItem()).getId());
								if(wrks!=null && wrks.size()==1){
									wrk=(WrkWorkflowdata)CollectionUtils.get(wrks, 0);
									if(!"xnat_tools/AutoRun.xml".equals(wrk.getPipelineName())){
										wrk=null;
									}else{
										if(StringUtils.isBlank(wrk.getCategory())){
											wrk.setCategory(EventUtils.CATEGORY.DATA);
											wrk.setType(EventUtils.TYPE.PROCESS);
											WorkflowUtils.save(wrk, wrk.buildEvent());
										}
									}
								}
							}
						}
						
						boolean isNew=false;
						if(wrk==null){
							isNew=true;
							wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, getSecurityItem().getItem(), newEventInstance(EventUtils.CATEGORY.DATA,(getAction()!=null)?getAction():EventUtils.CREATE_RESOURCE));
						}

						assert wrk != null;
						EventMetaI ci=wrk.buildEvent();

						this.insertCatalog(catResource);

						if(isNew){
							WorkflowUtils.complete(wrk, ci);
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,"Only ResourceCatalog documents can be PUT to this address.");
					}
				}else{
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"User account doesn't have permission to modify this session.");
				}

			} catch (ActionException e) {
				this.getResponse().setStatus(e.getStatus(),e.getMessage());
			} catch (Exception e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				logger.error("",e);
			}
		}
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void handleDelete(){
		if(failFastDueToNonEmptyFilePath()) {
			return;
		}

		if(resources.size()>0 && this.parent!=null && this.security!=null){
			final UserI user = getUser();
			for(XnatAbstractresource resource:resources){
				try {
					if(Permissions.canDelete(user,this.security)){
						String securityId=null;
                        String xsiType=null;
                        if(parent.getItem().instanceOf("xnat:experimentData")){
                            securityId=((XnatExperimentdata)parent).getId();
                            xsiType=parent.getXSIType();
                            if(proj==null) proj = ((XnatExperimentdata)parent).getPrimaryProject(false);
                        }else if(security.getItem().instanceOf("xnat:experimentData")){
                            securityId=((XnatExperimentdata)security).getId();
                            xsiType=security.getXSIType();
                            if(proj==null) proj = ((XnatExperimentdata)security).getPrimaryProject(false);
                        }else if(parent.getItem().instanceOf("xnat:subjectData")){
                            securityId=((XnatSubjectdata)parent).getId();
                            xsiType=parent.getXSIType();
                            if(proj==null) proj = ((XnatSubjectdata)parent).getPrimaryProject(false);
                        }else if(security.getItem().instanceOf("xnat:subjectData")){
                            securityId=((XnatSubjectdata)security).getId();
                            xsiType=security.getXSIType();
                            if(proj==null) proj = ((XnatSubjectdata)security).getPrimaryProject(false);
                        }else if(parent.getItem().instanceOf("xnat:projectData")){
                            securityId=((XnatProjectdata)parent).getId();
                            xsiType=parent.getXSIType();
                            if(proj==null) proj = ((XnatProjectdata)security);
                        }else if(security.getItem().instanceOf("xnat:projectData")){
                            securityId=((XnatProjectdata)security).getId();
                            xsiType=security.getXSIType();
                            if(proj==null) proj = ((XnatProjectdata)security);
                        }

						final String rootPath=proj.getRootArchivePath();

						if(!((security).getItem().isActive() || (security).getItem().isQuarantine() )){ 
							//cannot modify it if it isn't active
							throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,new Exception());
						}
						
						final PersistentWorkflowI workflow=PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, xsiType, securityId, (proj==null)?null:proj.getId(),newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.REMOVE_CATALOG));
				    	EventMetaI ci=workflow.buildEvent();
						
						try {
							resource.deleteWithBackup(rootPath, user, ci);
							SaveItemHelper.authorizedRemoveChild(this.parent.getItem(), xmlPath, resource.getItem(), user,ci);

							PersistentWorkflowUtils.complete(workflow,ci);
						} catch (Exception e) {
							PersistentWorkflowUtils.fail(workflow,ci);
							throw e;
						}
					}else{
						this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"User account doesn't have permission to modify this session.");
						return;
					}
				} catch (Exception e) {
					logger.error("",e);
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
					return;
				}
			}
		}
	}
	
	private void getAllMatches(){
		catalogs=null;
		resources= new ArrayList<>();
		try {
			catalogs=this.loadCatalogs(resource_ids,false,true);
		} catch (Exception e) {
            logger.error("",e);
		}
		
		if(catalogs!=null && catalogs.size()>0){
			for(Object[] row: catalogs.rows()){
				Integer id = (Integer)row[0];
				String label = (String)row[1];
				
				for(String resourceID:this.resource_ids){
					if(id.toString().equals(resourceID) || (label!=null && label.equals(resourceID))){
						resources.add(XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(row[0], getUser(), false));
					}
				}
				
			}
		}
	}
	

	@Override
	public Representation represent(Variant variant) {
	    	if(failFastDueToNonEmptyFilePath()) {
	    	    return null;
	    	}
	    	
		this.getAllMatches();
		
		if(resources.size()==0){
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find the specified catalog.");
		}else if(resources.size()==1){
			XnatAbstractresource resource=resources.get(0);
			try {
				if(proj==null){
					if(parent.getItem().instanceOf("xnat:experimentData")){
						proj = ((XnatExperimentdata)parent).getPrimaryProject(false);
					}else if(security.getItem().instanceOf("xnat:experimentData")){
						proj = ((XnatExperimentdata)security).getPrimaryProject(false);
					}
				}
				
				if(resource.getItem().instanceOf("xnat:resourceCatalog")){
					boolean includeRoot=this.isQueryVariableTrue("includeRootPath");
					
					XnatResourcecatalog catResource = (XnatResourcecatalog)resource;
					CatCatalogBean cat= catResource.getCleanCatalog(proj.getRootArchivePath(),includeRoot,null,null);
			    	
			    	if(cat!=null)
						return new BeanRepresentation(cat,MediaType.TEXT_XML);
			    	else{
			    		this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"Unable to find catalog file.");
			    	}
				}else{
					return new ItemXMLRepresentation(resource.getItem(),MediaType.TEXT_XML);
				}
			} catch (ElementNotFoundException e) {
	            logger.error("",e);
			}
		}

		return null;

	}
}
