/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseProvProcess
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoProvProcess;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseProvProcess extends AutoProvProcess {

    @Serial
    private static final long serialVersionUID = 1;

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
