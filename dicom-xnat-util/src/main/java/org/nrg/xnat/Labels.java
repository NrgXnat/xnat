/*
 * dicom-xnat-util: org.nrg.xnat.Labels
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat;

import java.util.regex.Pattern;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class Labels {
	private Labels() {}
	
	private final static Pattern labelPattern = Pattern.compile("[\\w-]+");
    
    public static boolean isValidLabel(final CharSequence in) {
    	return null != in && labelPattern.matcher(in).matches();
    }
    
    private final static Pattern nonLabelCharsPattern = Pattern.compile("[^\\w-]");

    public static String toLabelChars(final CharSequence in) {
        return null == in ? null : nonLabelCharsPattern.matcher(in).replaceAll("_");
    }
}
