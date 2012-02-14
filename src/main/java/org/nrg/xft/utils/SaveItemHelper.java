// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.utils;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.security.UserI;

public class SaveItemHelper {
	static Logger logger = Logger.getLogger(SaveItemHelper.class);
	public static SaveItemHelper getInstance(){
		return new SaveItemHelper();
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval) throws Exception {
		ItemWrapper temp;
		if(i instanceof XFTItem){
			temp=(ItemWrapper)BaseElement.GetGeneratedItem(i);
		}else{
			temp=(ItemWrapper)i;
		}
		temp.preSave();
		temp.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval);
		temp.postSave();
	}

	protected boolean save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval) throws Exception {
		ItemWrapper temp;
		if(i instanceof XFTItem){
			temp=(ItemWrapper)BaseElement.GetGeneratedItem(i);
		}else{
			temp=(ItemWrapper)i;
		}
		temp.preSave();
        final boolean _success= temp.save(user,overrideSecurity,allowItemRemoval);
        if(_success)temp.postSave();
        return _success;
	}
	
	protected void delete(ItemI i, UserI user) throws SQLException, Exception{
		DBAction.DeleteItem(i.getItem(),user);
	}
	
	protected void removeItemReference(ItemI parent,String s, ItemI child, UserI user) throws SQLException, Exception{
        DBAction.RemoveItemReference(parent.getItem(),null,child.getItem(),user);
	}
	
	/**
	 * Remove child from parent without additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void authorizedRemoveChild(ItemI parent,String s, ItemI child, UserI user) throws SQLException, Exception{
		if(parent==null || child==null){
			throw new NullPointerException();
		}

		getInstance().removeItemReference(parent, s, child, user);
	}
	
	/**
	 * Remove child from parent with additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void unauthorizedRemoveChild(ItemI parent,String s, ItemI child, UserI user) throws SQLException, Exception{
		if(parent==null || child==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(parent.getItem(), user);

		Authorizer.getInstance().authorizeSave(child.getItem(), user);

		getInstance().removeItemReference(parent, s, child, user);
	}
	
	/**
	 * Delete resource without additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void authorizedDelete(XFTItem i, UserI user) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}

		getInstance().delete(i, user);
	}
	
	/**
	 * Delete resource with additional security precautions.
	 * @param i
	 * @param user
	 * @throws SQLException
	 * @throws Exception
	 */
	public static void unauthorizedDelete(XFTItem i, UserI user) throws SQLException, Exception{
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		getInstance().delete(i, user);
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
	public static void unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);
		
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval);
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
	public static void authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval);
	}

	/**
	 * Save resource with additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static boolean unauthorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem(), user);

		return getInstance().save(i, user, overrideSecurity, allowItemRemoval);
	}

	/**
	 * Save resource without additional security precautions.
	 * @param i
	 * @param user
	 * @param overrideSecurity
	 * @param allowItemRemoval
	 * @throws Exception
	 */
	public static boolean authorizedSave(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}
		
		return getInstance().save(i, user, overrideSecurity, allowItemRemoval);
	}
}
