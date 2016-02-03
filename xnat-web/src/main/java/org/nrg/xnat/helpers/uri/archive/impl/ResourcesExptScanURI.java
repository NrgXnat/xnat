/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptScanURI
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
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.Map;

public class ResourcesExptScanURI extends ResourceURIA implements ArchiveItemURI,AssessedURII,ResourceURII,ScanURII{
	private XnatImagescandata scan=null;
	private XnatImagesessiondata session=null; 
	
	public ResourcesExptScanURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populate() {
		if(scan==null){
			final String exptID= (String)props.get(URIManager.ASSESSED_ID);
			
			if(session==null){
				session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
			}
			
			final String scanID= (String)props.get(URIManager.SCAN_ID);
			
			if(scan==null&& scanID!=null){
				if(scan==null && session!=null){
					CriteriaCollection cc= new CriteriaCollection("AND");
					cc.addClause("xnat:imageScanData/ID", scanID);
					cc.addClause("xnat:imageScanData/image_session_ID", session.getId());
					ArrayList<XnatImagescandata> scans=XnatImagescandata.getXnatImagescandatasByField(cc, null, false);
					if(scans.size()>0){
						scan=scans.get(0);
					}
				}
			}
		}
	}

	public XnatImagesessiondata getSession(){
		this.populate();
		return this.session;
	}

	public XnatImagescandata getScan(){
		this.populate();
		return this.scan;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getSession();
	}


	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(this.getScan()!=null){
			for(XnatAbstractresourceI res:this.getScan().getFile()){
				if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
					return res;
				}
			}
		}
		
		return null;
	}

	@Override
	public XnatProjectdata getProject() {
		return this.getSession().getProjectData();
	}
}
