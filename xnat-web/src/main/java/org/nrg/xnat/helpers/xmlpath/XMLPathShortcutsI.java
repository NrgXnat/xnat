/*
 * org.nrg.xnat.helpers.xmlpath.XMLPathShortcutsI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.xmlpath;

import java.util.Map;

public interface XMLPathShortcutsI {
	public Map<String,Object> identifyFields(final Map<String,Object> params, final String TYPE,boolean readOnly);
	public Map<String,String> getShortcuts(final String type, final boolean readOnly);
}
