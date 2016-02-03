/*
 * org.nrg.xdat.om.base.BaseXnatMrqcscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatMrqcscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatMrqcscandata extends AutoXnatMrqcscandata {
	public BaseXnatMrqcscandata(ItemI item) {
		super(item);
	}

	public BaseXnatMrqcscandata(UserI user) {
		super(user);
	}

	public BaseXnatMrqcscandata() {
	}

	public BaseXnatMrqcscandata(Hashtable properties, UserI user) {
		super(properties, user);
	}
}
