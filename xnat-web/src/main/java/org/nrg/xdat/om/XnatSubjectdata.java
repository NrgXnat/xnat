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
public class XnatSubjectdata extends BaseXnatSubjectdata {

	public XnatSubjectdata(ItemI item)
	{
		super(item);
	}

	public XnatSubjectdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSubjectdata(UserI user)
	 **/
	public XnatSubjectdata()
	{}

	public XnatSubjectdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
