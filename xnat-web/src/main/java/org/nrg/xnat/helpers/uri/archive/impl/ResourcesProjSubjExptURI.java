/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:53 AM
 */
package org.nrg.xnat.helpers.uri.archive.impl;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjSubjExptURI extends ResourcesProjSubjURI implements ResourceURII,ArchiveItemURI,ExperimentURII{
	private XnatExperimentdata expt=null;
	
	public ResourcesProjSubjExptURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateExperiment(){
		super.populateSubject();
		
		if(expt==null){
			final XnatProjectdata proj=getProject();
			
			final String exptID= (String)props.get(URIManager.EXPT_ID);
			
			if(proj!=null){
				expt=XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID,null, false);
			}
			
			if(expt==null){
				expt=XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
				if(expt!=null && (proj!=null && !expt.hasProject(proj.getId()))){
					expt=null;
				}
			}
		}
	}
	
	public XnatExperimentdata getExperiment(){
		this.populateExperiment();
		return expt;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getExperiment();
	}

	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(this.getExperiment()!=null){
			for(XnatAbstractresourceI res:this.getExperiment().getResources_resource()){
				if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
					return res;
				}
			}
		}
		
		return null;
	}
}
