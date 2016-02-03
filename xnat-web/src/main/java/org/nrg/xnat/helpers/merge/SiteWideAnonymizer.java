/*
 * org.nrg.xnat.helpers.merge.SiteWideAnonymizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.merge;

import org.nrg.config.entities.Configuration;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.editscript.DicomEdit.ResourceScope;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SiteWideAnonymizer extends AnonymizerA {
	final boolean located_in_prearchive;
	final XnatImagesessiondataI s;
	final String path;
	public SiteWideAnonymizer(XnatImagesessiondataI s, boolean located_in_prearchive) {
		this.s = s;
		this.located_in_prearchive = located_in_prearchive;
		this.path = DicomEdit.buildScriptPath(ResourceScope.SITE_WIDE, null);
	}
	public SiteWideAnonymizer(XnatImagesessiondataI s){
		this(s,false);
	}
	
	@Override
	String getProjectName() {
		return null;
	}
	
	/**
	 * Returns the subject string that will be passed into the 
	 * Anonymize.anonymize function
	 * @return The subject label or subject id (if label is null)
	 */
	@Override
	String getSubject() {
		String label = null;
		if(s instanceof XnatImagesessiondata){
			XnatSubjectdata d = ((XnatImagesessiondata)this.s).getSubjectData();
			if ( d != null){
				label = d.getLabel();
			}
		}
		
		// If the label is null, return the SubjectId
		return (label != null) ? label : this.s.getSubjectId();
	}
	
	@Override
	String getLabel() {
		return this.s.getLabel();
	}
	
	
	Configuration getScript() {
		return AnonUtils.getService().getScript(this.path, null);
	}
	
	public List<File> getFilesToAnonymize() throws IOException {
		List<File> fs = new ArrayList<File>();
		if (located_in_prearchive) {
			PrearcSessionAnonymizer p = new PrearcSessionAnonymizer(s, s.getProject(), s.getPrearchivepath());
			fs.addAll(p.getFilesToAnonymize());
		}
		else {
			ProjectAnonymizer p = new ProjectAnonymizer(this.s, this.getProjectName(), s.getPrearchivepath());
			fs.addAll(p.getFilesToAnonymize());
		}
		return fs;
	}
	
	boolean isEnabled() {
		return AnonUtils.getService().isEnabled(this.path, null);
	}
	public java.lang.Void call() throws Exception {
		super.call();
		return null;
	}
}

