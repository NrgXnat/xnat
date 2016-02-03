/*
 * org.nrg.xdat.om.base.BaseWrkXnatexecutionenvironment
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import java.util.Hashtable;
import java.util.List;

import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI;
import org.nrg.xdat.om.base.auto.AutoWrkXnatexecutionenvironment;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

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

    public XnatPipelineLauncher getLauncher(UserI user) {
        XnatPipelineLauncher xnatLauncher = new XnatPipelineLauncher((UserI)user);
        xnatLauncher.setPipelineName(getPipeline());
        xnatLauncher.setStartAt(getStartat());
        List parameters = getParameters_parameter();
        for (int i = 0; i < parameters.size(); i++) {
            WrkXnatexecutionenvironmentParameterI aParameter = (WrkXnatexecutionenvironmentParameterI)parameters.get(i);
            xnatLauncher.setParameter(aParameter.getName(), aParameter.getParameter());
        }
        List notified = getNotify();
        for (int i = 0; i < notified.size(); i++) {
            String notifiedEmailId = (String)notified.get(i);
            if (!notifiedEmailId.equals(user.getEmail()) && !notifiedEmailId.equals(AdminUtils.getAdminEmailId())) {
                xnatLauncher.notify(notifiedEmailId);
            }
        }
        xnatLauncher.setDataType(getDatatype());
        xnatLauncher.setId(getId());
        xnatLauncher.setSupressNotification(getSupressnotification());
        if (this.getParameterfile_path() != null) {
            xnatLauncher.setParameterFile(getParameterfile_path());
        }
        return xnatLauncher;
    }
}
