/*
 * org.nrg.xdat.om.base.BaseValProtocoldata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.ValProtocoldataScanCheckI;
import org.nrg.xdat.om.ValProtocoldataScanCheck;
import org.nrg.xdat.om.base.auto.AutoValProtocoldata;
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
public abstract class BaseValProtocoldata extends AutoValProtocoldata implements ScanAssessorI{

	public BaseValProtocoldata(ItemI item)
	{
		super(item);
	}

	public BaseValProtocoldata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseValProtocolvalidationdata(UserI user)
	 **/
	public BaseValProtocoldata()
	{}

	public BaseValProtocoldata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	public ValProtocoldataScanCheck getScanProtocolValidation(String scanId) throws NullPointerException {
		ValProtocoldataScanCheck rtn = null;
		if (scanId == null) throw new NullPointerException("Expected a non-null value for the scan id input parameter");
		List<ValProtocoldataScanCheckI> scans =  super.getScans_scanCheck();
		if (scans != null && scans.size() > 0) {
			for (int i=0; i< scans.size(); i++) {
				ValProtocoldataScanCheck aScan = (ValProtocoldataScanCheck)scans.get(i);
				if (aScan.getScanId().equals(scanId)) {
					rtn = aScan;
					break;
				}
			}
		}
		if (rtn == null) throw new NullPointerException("Couldnt find scan protocol validation for scan id " + scanId);
		return rtn;
	}
	
	public ScanAssessorScanI getScanById(String id){
		return this.getScanProtocolValidation(id);
	}
	
	public String getHeader(){
		return "Protocol Val";
	}

	public int getPrecedence(){
		return 0;
	}
}
