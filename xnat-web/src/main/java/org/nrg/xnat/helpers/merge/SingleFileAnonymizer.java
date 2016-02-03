/*
 * org.nrg.xnat.helpers.merge.SingleFileAnonymizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:33 PM
 */
package org.nrg.xnat.helpers.merge;

import org.nrg.config.entities.Configuration;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xnat.helpers.editscript.DicomEdit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
		return AnonUtils.getService().getScript(this.path, BaseXnatProjectdata.getProjectInfoIdFromStringId(anonProject));
	}

	@Override
	boolean isEnabled() {
		return AnonUtils.getService().isEnabled(this.path, BaseXnatProjectdata.getProjectInfoIdFromStringId(anonProject));
	}

	@Override
	String getProjectName() {
		return this.anonProject;
	}

	@Override
	List<File> getFilesToAnonymize() {
		List<File> ret = new ArrayList<File>(1);
		ret.add(this.f);
		return ret;
	}
	
	public boolean alreadyAnonymized() {
		return false;
	}
	
	@Override
	public java.lang.Void call () throws Exception {
		if (this.reanonymize) {
			super.call();
		}
		else {
			// do nothing
		}
		return null;
	}
}
