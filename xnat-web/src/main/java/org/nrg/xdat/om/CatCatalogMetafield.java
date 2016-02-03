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
public class CatCatalogMetafield extends BaseCatCatalogMetafield {

	public CatCatalogMetafield(ItemI item)
	{
		super(item);
	}

	public CatCatalogMetafield(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseCatCatalogMetafield(UserI user)
	 **/
	public CatCatalogMetafield()
	{}

	public CatCatalogMetafield(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
