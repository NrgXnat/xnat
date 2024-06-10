/*
 * web: org.nrg.xnat.helpers.resource.direct.DirectAssessResourceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.resource.direct;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;

/**
 * @author timo
 *
 */
public class DirectAssessResourceImpl extends ResourceModifierA {
	private final XnatImageassessordata expt;
	private final XnatImagesessiondata session;
	private final String type;
	
	public DirectAssessResourceImpl(final XnatImageassessordata expt, final XnatImagesessiondata session, final String type,final boolean overwrite, final UserI user, final EventMetaI ci){
		super(overwrite,user,ci);
		this.expt=expt;
		this.session=session;
		this.type=type;
		
		if(session==null){
			throw new NullPointerException("Must reference a valid imaging session");
		}
	}
	
	public XnatProjectdata getProject(){
		return expt.getProjectData();
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.DirectResourceModifierA#buildDestinationPath()
	 */
	@Override
	public String buildDestinationPath() throws InvalidArchiveStructure, UnknownPrimaryProjectException {
		String path=FileUtils.AppendRootPath(((XnatImagesessiondata)session).getCurrentSessionFolder(true), "ASSESSORS/" + expt.getId() +"/");
		
		if(type!=null){
			path=FileUtils.AppendRootPath(path,type);
		}
		
		return path;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.DirectResourceModifierA#addResource(org.nrg.xdat.om.XnatResource, org.nrg.xdat.security.UserI)
	 */
	@Override
	public boolean addResource(XnatResource resource, final String type, UserI user) throws Exception {
		XnatImageassessordata iad = (XnatImageassessordata)expt.getLightCopy();
		if(type!=null){
			if(type.equals("in")){
				iad.setIn_file(resource);
			}else{
				iad.setOut_file(resource);
			}
		}else{
			iad.setOut_file(resource);
		}
		
		SaveItemHelper.authorizedSave(iad,user, false, false,ci);
		return true;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.ResourceModifierA#getResourceById(java.lang.Integer)
	 */
	@Override
	public XnatAbstractresourceI getResourceById(Integer i, final String type) {
		for(XnatAbstractresourceI res: this.expt.getResources_resource()){
			if(res.getXnatAbstractresourceId().equals(i)){
				return res;
			}
		}
		for(XnatAbstractresourceI res: this.expt.getIn_file()){
			if(res.getXnatAbstractresourceId().equals(i)){
				return res;
			}
		}
		for(XnatAbstractresourceI res: this.expt.getOut_file()){
			if(res.getXnatAbstractresourceId().equals(i)){
				return res;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.resource.direct.ResourceModifierA#getResourceByLabel(java.lang.String)
	 */
	@Override
	public XnatAbstractresourceI getResourceByLabel(String lbl, final String type) {
		for(XnatAbstractresourceI res: this.expt.getResources_resource()){
			if(StringUtils.isNotEmpty(res.getLabel()) && res.getLabel().equals(lbl)){
				return res;
			}
		}
		for(XnatAbstractresourceI res: this.expt.getIn_file()){
			if(StringUtils.isNotEmpty(res.getLabel()) && res.getLabel().equals(lbl)){
				return res;
			}
		}
		for(XnatAbstractresourceI res: this.expt.getOut_file()){
			if(StringUtils.isNotEmpty(res.getLabel()) && res.getLabel().equals(lbl)){
				return res;
			}
		}
		
		return null;
	}

}
