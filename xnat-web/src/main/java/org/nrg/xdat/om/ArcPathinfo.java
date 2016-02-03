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
public class ArcPathinfo extends BaseArcPathinfo {

	public ArcPathinfo(ItemI item)
	{
		super(item);
	}

	public ArcPathinfo(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcPathinfo(UserI user)
	 **/
	public ArcPathinfo()
	{}

	public ArcPathinfo(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
