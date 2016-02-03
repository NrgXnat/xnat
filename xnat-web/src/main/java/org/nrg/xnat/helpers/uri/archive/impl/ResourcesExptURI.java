/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri.archive.impl;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesExptURI extends ResourceURIA implements ArchiveItemURI,ResourceURII,ExperimentURII{
	private XnatExperimentdata expt=null;
	
	public ResourcesExptURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populate(){
		if(expt==null){
			
			final String exptID= (String)props.get(URIManager.EXPT_ID);
			
			if(expt==null){
				expt=XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
			}
		}
	}
	
	public XnatExperimentdata getExperiment(){
		this.populate();
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

	@Override
	public XnatProjectdata getProject() {
		return this.getExperiment().getProjectData();
	}
}
