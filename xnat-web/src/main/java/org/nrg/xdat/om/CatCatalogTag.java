/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:01 UTC 2016
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xdat.om.base.*;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CatCatalogTag extends BaseCatCatalogTag {

	public CatCatalogTag(ItemI item)
	{
		super(item);
	}

	public CatCatalogTag(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseCatCatalogTag(UserI user)
	 **/
	public CatCatalogTag()
	{}

	public CatCatalogTag(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
