//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Feb 21, 2005
 *
 */
package org.nrg.xdat.turbine.modules.navigations;
import org.apache.turbine.modules.navigations.VelocityNavigation;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xft.XFT;
/**
 * @author Tim
 *
 */
public class DefaultTop extends VelocityNavigation {
	protected void doBuildTemplate(RunData data,Context context)throws Exception
	{
		if (XFT.GetRequireLogin())
		{
			context.put("logout","true");
		}
	}
}

