/*
 * org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri.archive;

import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjURI;

import java.util.Map;

public abstract class ResourcesProjSubjSessionURIA extends ResourcesProjSubjURI implements ResourceURII{
	private XnatImagesessiondata assessed=null;

	public ResourcesProjSubjSessionURIA(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateSession(){
		super.populateSubject();
		
		if(assessed==null){
			final XnatProjectdata proj=getProject();
			
			final String exptID= (String)props.get(URIManager.ASSESSED_ID);
			
			if(proj!=null){
				assessed=(XnatImagesessiondata)XnatImagesessiondata.GetExptByProjectIdentifier(proj.getId(), exptID,null, false);
			}
			
			if(assessed==null){
				assessed=(XnatImagesessiondata)XnatImagesessiondata.getXnatExperimentdatasById(exptID, null, false);
				if(assessed!=null && (proj!=null && !assessed.hasProject(proj.getId()))){
					assessed=null;
				}
			}
		}
	}
	
	public XnatImagesessiondata getSession(){
		populateSession();
		return assessed;
	}
}
