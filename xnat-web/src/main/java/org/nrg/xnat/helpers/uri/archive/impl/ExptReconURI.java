/*
 * org.nrg.xnat.helpers.uri.archive.impl.ExptReconURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri.archive.impl;

import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class ExptReconURI extends ArchiveURI implements ArchiveItemURI,AssessedURII,ReconURII{
	private XnatReconstructedimagedata recon=null;
	private XnatImagesessiondata session=null; 
	
	public ExptReconURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populate() {
		if(recon==null){
			final String exptID= (String)props.get(URIManager.ASSESSED_ID);
			
			if(session==null){
				session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
			}
			
			final String reconID= (String)props.get(URIManager.RECON_ID);
			
			if(recon==null&& reconID!=null){
				recon=(XnatReconstructedimagedata)XnatReconstructedimagedata.getXnatReconstructedimagedatasById(reconID, null, false);
			}
		}
	}

	public XnatImagesessiondata getSession(){
		this.populate();
		return this.session;
	}

	public XnatReconstructedimagedata getRecon(){
		this.populate();
		return this.recon;
	}

	public ArchivableItem getSecurityItem() {
		return getSession();
	}

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatReconstructedimagedata expt=getRecon();
		res.addAll(expt.getOut_file());
		return res;
	}
}
