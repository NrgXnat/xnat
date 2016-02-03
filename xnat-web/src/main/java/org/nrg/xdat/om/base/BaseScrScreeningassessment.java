/*
 * org.nrg.xdat.om.base.BaseScrScreeningassessment
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;
import org.nrg.xdat.model.ScrScreeningscandataI;
import org.nrg.xdat.om.ScrScreeningscandata;
import org.nrg.xdat.om.base.auto.AutoScrScreeningassessment;
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
public abstract class BaseScrScreeningassessment extends AutoScrScreeningassessment implements ScanAssessorI {

	public BaseScrScreeningassessment(ItemI item)
	{
		super(item);
	}

	public BaseScrScreeningassessment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseScrScreeningassessment(UserI user)
	 **/
	public BaseScrScreeningassessment()
	{}

	public BaseScrScreeningassessment(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public ScrScreeningscandata getScanScreeningAssessment(String scanId){
		ScrScreeningscandata rtn = null;
		if (scanId == null) throw new NullPointerException("Expected a non-null value for the scan id input parameter");
		List<ScrScreeningscandataI> scans = super.getScans_scan();
		if (scans != null && scans.size() > 0) {
			for (int i=0; i< scans.size(); i++) {
				ScrScreeningscandata aScan = (ScrScreeningscandata)scans.get(i);
				if (aScan.getImagescanId().equals(scanId)) {
					rtn = aScan;
					break;
				}
			}
		}
		if (rtn == null) throw new NullPointerException("Couldnt find screening assessment for scan id " + scanId);
		return rtn;
	}
	
	public ScanAssessorScanI getScanById(String id){
		return this.getScanScreeningAssessment(id);
	}
	
	public String getHeader(){
		return "Screening Assess";
	}

	public int getPrecedence(){
		return 1;
	}
}
