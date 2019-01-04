//Copyright 2015 Radiologics, Inc
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import java.util.ArrayList;
import java.util.List;

import org.nrg.action.ActionException;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.ListMultimap;

/**
 * @author james
 */
@Service
public class TriageFileService implements TriageService {
	private final static Logger logger = LoggerFactory.getLogger(TriageFileService.class);

	//@Autowired
	private Mover mover;
	
	public TriageFileService(){
	
	}
	
	@Autowired
	public TriageFileService(Mover mover){
		this.mover=mover;
	}
	
	@Override
	public List<String> move(UserI user,Integer eventId,Boolean overwrite,ListMultimap<String,Object> params,DataURIA src,ResourceURII dest) throws Exception{				
		List<String> duplicates=new ArrayList<String>();
		PersistentWorkflowI wrk=WorkflowUtils.getOrCreateWorkflowData(eventId, user,dest.getSecurityItem().getItem(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.TYPE.WEB_FORM, "Approve Quarantine File","Approve Quarantine File","Approve Quarantine File"));
		EventMetaI ci=wrk.buildEvent();
		try{
			duplicates=mover.move(user, eventId, overwrite, params, src,dest,ci);
			if(!overwrite && duplicates.size()>0){
				//only log non duplicate updates.
			}else{
				WorkflowUtils.complete(wrk, ci);
			}
		}catch(Exception ex){
			logger.warn("Quarantine Approval Failed");
			WorkflowUtils.fail(wrk, ci);
			throw ex;
		}
		return duplicates;
	}
	
	/*
	 * Checking if the target resource is locked before attempting to move.
	 * (non-Javadoc)
	 * @see org.nrg.xnat.services.triage.TriageService#isLocked(org.nrg.xdat.security.XDATUser, java.lang.Integer, java.lang.Boolean, com.google.common.collect.ListMultimap, org.nrg.xnat.helpers.uri.URIManager.DataURIA, org.nrg.xnat.helpers.uri.archive.ResourceURII)
	 */
	@Override
	public boolean isLocked(UserI user, Integer eventId, Boolean overwrite, ListMultimap<String, Object> otherParams,DataURIA src, ResourceURII dest)throws Exception {
		boolean isLocked=false;
		final String type=(String)dest.getProps().get(URIManager.TYPE);
		try {
			ResourceModifierA rModifier= mover.buildResourceModifier(user, dest, overwrite, type, null);
			if(rModifier!=null){
				XnatAbstractresource r=(XnatAbstractresource)rModifier.getResourceByIdentifier(dest.getResourceLabel(), type);
				if(r!=null){
					XFTItem item = r.getItem();
					if(item!=null){
						isLocked=item.isLocked();
					}
				}
			}
		} catch (ActionException e) {
			logger.error("Failed checking isLocked ",e);
			throw new Exception(e);
		} catch (MetaDataException e) {
			logger.error("Failed checking isLocked ",e);
			throw new Exception(e);
		}
		return isLocked;

	}
}