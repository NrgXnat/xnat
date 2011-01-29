// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.utils;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.ItemWrapper;


import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;

public class SaveItemHelper {
	static Logger logger = Logger.getLogger(SaveItemHelper.class);
	public static SaveItemHelper getInstance(){
		return new SaveItemHelper();
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem().getGenericSchemaElement(), user);

		if(i instanceof XFTItem){
			ItemI temp=BaseElement.GetGeneratedItem(i);
			temp.save(user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval);
		}else{
			i.save(user,overrideSecurity,quarantine,overrideQuarantine,allowItemRemoval);
		}
	}

	protected void save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval) throws Exception {
		if(i==null){
			throw new NullPointerException();
		}

		Authorizer.getInstance().authorizeSave(i.getItem().getGenericSchemaElement(), user);

		if(i instanceof XFTItem){
			ItemI temp=BaseElement.GetGeneratedItem(i);
			temp.save(user, overrideSecurity, allowItemRemoval);
		}else{
			i.save(user,overrideSecurity,allowItemRemoval);
		}
	}

	public static void Save(ItemI i,UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval) throws Exception {
		getInstance().save(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval);
	}

	public static void Save(ItemI i,UserI user, boolean overrideSecurity, boolean allowItemRemoval) throws Exception {
		getInstance().save(i, user, overrideSecurity, allowItemRemoval);
	}
}
