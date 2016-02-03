/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjURI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:53 AM
 */
package org.nrg.xnat.helpers.uri.archive.impl;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjSubjURI extends ResourcesProjURI implements ResourceURII,ArchiveItemURI,SubjectURII{
	private XnatSubjectdata subject=null;
	
	public ResourcesProjSubjURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateSubject(){
		super.populateProject();
		
		if(subject==null){
			final XnatProjectdata proj=getProject();
			
			final String subID= (String)props.get(URIManager.SUBJECT_ID);
			
			if(proj!=null){
				subject=XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subID,null, false);
			}
			
			if(subject==null){
				subject=XnatSubjectdata.getXnatSubjectdatasById(subID, null, false);
				if(subject!=null && (proj!=null && !subject.hasProject(proj.getId()))){
					subject=null;
				}
			}
		}
	}
	
	public XnatSubjectdata getSubject(){
		this.populateSubject();
		return subject;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getSubject();
	}

	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(this.getSubject()!=null){
			for(XnatAbstractresourceI res:this.getSubject().getResources_resource()){
				if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
					return res;
				}
			}
		}
		
		return null;
	}
}
