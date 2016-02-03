/*
 * org.nrg.xnat.helpers.uri.archive.impl.SubjURI
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
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.archive.SubjectURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.List;
import java.util.Map;

public class SubjURI extends ArchiveURI implements ArchiveItemURI,SubjectURII {
	private XnatSubjectdata subj=null;
	
	public SubjURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populate(){
		if(subj==null){
			
			final String subjID= (String)props.get(URIManager.SUBJECT_ID);
			
			if(subj==null){
				subj=XnatSubjectdata.getXnatSubjectdatasById(subjID, null, false);
			}
		}
	}
	
	public XnatSubjectdata getSubject(){
		this.populate();
		return subj;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getSubject();
	}

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatSubjectdata expt=getSubject();
		res.addAll(expt.getResources_resource());
		return res;
	}
}
