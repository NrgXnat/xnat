// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.utils;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;

public class SaveItemHelper {
	static Logger logger = Logger.getLogger(SaveItemHelper.class);
	public static SaveItemHelper getInstance(){
		return new SaveItemHelper();
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem().getGenericSchemaElement(), user);

		if(i instanceof XFTItem){
			ItemI temp=BaseElement.GetGeneratedItem(i);
			temp.save(user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,c);
		}else{
			i.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval,c);
		}
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval, EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem().getGenericSchemaElement(), user);

		if(i instanceof XFTItem){
			ItemI temp=BaseElement.GetGeneratedItem(i);
			temp.save(user, overrideSecurity, allowItemRemoval,c);
		}else{
			i.save(user,overrideSecurity,allowItemRemoval,c);
		}
	}

	public static void Save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,c);
	}

	public static void Save(ItemI item,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventDetails event) throws Exception {
		PersistentWorkflowI wrk=null;
	    EventMetaI c;
	    if(item.getItem().instanceOf("xnat:experimentData") ||
	    		item.getItem().instanceOf("xnat:subjectData") ||
	    		item.getItem().instanceOf("xnat:projectData")){
	    	wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user, item.getItem(), event);
	    	c=wrk.buildEvent();
	    }else{
	    	c=EventUtils.ADMIN_EVENT(user);
	    }
	    
	    try {
	    	SaveItemHelper.Save(item,user,overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,c);
			if(wrk!=null)PersistentWorkflowUtils.complete(wrk, c);
		} catch (Exception e) {
			if(wrk!=null)PersistentWorkflowUtils.fail(wrk, c);
			throw e;
		}
	}
	
	

	public static void Save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception {
		getInstance().save(i, user, overrideSecurity, allowItemRemoval,c);
	}
}
