/*
 * org.nrg.xnat.comparators.CompareImageScanBySeriesUID
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.comparators;

import org.nrg.xdat.model.XnatImagescandataI;

import java.util.Comparator;

public class CompareImageScanBySeriesUID implements Comparator<XnatImagescandataI> {

	@Override
	public int compare(XnatImagescandataI value1, XnatImagescandataI value2) {
		if (value1 == null){
            if (value2 == null)
            {
                return 0;
            }else{
                return -1;
            }
        }
        if (value2== null)
        {
        	return 1;
	    }
        
        return value1.getUid().compareTo(value2.getUid());
	}


}
