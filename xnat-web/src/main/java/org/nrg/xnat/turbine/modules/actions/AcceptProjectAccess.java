/*
 * web: org.nrg.xnat.turbine.modules.actions.AcceptProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.util.List;

public class AcceptProjectAccess extends SecureAction {

	@Override
	public void doPerform(RunData data, Context context) throws Exception {
		UserI user = (UserI) context.get("user"); //Check the user in the context before XDAT.getUserDetails() because XDAT.getUserDetails() may still be the guest user at this point.

		if (user == null) {
			user = XDAT.getUserDetails();
        }
		if (user.getUsername().equals("guest")) {
			data.getParameters().add("nextPage", data.getTemplateInfo().getScreenTemplate());
			if (!StringUtils.isBlank(data.getAction())) {
				data.getParameters().add("nextAction", data.getAction());
            } else {
				data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
            }

			data.setScreenTemplate(Turbine.getConfiguration().getString("template.login"));
			if (logger.isDebugEnabled()) {
                logger.debug("Re-route to login:" + Turbine.getConfiguration().getString("template.login"));
            }

			return;
		}

        String parId = (String) TurbineUtils.GetPassedParameter("par", data);
        String hash = (String) TurbineUtils.GetPassedParameter("hash", data);
        ProjectAccessRequest par = ProjectAccessRequest.RequestPARByGUID(parId, user);
		if (par.getApproved() != null || par.getApprovalDate() != null) {
			data.setMessage("Project Invitation already accepted by a different user.  Please request access to the project directly.");
			data.setScreenTemplate("Index.vm");
            logger.debug("PAR not approved or already accepted: " + par.getGuid());
		} else if (StringUtils.isBlank(hash) || !hash.equals(par.getHashedEmail())) {
			data.setMessage("The link for this project access request is insecure or invalid. Please contact the system administrator to verify your email or project membership.");
			data.setScreenTemplate("Index.vm");
            logger.warn("PAR rejected as insecure, failed to match verification hash: " + par.getGuid() + " from email " + par.getEmail());
		} else {
            List<String> processedProjects = par.process(user, true, getEventType(data), getReason(data), getComment(data));
            if (processedProjects.size() > 0) {
                context.put("accepted_pars", processedProjects);
            }
			redirectToReportScreen(XnatProjectdata.getProjectByIDorAlias(par.getProjectId(), user, false), data);
		}
	}

    private static final Logger logger = Logger.getLogger(AcceptProjectAccess.class);
}
