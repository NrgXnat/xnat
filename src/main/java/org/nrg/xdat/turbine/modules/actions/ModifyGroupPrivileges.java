/*
 * org.nrg.xdat.turbine.modules.actions.ModifyGroupPrivileges
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
public class ModifyGroupPrivileges extends AdminAction {
	static Logger logger = Logger.getLogger(ModifyGroupPrivileges.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		data.setMessage("Not currently supported");
	}
}
