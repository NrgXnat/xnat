/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatPetqcscandataProcessingerror
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatPetqcscandataProcessingerror;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatPetqcscandataProcessingerror extends AutoXnatPetqcscandataProcessingerror {
    @Serial
    private static final long serialVersionUID = 1;
	public BaseXnatPetqcscandataProcessingerror(ItemI item) {
		super(item);
	}

	public BaseXnatPetqcscandataProcessingerror(UserI user) {
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatPetqcscandataProcessingerror(UserI user)
	 */
	public BaseXnatPetqcscandataProcessingerror() {
	}

	public BaseXnatPetqcscandataProcessingerror(Hashtable properties, UserI user) {
		super(properties, user);
	}

}
