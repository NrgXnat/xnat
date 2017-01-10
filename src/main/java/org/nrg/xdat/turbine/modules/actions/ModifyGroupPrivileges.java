/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyGroupPrivileges
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
