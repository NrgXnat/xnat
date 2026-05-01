/*
 * GENERATED FILE
 *
 */
package org.nrg.xdat.om.base;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.nrg.xdat.BaseElementDeserializer;
import org.nrg.xdat.BaseElementSerializer;
import org.nrg.xdat.om.base.auto.*;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.ajax.writer.JSONWriter;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatContrastbolusv2 extends AutoXnatContrastbolusv2 {

	public BaseXnatContrastbolusv2(ItemI item)
	{
		super(item);
	}

	public BaseXnatContrastbolusv2(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatContrastbolusv2(UserI user)
	 **/
	public BaseXnatContrastbolusv2()
	{}

	public BaseXnatContrastbolusv2(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


}
