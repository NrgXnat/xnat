/*
 * web: org.nrg.xnat.turbine.modules.screens.AdminSummary
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Set;

public class ConfigureSearchFields extends AdminScreen {
	UserI u;
	@Override
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {

	}
}
