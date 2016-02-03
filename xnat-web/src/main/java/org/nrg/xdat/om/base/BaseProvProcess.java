/*
 * org.nrg.xdat.om.base.BaseProvProcess
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoProvProcess;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseProvProcess extends AutoProvProcess {

	public BaseProvProcess(ItemI item)
	{
		super(item);
	}

	public BaseProvProcess(UserI user)
	{
		super(user);
	}

	public BaseProvProcess()
	{}

	public BaseProvProcess(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
