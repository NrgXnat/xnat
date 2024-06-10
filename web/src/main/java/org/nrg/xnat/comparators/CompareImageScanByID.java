/*
 * web: org.nrg.xnat.comparators.CompareImageScanByID
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.comparators;

import org.nrg.xdat.model.XnatImagescandataI;

import java.util.Comparator;


public class CompareImageScanByID implements Comparator<XnatImagescandataI>{

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
        
        return value1.getId().compareTo(value2.getId());
	}
}
