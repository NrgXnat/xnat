/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatQcassessmentdataScan
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatQcassessmentdataScan;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.scanAssessors.ScanAssessorScanI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatQcassessmentdataScan extends AutoXnatQcassessmentdataScan implements ScanAssessorScanI{

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatQcassessmentdataScan(ItemI item)
	{
		super(item);
	}

	public BaseXnatQcassessmentdataScan(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatQcassessmentdataScan(UserI user)
	 **/
	public BaseXnatQcassessmentdataScan()
	{}

	public BaseXnatQcassessmentdataScan(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public String getSummary(){
		return "Present";
	}

}
