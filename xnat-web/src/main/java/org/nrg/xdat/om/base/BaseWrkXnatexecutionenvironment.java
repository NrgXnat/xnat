/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseWrkXnatexecutionenvironment
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.pipeline.PipelineLaunchParameters;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI;
import org.nrg.xdat.om.base.auto.AutoWrkXnatexecutionenvironment;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseWrkXnatexecutionenvironment extends AutoWrkXnatexecutionenvironment {

	public BaseWrkXnatexecutionenvironment(ItemI item)
	{
		super(item);
	}

	public BaseWrkXnatexecutionenvironment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseWrkXnatexecutionenvironment(UserI user)
	 **/
	public BaseWrkXnatexecutionenvironment()
	{}

	public BaseWrkXnatexecutionenvironment(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public XnatPipelineLauncher getLauncher(final UserI user) {
        return new XnatPipelineLauncher(getLaunchParameters(user));
    }

    public XnatPipelineLauncher getLauncher(final UserI user, final String startAt) {
        PipelineLaunchParameters pipelineLaunchParameters = getLaunchParameters(user);
        pipelineLaunchParameters.setStartAt(startAt);
        return new XnatPipelineLauncher(getLaunchParameters(user));
    }

    private PipelineLaunchParameters getLaunchParameters(final UserI user) {
        PipelineLaunchParameters pipelineLaunchParameters = PipelineLaunchParameters.builder()
                .user(user)
                .build();
        pipelineLaunchParameters.setPipelineName(getPipeline());
        pipelineLaunchParameters.setStartAt(getStartat());
        pipelineLaunchParameters.setDataType(getDatatype());
        pipelineLaunchParameters.setId(getId());
        pipelineLaunchParameters.setSupressNotification(getSupressnotification());

        List parameters = getParameters_parameter();
        for (int i = 0; i < parameters.size(); i++) {
            WrkXnatexecutionenvironmentParameterI aParameter = (WrkXnatexecutionenvironmentParameterI)parameters.get(i);
            pipelineLaunchParameters.setParameter(aParameter.getName(), aParameter.getParameter());
        }
        if (this.getParameterfile_path() != null) {
            pipelineLaunchParameters.setParameterFile(getParameterfile_path());
        }
        List notified = getNotify();
        for (int i = 0; i < notified.size(); i++) {
            String notifiedEmailId = (String)notified.get(i);
            if (!notifiedEmailId.equals(user.getEmail()) && !notifiedEmailId.equals(XDAT.getSiteConfigPreferences().getAdminEmail())) {
                pipelineLaunchParameters.notificationEmailId(notifiedEmailId);
            }
        }
        return pipelineLaunchParameters;
    }
}
