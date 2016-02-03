/*
 * org.nrg.xdat.om.base.BaseXnatPetmrsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.base.auto.AutoXnatPetmrsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatPetmrsessiondata extends AutoXnatPetmrsessiondata {

	public BaseXnatPetmrsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatPetmrsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatPetmrsessiondata(UserI user)
	 **/
	public BaseXnatPetmrsessiondata()
	{}

	public BaseXnatPetmrsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public ArrayList getUnionOfScansByType(String csvType) {
		ArrayList _return = new ArrayList();
		String[] types = csvType.split(",");
		if (types != null && types.length > 0) {
			for(int i = 0; i < types.length; i++) {
				ArrayList rtn = getScansByType(types[i].trim());
				if (rtn.size() > 0 )_return.addAll(rtn);
			}
		}
		_return.trimToSize();
		return _return;
	}

	public ArrayList getUnionOfScansByType(String csvType, boolean chronological) {
		ArrayList _return = new ArrayList();
		if (chronological) {
			String[] types = csvType.split(",");
			Hashtable scanTypes = new Hashtable();
			if (types != null && types.length > 0) {
				for(int i = 0; i < types.length; i++) {
					scanTypes.put(types[i].trim(), "");
				}
			}
			for(XnatImagescandataI scan :  this.getScans_scan()){
				if (scan.getType() != null && scanTypes.containsKey(scan.getType())) {
					_return.add(scan);
				}
			}
			_return.trimToSize();
			return _return;
		}else
			return getUnionOfScansByType(csvType);
	}

	public ArrayList getUnionOfScansByType(String csvType, String chronological) {
		return getUnionOfScansByType(csvType, new Boolean(chronological).booleanValue());
	}

}
