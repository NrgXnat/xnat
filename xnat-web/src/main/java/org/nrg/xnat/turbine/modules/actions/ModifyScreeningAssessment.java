/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifyScreeningAssessment
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.nrg.xdat.turbine.modules.actions.ModifyItem;

public class ModifyScreeningAssessment extends ModifyItem {

	@Override
	public boolean allowDataDeletion() {
		// the user should have the ability to "clear out" data on the edit
		// form. Be careful if secondary data is ever attached to the item,
		// as it would be cleared out.
		return true;
	}

}
