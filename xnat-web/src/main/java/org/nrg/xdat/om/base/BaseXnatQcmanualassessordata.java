/*
 * org.nrg.xdat.om.base.BaseXnatQcmanualassessordata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.XnatQcscandata;
import org.nrg.xdat.om.base.auto.AutoXnatQcmanualassessordata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.scanAssessors.ScanAssessorI;
import org.nrg.xnat.scanAssessors.ScanAssessorScanI;

import java.util.Hashtable;
import java.util.List;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatQcmanualassessordata extends AutoXnatQcmanualassessordata implements ScanAssessorI{
	public BaseXnatQcmanualassessordata(ItemI item) {
		super(item);
	}

	public BaseXnatQcmanualassessordata(UserI user) {
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatQcmanualassessordata(UserI user)
	 */
	public BaseXnatQcmanualassessordata() {
	}

	public BaseXnatQcmanualassessordata(Hashtable properties, UserI user) {
		super(properties, user);
	}
	
	public ScanAssessorScanI getScanById(String scanId){
		XnatQcscandata rtn = null;
		if (scanId == null) throw new NullPointerException("Expected a non-null value for the scan id input parameter");
		List<XnatQcscandata> scans = super.getScans_scan();
		if (scans != null && scans.size() > 0) {
			for (int i=0; i< scans.size(); i++) {
				XnatQcscandata aScan = (XnatQcscandata)scans.get(i);
				if (aScan.getImagescanId().equals(scanId)) {
					rtn = aScan;
					break;
				}
			}
		}
		if (rtn == null) throw new NullPointerException("Couldnt find manual QC assessment for scan id " + scanId);
		return rtn;
	}
		
	public String getHeader(){
		return "Manual QC";
	}
	
	public int getPrecedence(){
		return 2;
	}
}
