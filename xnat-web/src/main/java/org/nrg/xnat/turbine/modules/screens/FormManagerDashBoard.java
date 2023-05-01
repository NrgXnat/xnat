/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 * 
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;

/**
 * 
 * Prep screen class for the FormManagerDashBoard.vm.
 *
 */
public class FormManagerDashBoard extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		UserI user = XDAT.getUserDetails();
		if (!Roles.isSiteAdmin(user) && !Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE)) {
			data.setMessage("Unauthorized: You do not have sufficient permission to access this page");
			data.setScreenTemplate("Error.vm");
			return;
		}

	}
}
