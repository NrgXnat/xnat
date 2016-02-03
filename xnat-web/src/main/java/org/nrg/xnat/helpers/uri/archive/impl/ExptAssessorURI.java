/*
 * org.nrg.xnat.helpers.uri.archive.impl.ExptAssessorURI
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
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class ExptAssessorURI extends ArchiveURI implements ArchiveItemURI,AssessedURII,AssessorURII{
	private XnatImageassessordata expt=null;
	private XnatImagesessiondata session=null; 
	
	public ExptAssessorURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}
	
	protected void populate(){		
		if(expt==null){
			final String sessID= (String)props.get(URIManager.ASSESSED_ID);
			
			if(session==null){
				session=(XnatImagesessiondata)XnatExperimentdata.getXnatExperimentdatasById(sessID, null, false);
			}
			
			final String exptID= (String)props.get(URIManager.EXPT_ID);
			
			if(expt==null){
				expt=(XnatImageassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
			}
		}
	}

	public XnatImagesessiondata getSession(){
		this.populate();
		return this.session;
	}
	
	public XnatImageassessordata getAssessor(){
		this.populate();
		return expt;
	}

	public ArchivableItem getSecurityItem() {
		return getAssessor();
	}

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatExperimentdata expt=getAssessor();
		res.addAll(expt.getResources_resource());
		res.addAll(((XnatImageassessordata)expt).getOut_file());
		return res;
	}
}
