
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class TriageUtils {

	final private static String TRIAGEPATH = "triagePath";

	public static String getTriageUploadsPath() {
		String triage = "";
		try {
			triage = XDAT.getSiteConfigurationProperty(TRIAGEPATH);
		} catch (ConfigServiceException sex) {
			triage = ArcSpecManager.GetInstance().getGlobalCachePath()+ "TRIAGE";
		}
		if(StringUtils.isBlank(triage)){
			//prevents nullpointerexception when variable isn't set.
			triage = ArcSpecManager.GetInstance().getGlobalCachePath()+ "TRIAGE";
		}
		if (triage.endsWith(File.separator)) {
			return triage;
		} else {
			return triage + File.separator;
		}

	}

	public static String getTriageProjectPath(String project) {

		return getTriageUploadsPath() + "projects" + File.separator + project;

	}

	public static File getTriageFile(final String directory, final String file) {
		return new File(new File(getTriageUploadsPath(), directory), file);
	}

	public static File getTriageFile(final String directory) {
		return new File(getTriageUploadsPath(), directory);
	}
}