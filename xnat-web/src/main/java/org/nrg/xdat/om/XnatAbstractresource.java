/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:02 UTC 2016
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
public abstract class XnatAbstractresource extends BaseXnatAbstractresource {

	public XnatAbstractresource(ItemI item)
	{
		super(item);
	}

	public XnatAbstractresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAbstractresource(UserI user)
	 **/
	public XnatAbstractresource()
	{}

	public XnatAbstractresource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
