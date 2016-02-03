/*
 * org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssScanURI
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
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ProjSubjSessionURIA;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjSubjAssScanURI extends ProjSubjSessionURIA  implements ArchiveItemURI,AssessedURII,ScanURII{
	private XnatImagescandata scan=null;
	
	public ProjSubjAssScanURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateScan() {
		super.populateSession();

		if(scan==null){
			final String scanID= (String)props.get(URIManager.SCAN_ID);
			final XnatImagesessiondata session=this.getSession();
			
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

	public XnatImagescandata getScan(){
		this.populateScan();
		return this.scan;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getSession();
	}

	@Override
	public List<XnatAbstractresourceI> getResources(boolean includeAll) {
		List<XnatAbstractresourceI> res=Lists.newArrayList();
		final XnatImagescandata expt=getScan();
		res.addAll(expt.getFile());
		return res;
	}
}
