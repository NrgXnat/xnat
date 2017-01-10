/*
 * web: org.nrg.xnat.restlet.actions.TriggerPipelines
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import org.apache.log4j.Logger;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.util.concurrent.Callable;

/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 *
 */

public class TriggerPipelines implements Callable<Boolean> {
	private static final String XNAT_TOOLS_AUTO_RUN_XML = "xnat_tools/AutoRun.xml";

	private final XnatExperimentdata expt;
	private final boolean supressEmail;
	private final UserI user;
	private final boolean waitFor;

	public TriggerPipelines(XnatExperimentdata expt,boolean supressEmail,UserI user){
		this(expt,supressEmail,user,false);
	}
	
	public TriggerPipelines(XnatExperimentdata expt,boolean supressEmail,UserI user, boolean waitFor){
		this.expt=expt;
		this.supressEmail=supressEmail;
		this.user=user;
		this.waitFor=waitFor;
	}

	public Boolean call() {
		XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(user);
        xnatPipelineLauncher.setAdmin_email(XDAT.getSiteConfigPreferences().getAdminEmail());
        xnatPipelineLauncher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
        String pipelineName = XNAT_TOOLS_AUTO_RUN_XML;
        xnatPipelineLauncher.setPipelineName(pipelineName);
        xnatPipelineLauncher.setNeedsBuildDir(false);
        xnatPipelineLauncher.setSupressNotification(true);
        xnatPipelineLauncher.setId(expt.getId());
        xnatPipelineLauncher.setLabel(expt.getLabel());
        xnatPipelineLauncher.setDataType(expt.getXSIType());
        xnatPipelineLauncher.setExternalId(expt.getProject());
        xnatPipelineLauncher.setWaitFor(this.waitFor);
        xnatPipelineLauncher.setParameter(XNATRestConstants.SUPRESS_EMAIL, (Boolean.valueOf(supressEmail)).toString());
        xnatPipelineLauncher.setParameter("session", expt.getId());
        xnatPipelineLauncher.setParameter("sessionLabel", expt.getLabel());
        xnatPipelineLauncher.setParameter("useremail", user.getEmail());
        xnatPipelineLauncher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
        xnatPipelineLauncher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());
        xnatPipelineLauncher.setParameter("xnatserver", TurbineUtils.GetSystemName());
        xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
        xnatPipelineLauncher.setParameter("sessionType", expt.getXSIType());
        xnatPipelineLauncher.setParameter("xnat_project", expt.getProject());


        return xnatPipelineLauncher.launch(null);

	}
}
