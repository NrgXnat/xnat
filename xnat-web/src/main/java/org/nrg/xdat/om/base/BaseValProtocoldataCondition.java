/*
 * org.nrg.xdat.om.base.BaseValProtocoldataCondition
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoValProtocoldataCondition;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseValProtocoldataCondition extends AutoValProtocoldataCondition {

	public BaseValProtocoldataCondition(ItemI item)
	{
		super(item);
	}

	public BaseValProtocoldataCondition(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseValProtocoldataCondition(UserI user)
	 **/
	public BaseValProtocoldataCondition()
	{}

	public BaseValProtocoldataCondition(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public String getExpectedValue() {
		String rtn ="";
		String diagnosis = getDiagnosis();
		int expectedIndex = diagnosis.indexOf("Expected:");
		if (expectedIndex != -1) {
			int foundIndex = diagnosis.indexOf("Found:");
			if (foundIndex != -1) {
			   	rtn = diagnosis.substring(expectedIndex + 9 ,foundIndex);
			}else {
				rtn = diagnosis.substring(expectedIndex + 9);
			}
		}
		return rtn.trim();
	}

	public String getFoundValue() {
		String rtn ="";
		String diagnosis = getDiagnosis();
		int foundIndex = diagnosis.indexOf("Found:");
		if (foundIndex != -1) {
		   	rtn = diagnosis.substring(foundIndex + 6);
		}
		return rtn.trim();
	}

}
