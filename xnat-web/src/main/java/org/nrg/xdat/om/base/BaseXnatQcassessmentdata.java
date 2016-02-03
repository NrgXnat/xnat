/*
 * org.nrg.xdat.om.base.BaseXnatQcassessmentdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.XnatQcassessmentdataScan;
import org.nrg.xdat.om.base.auto.AutoXnatQcassessmentdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.scanAssessors.ScanAssessorI;
import org.nrg.xnat.scanAssessors.ScanAssessorScanI;

import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatQcassessmentdata extends AutoXnatQcassessmentdata implements ScanAssessorI{

	public BaseXnatQcassessmentdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatQcassessmentdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatQcassessmentdata(UserI user)
	 **/
	public BaseXnatQcassessmentdata()
	{}

	public BaseXnatQcassessmentdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public ScanAssessorScanI getScanById(String scanId){
		XnatQcassessmentdataScan rtn = null;
		if (scanId == null) throw new NullPointerException("Expected a non-null value for the scan id input parameter");
		List<XnatQcassessmentdataScan> scans = super.getScans_scan();
		if (scans != null && scans.size() > 0) {
			for (int i=0; i< scans.size(); i++) {
				XnatQcassessmentdataScan aScan = (XnatQcassessmentdataScan)scans.get(i);
				if (aScan.getId().equals(scanId)) {
					rtn = aScan;
					break;
				}
			}
		}
		if (rtn == null) throw new NullPointerException("Couldnt find QC assessment for scan id " + scanId);
		return rtn;
	}
		
	public String getHeader(){
		return this.getType();
	}
	
	public int getPrecedence(){
		return 3;
	}
}
