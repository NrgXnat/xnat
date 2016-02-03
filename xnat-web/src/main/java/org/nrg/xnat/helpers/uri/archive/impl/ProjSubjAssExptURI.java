/*
 * org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssExptURI
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
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class ProjSubjAssExptURI extends ProjSubjSessionURIA  implements ArchiveItemURI,AssessedURII,AssessorURII{
	private XnatImageassessordata expt=null;
	
	public ProjSubjAssExptURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}
	
	protected void populateExperiment(){
		super.populateSession();
		
		if(expt==null){
			final XnatProjectdata proj=getProject();
			
			final String exptID= (String)props.get(URIManager.EXPT_ID);
			
			if(proj!=null){
				expt=(XnatImageassessordata)XnatExperimentdata.GetExptByProjectIdentifier(proj.getId(), exptID,null, false);
			}
			
			if(expt==null){
				expt=(XnatImageassessordata)XnatExperimentdata.getXnatExperimentdatasById(exptID, null, false);
				if(expt!=null && (proj!=null && !expt.hasProject(proj.getId()))){
					expt=null;
				}
			}
		}
	}
	
	public XnatImageassessordata getAssessor(){
		this.populateExperiment();
		return expt;
	}

	@Override
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