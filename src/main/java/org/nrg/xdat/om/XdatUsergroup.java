// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Jun 29 12:54:15 CDT 2007
 *
 */
package org.nrg.xdat.om;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.nrg.xdat.om.base.BaseXdatUsergroup;
import org.nrg.xdat.security.ElementAccessManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUsergroup extends BaseXdatUsergroup {

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
