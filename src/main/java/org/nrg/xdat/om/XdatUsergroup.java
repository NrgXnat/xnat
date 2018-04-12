/*
 * core: org.nrg.xdat.om.XdatUsergroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import org.nrg.xdat.om.base.BaseXdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUsergroup extends BaseXdatUsergroup {
    public static final Pattern PROJECT_GROUP = Pattern.compile("(?<project>.*)_(owner|member|collaborator)?");

    public XdatUsergroup(ItemI item)
	{
		super(item);
	}

	public XdatUsergroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUsergroup(UserI user)
	 **/
	public XdatUsergroup()
	{}

	public XdatUsergroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
}
