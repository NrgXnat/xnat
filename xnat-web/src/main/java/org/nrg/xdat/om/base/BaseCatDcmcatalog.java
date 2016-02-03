/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:01 UTC 2016
 *
 */
package org.nrg.xdat.om.base;
import org.nrg.xdat.om.base.auto.*;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseCatDcmcatalog extends AutoCatDcmcatalog {

	public BaseCatDcmcatalog(ItemI item)
	{
		super(item);
	}

	public BaseCatDcmcatalog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseCatDcmcatalog(UserI user)
	 **/
	public BaseCatDcmcatalog()
	{}

	public BaseCatDcmcatalog(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
