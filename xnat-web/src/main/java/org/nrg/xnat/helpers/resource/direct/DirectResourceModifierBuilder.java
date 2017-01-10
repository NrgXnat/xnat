/*
 * web: org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.resource.direct;

import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;

public class DirectResourceModifierBuilder implements ResourceModifierBuilderI {
	private XnatReconstructedimagedata recon;
	private XnatImagescandata scan;
	private XnatImageassessordata assess;
	private XnatImagesessiondata assessed;
	private XnatExperimentdata expt;
	private XnatSubjectdata subject;
	private XnatProjectdata project;
	
	private String type;
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public XnatReconstructedimagedata getRecon() {
		return recon;
	}

	@Override
	public void setRecon(XnatImagesessiondata assessed, XnatReconstructedimagedata recon, String type) {
		this.type = type;
		this.assessed=assessed;
		this.recon = recon;
	}

	@Override
	public XnatImagescandata getScan() {
		return scan;
	}

	@Override
	public void setScan(XnatImagesessiondata assessed, XnatImagescandata scan) {
		this.assessed=assessed;
		this.scan = scan;
	}

	@Override
	public XnatImageassessordata getAssess() {
		return assess;
	}

	@Override
	public void setAssess(XnatImagesessiondata assessed, XnatImageassessordata assess, String type) {
		this.type = type;
		this.assessed=assessed;
		this.assess = assess;
	}

	@Override
	public XnatExperimentdata getExpt() {
		return expt;
	}

	@Override
	public void setExpt(XnatProjectdata project,XnatExperimentdata expt) {
		this.project = project;
		this.expt = expt;
	}

	@Override
	public XnatSubjectdata getSubject() {
		return subject;
	}

	@Override
	public void setSubject(XnatProjectdata project,XnatSubjectdata subject) {
		this.project = project;
		this.subject = subject;
	}

	@Override
	public XnatProjectdata getProject() {
		return project;
	}

	@Override
	public void setProject(XnatProjectdata project) {
		this.project = project;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.DirectResourceBuilderI#buildResourceModifier()
	 */
	@Override
	public ResourceModifierA buildResourceModifier(final boolean overwrite, final UserI user,EventMetaI ci) throws Exception{        
		if(recon!=null){
			//reconstruction			
			if(assessed==null){
				throw new Exception("Invalid session id");
			}
			
			return new DirectReconResourceImpl(recon, assessed, type,overwrite,user,ci);
		}else if(scan!=null){
			//scan
			if(assessed==null){
				throw new Exception("Invalid session id");
			}
			
			return new DirectScanResourceImpl(scan, assessed,overwrite,user,ci);
		}else if(assess!=null){
			if(assessed==null){
				throw new Exception("Invalid session id");
			}
		
			return new DirectAssessResourceImpl((XnatImageassessordata)assess,(XnatImagesessiondata)assessed,type,overwrite,user,ci);
		}else if(expt!=null){
			return new DirectExptResourceImpl(project, expt,overwrite,user,ci);
		}else if(subject!=null){
			return new DirectSubjResourceImpl(project, subject,overwrite,user,ci);
		}else if(project!=null){
			return new DirectProjResourceImpl(project,overwrite,user,ci);
		}else{
			throw new Exception("Unknown resource");
		}
	}
}
