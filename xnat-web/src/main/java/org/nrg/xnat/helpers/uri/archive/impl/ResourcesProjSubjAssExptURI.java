/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjAssExptURI
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
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjSubjAssExptURI extends ResourcesProjSubjSessionURIA  implements AssessedURII,ResourceURII,ArchiveItemURI,AssessorURII{
	private XnatImageassessordata expt=null;
	
	public ResourcesProjSubjAssExptURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}
	
	protected void populateAssmt(){
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
		this.populateAssmt();
		return expt;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getAssessor();
	}

	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(getAssessor()!=null){
			String type=(String)this.props.get(URIManager.TYPE);
			
			if(type==null){
				type="out";
			}
			
			if(type.equals("out")){
				for(XnatAbstractresourceI res:this.getAssessor().getOut_file()){
					if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
						return res;
					}
				}
			}else if(type.equals("in")){
				for(XnatAbstractresourceI res:this.getAssessor().getIn_file()){
					if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
						return res;
					}
				}
			}
		}
		
		return null;
	}
}