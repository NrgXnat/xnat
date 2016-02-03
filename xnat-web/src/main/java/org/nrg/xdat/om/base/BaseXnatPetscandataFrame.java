/*
 * org.nrg.xdat.om.base.BaseXnatPetscandataFrame
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatPetscandataFrame;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatPetscandataFrame extends AutoXnatPetscandataFrame {

	public BaseXnatPetscandataFrame(ItemI item)
	{
		super(item);
	}

	public BaseXnatPetscandataFrame(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatPetscandataFrame(UserI user)
	 **/
	public BaseXnatPetscandataFrame()
	{}

	public BaseXnatPetscandataFrame(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
