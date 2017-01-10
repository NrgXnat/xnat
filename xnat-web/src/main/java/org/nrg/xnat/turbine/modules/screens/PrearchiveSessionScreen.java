/*
 * web: org.nrg.xnat.turbine.modules.screens.PrearchiveSessionScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.util.Date;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

public abstract class PrearchiveSessionScreen extends SecureScreen {

	public PrearchiveSessionScreen() {
		super();
	}

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		final String folder = (String)TurbineUtils.GetPassedParameter("folder",data);
	    final String timestamp = (String)TurbineUtils.GetPassedParameter("timestamp",data);
	    final String project = (String)TurbineUtils.GetPassedParameter("project",data);	
	    final UserI user = TurbineUtils.getUser(data);
	    
	    final File sessionDir=PrearcUtils.getPrearcSessionDir(user, project, timestamp, folder,false);
	    
	    final File sessionXML = new File(sessionDir.getPath() + ".xml");
		final XnatImagesessiondataBean sessionBean;
		try {
			sessionBean = PrearcTableBuilder.parseSession(sessionXML);
		} catch (Exception e) {
			error(e, data);
			return;
		}
		
		Date upload=PrearcUtils.parseTimestampDirectory(timestamp);

		context.put("uploadDate",upload);
		context.put("timestamp",timestamp);
		context.put("folder",folder);
		context.put("status", PrearcDatabase.getSession(folder, timestamp, project).getStatus().toString());
		context.put("session",sessionBean);
		context.put("url", String.format("/prearchive/projects/%s/%s/%s",(project==null)?"Unassigned":project,timestamp,folder));
		
		finalProcessing(sessionBean, data,context);
	}

	public abstract void finalProcessing(XnatImagesessiondataBean session, RunData data, Context context) throws Exception;
}
