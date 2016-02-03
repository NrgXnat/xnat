/*
 * org.nrg.xdat.om.base.BaseXnatMegscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatMegscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatMegscandata extends AutoXnatMegscandata {

	public BaseXnatMegscandata(ItemI item) {
		super(item);
	}

	public BaseXnatMegscandata(UserI user) {
		super(user);
	}

	public BaseXnatMegscandata() {
	}

	public BaseXnatMegscandata(Hashtable properties, UserI user) {
		super(properties, user);
	}

}
