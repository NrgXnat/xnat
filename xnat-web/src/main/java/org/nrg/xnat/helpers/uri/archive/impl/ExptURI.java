/*
 * org.nrg.xnat.helpers.uri.archive.impl.ExptURI
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
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatReconstructedimagedataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.ExperimentURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class ExptURI extends ArchiveURI implements ArchiveItemURI,ExperimentURII {
	private XnatExperimentdata expt=null;
	
	public ExptURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateExperiment(){
		if(expt==null){
			
			final String exptID= (String)props.get(URIManager.EXPT_ID);
			
			if(expt==null){
				expt=XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
			}
		}
	}
	
	public XnatExperimentdata getExperiment(){
		this.populateExperiment();
		return expt;
	}

	public ArchivableItem getSecurityItem() {
		return getExperiment();
	}

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatExperimentdata expt=getExperiment();
		res.addAll(expt.getResources_resource());
		
		if(expt instanceof XnatImagesessiondata && includeAll){
			for(XnatImagescandataI scan:((XnatImagesessiondata)expt).getScans_scan()){
				res.addAll(scan.getFile());
			}
			for(XnatReconstructedimagedataI scan:((XnatImagesessiondata)expt).getReconstructions_reconstructedimage()){
				res.addAll(scan.getOut_file());
			}
			for(XnatImageassessordataI scan:((XnatImagesessiondata)expt).getAssessors_assessor()){
				res.addAll(scan.getOut_file());
			}
		}
		
		return res;
	}
}
