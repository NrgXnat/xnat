/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjURI
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
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjURI extends ResourceURIA implements ArchiveItemURI,ResourceURII,ProjectURII{
	private XnatProjectdata project = null;
	
	public ResourcesProjURI(final Map<String, Object> props, final String uri) {
		super(props, uri);
	}

	protected void populateProject(){
		if(project==null){
			project=XnatProjectdata.getProjectByIDorAlias(props.get(URIManager.PROJECT_ID).toString(), null, false);
		}
	}
	
	public XnatProjectdata getProject(){
		this.populateProject();
		return project;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getProject();
	}


	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(this.getProject()!=null){
			for(XnatAbstractresourceI res:this.getProject().getResources_resource()){
				if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
					return res;
				}
			}
		}
		
		return null;
	}
}
