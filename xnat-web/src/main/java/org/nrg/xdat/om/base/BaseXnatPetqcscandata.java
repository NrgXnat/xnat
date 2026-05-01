/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatPetqcscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatPetqcscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatPetqcscandata extends AutoXnatPetqcscandata {
	public BaseXnatPetqcscandata(ItemI item) {
		super(item);
	}

	public BaseXnatPetqcscandata(UserI user) {
		super(user);
	}

	public BaseXnatPetqcscandata() {
	}

	public BaseXnatPetqcscandata(Hashtable properties, UserI user) {
		super(properties, user);
	}
}
