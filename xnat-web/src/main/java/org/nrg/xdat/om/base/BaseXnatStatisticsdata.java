/*
 * org.nrg.xdat.om.base.BaseXnatStatisticsdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatStatisticsdataAddfieldI;
import org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI;
import org.nrg.xdat.om.XnatStatisticsdataAddfield;
import org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics;
import org.nrg.xdat.om.base.auto.AutoXnatStatisticsdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatStatisticsdata extends AutoXnatStatisticsdata {

	public BaseXnatStatisticsdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatStatisticsdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatStatisticsdata(UserI user)
	 **/
	public BaseXnatStatisticsdata()
	{}

	public BaseXnatStatisticsdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public XnatStatisticsdataAdditionalstatistics getAdditionalstatistics(String name) {
		XnatStatisticsdataAdditionalstatistics rtn = null;
		List<XnatStatisticsdataAdditionalstatisticsI> additionalStats = getAdditionalstatistics() ;
		for (int i = 0; i < additionalStats.size(); i++) {
			if (additionalStats.get(i).getName().equals(name)) {
				rtn = (XnatStatisticsdataAdditionalstatistics)additionalStats.get(i);
				break;
			}
		}
		return rtn;
	}	

	public XnatStatisticsdataAddfield getAddfield(String name) {
		XnatStatisticsdataAddfield rtn = null;
		List<XnatStatisticsdataAddfieldI> additionalFields = getAddfield() ;
		for (int i = 0; i < additionalFields.size(); i++) {
			if (additionalFields.get(i).getName().equals(name)) {
				rtn = (XnatStatisticsdataAddfield)additionalFields.get(i);
				break;
			}
		}
		return rtn;
	}


}
