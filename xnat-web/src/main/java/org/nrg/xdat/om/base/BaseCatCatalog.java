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
public abstract class BaseCatCatalog extends AutoCatCatalog {

	public BaseCatCatalog(ItemI item)
	{
		super(item);
	}

	public BaseCatCatalog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseCatCatalog(UserI user)
	 **/
	public BaseCatCatalog()
	{}

	public BaseCatCatalog(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
