/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifyScreeningAssessment
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import groovy.util.logging.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class ModifyScreeningAssessment extends ModifyImageAssessorData {

	@Override
	public boolean allowDataDeletion() {
		// the user should have the ability to "clear out" data on the edit
		// form. Be careful if secondary data is ever attached to the item,
		// as it would be cleared out.
		return true;
	}


}
