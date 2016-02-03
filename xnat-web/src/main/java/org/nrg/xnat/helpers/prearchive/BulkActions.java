/*
 * org.nrg.xnat.helpers.prearchive.BulkActions
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import java.util.ArrayList;
import java.util.List;

public final class BulkActions {
	static List<Object> scheduledActions = java.util.Collections.synchronizedList(new ArrayList<Object>());
	
	// prevent instantiation
	private BulkActions() {} 
}
