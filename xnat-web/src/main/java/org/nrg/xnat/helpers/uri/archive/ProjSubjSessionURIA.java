/*
 * org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri.archive;

import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatReconstructedimagedataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.impl.ProjSubjURI;

import java.util.List;
import java.util.Map;

public abstract class ProjSubjSessionURIA extends ProjSubjURI  implements ArchiveItemURI{
	private XnatImagesessiondata assessed=null;

	public ProjSubjSessionURIA(Map<String, Object> props, String uri) {
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

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatImagesessiondata expt=getSession();
		res.addAll(expt.getResources_resource());
		
		if(includeAll){
			for(XnatImagescandataI scan:expt.getScans_scan()){
				res.addAll(scan.getFile());
			}
			for(XnatReconstructedimagedataI scan:expt.getReconstructions_reconstructedimage()){
				res.addAll(scan.getOut_file());
			}
			for(XnatImageassessordataI scan:expt.getAssessors_assessor()){
				res.addAll(scan.getOut_file());
			}
		}
		
		return res;
	}
}
