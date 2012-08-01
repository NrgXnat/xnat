// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.utils;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;

public class SaveItemHelper {
	private static final String ID_PLACEHOLDER = "NULL";
	static Logger logger = Logger.getLogger(SaveItemHelper.class);
	public static SaveItemHelper getInstance(){
		return new SaveItemHelper();
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(!StringUtils.IsEmpty(i.getItem().getGenericSchemaElement().getAddin())){
			i.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval,c);
		}else{
			ItemWrapper temp;
			try {
				if(i instanceof XFTItem){
					temp=(ItemWrapper)BaseElement.GetGeneratedItem(i);
				}else{
					temp=(ItemWrapper)i;
				}
			} catch (Exception e) {
				logger.error("",e);
				i.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval,c);	
				return;
			}
			temp.preSave();
			temp.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval,c);
			temp.postSave();
		}
	}

	protected boolean save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(!StringUtils.IsEmpty(i.getItem().getGenericSchemaElement().getAddin())){
			return i.save(user, overrideSecurity, allowItemRemoval,c);
		}else{
			ItemWrapper temp;
			try {
				if(i instanceof XFTItem){
					temp=(ItemWrapper)BaseElement.GetGeneratedItem(i);
				}else{
					temp=(ItemWrapper)i;
				}
			} catch (Throwable e) {
				logger.error("",e);
				return i.save(user,overrideSecurity,allowItemRemoval,c);				
			}
			temp.preSave();
	        final boolean _success= temp.save(user,overrideSecurity,allowItemRemoval,c);
	        if(_success)temp.postSave();
	        return _success;
		}
	}
	
	protected void delete(ItemI i, UserI user,EventMetaI c) throws SQLException, Exception{
		DBAction.DeleteItem(i.getItem(),user,c);
	}
	
	protected void removeItemReference(ItemI parent,String s, ItemI child, UserI user,EventMetaI c) throws SQLException, Exception{
        DBAction.RemoveItemReference(parent.getItem(),s,child.getItem(),user,c);
	}
	
	/**
	 * Remove child from parent without additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void authorizedRemoveChild(ItemI parent,String s, ItemI child, UserI user,EventMetaI c) throws SQLException, Exception{
		if(parent==null || child==null){
			throw new NullPointerException();
		}

		getInstance().removeItemReference(parent, s, child, user,c);
	}
	
	/**
	 * Remove child from parent with additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void unauthorizedRemoveChild(ItemI parent,String s, ItemI child, UserI user,EventMetaI c) throws SQLException, Exception{
		if(parent==null || child==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(parent.getItem(), user);

		Authorizer.getInstance().authorizeSave(child.getItem(), user);

		getInstance().removeItemReference(parent, s, child, user,c);
	}
	
	/**
	 * Delete resource without additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void authorizedDelete(XFTItem i, UserI user,EventMetaI c) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}

		getInstance().delete(i, user,c);
	}
	
	/**
	 * Delete resource without additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void authorizedDelete(XFTItem i, UserI user,EventDetails c) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}


		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),i.getPKValueString(),PersistentWorkflowUtils.getExternalId(i),c);
		
		try {
			getInstance().delete(i, user,wrk.buildEvent());
			
			PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			throw e;
		}
	}
	
	/**
	 * Delete resource with additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void unauthorizedDelete(XFTItem i, UserI user,EventMetaI c) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		getInstance().delete(i, user,c);
	}
	
	/**
	 * Delete resource with additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void unauthorizedDelete(XFTItem i, UserI user,EventDetails c) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),i.getPKValueString(),PersistentWorkflowUtils.getExternalId(i),c);
		
		try {
			getInstance().delete(i, user,wrk.buildEvent());
			
			PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			throw e;
		}
	}
	
	/**
	 * Save resource with additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param quarantine
	 * @param overrideQuarantine
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static void unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);
					
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,c);
	}

	/**
	 * Save resource with additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param quarantine
	 * @param overrideQuarantine
	 * @param allowItemRemoval
	 * @param c
	 * @throws Exception
	 */
	public static void unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventDetails c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);
		
		String id=i.getItem().getPKValueString();
        if(StringUtils.IsEmpty(id)){
        	id=ID_PLACEHOLDER;
        }

        PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),id,PersistentWorkflowUtils.getExternalId(i),c);
			
		final EventMetaI ci=wrk.buildEvent();
        
		try {
			getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,ci);
			
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.complete(wrk,ci);
		} catch (Exception e) {
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.fail(wrk,ci);
			throw e;
		}
	}
	
	/**
	 * Save resource without additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param quarantine
	 * @param overrideQuarantine
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static void authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,c);
	}
	
	/**
	 * Save resource without additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param quarantine
	 * @param overrideQuarantine
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static void authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,EventDetails c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		String id=i.getItem().getPKValueString();
        if(StringUtils.IsEmpty(id)){
        	id=ID_PLACEHOLDER;
        }

        PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),id,PersistentWorkflowUtils.getExternalId(i),c);
			
		final EventMetaI ci=wrk.buildEvent();
        
		try {
			getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval,ci);
			
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.complete(wrk,ci,overrideSecurity);
		} catch (Exception e) {
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.fail(wrk,ci,overrideSecurity);
			throw e;
		}
	}

	/**
	 * Save resource with additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static boolean unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		return getInstance().save(i, user, overrideSecurity, allowItemRemoval,c);
	}

	/**
	 * Save resource with additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static boolean unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventDetails c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);
		
		String id=i.getItem().getPKValueString();
        if(StringUtils.IsEmpty(id)){
        	id=ID_PLACEHOLDER;
        }

        PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),id,PersistentWorkflowUtils.getExternalId(i),c);
			
		final EventMetaI ci=wrk.buildEvent();
        
		try {
			boolean _return=getInstance().save(i, user, overrideSecurity, allowItemRemoval,ci);
			
			if(_return){
				if(id.equals(ID_PLACEHOLDER)){
					wrk.setId(i.getItem().getPKValueString());
				}
				
				PersistentWorkflowUtils.complete(wrk,ci);
			}
			return _return;
		} catch (Exception e) {
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.fail(wrk,ci);
			throw e;
		}
	}

	/**
	 * Save resource without additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param allowItemRemoval
	 * @throws Exception
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
	
	
	 */
	public static boolean authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventMetaI c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		return getInstance().save(i, user, overrideSecurity, allowItemRemoval,c);
			
		
	}
	
	public static boolean authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval,EventDetails c) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		String id=i.getItem().getPKValueString();
        if(StringUtils.IsEmpty(id)){
        	id=ID_PLACEHOLDER;
        }

        PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow((XDATUser)user,i.getXSIType(),id,PersistentWorkflowUtils.getExternalId(i),c);
			
		final EventMetaI ci=wrk.buildEvent();
        
		try {
			boolean _return=getInstance().save(i, user, overrideSecurity, allowItemRemoval,ci);
			
			if(_return){
				if(id.equals(ID_PLACEHOLDER)){
					wrk.setId(i.getItem().getPKValueString());
				}
				
				PersistentWorkflowUtils.complete(wrk,ci);
			}
			return _return;
		} catch (Exception e) {
			if(id.equals(ID_PLACEHOLDER)){
				wrk.setId(i.getItem().getPKValueString());
			}
			
			PersistentWorkflowUtils.fail(wrk,ci);
			throw e;
		}
		
	}
}
