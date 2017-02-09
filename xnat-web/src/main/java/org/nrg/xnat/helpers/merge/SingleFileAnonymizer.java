/*
 * web: org.nrg.xnat.helpers.merge.SingleFileAnonymizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.nrg.config.entities.Configuration;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SingleFileAnonymizer extends AnonymizerA {
	public final File f;
	public final String project;
	public final String subject;
	public final String label;
	public final String anonProject;
	private final boolean reanonymize;
	final String path;
	
	public SingleFileAnonymizer(File f, String project, String subject, String label, String anonProject, boolean reanonymize) {
		this.f = f;
		this.project = project;
		this.subject = subject;
		this.label = label;
		this.anonProject = anonProject;
		this.reanonymize = reanonymize;
		if (anonProject != null) {
			this.path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.PROJECT, anonProject);	
		}
		else {
			this.path = DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null);	
		}
		
	}
	
	@Override
	String getSubject() {
		return this.subject;
	}

	@Override
	String getLabel() {
		return this.label;
	}

	@Override
	Configuration getScript() {
		return DefaultAnonUtils.getService().getProjectScriptConfiguration(anonProject);
	}

	@Override
	boolean isEnabled() {
		return DefaultAnonUtils.getService().isProjectScriptEnabled(anonProject);
	}

	@Override
	String getProjectName() {
		return this.anonProject;
	}

	@Override
	List<File> getFilesToAnonymize() {
		List<File> ret = new ArrayList<>(1);
		ret.add(this.f);
		return ret;
	}

	@Override
	public Boolean call () throws Exception {
		return reanonymize ? super.call() : false;
	}
}
