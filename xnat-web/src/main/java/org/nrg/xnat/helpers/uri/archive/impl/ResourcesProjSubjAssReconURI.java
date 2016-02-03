/*
 * org.nrg.xnat.helpers.uri.archive.impl.ResourcesProjSubjAssReconURI
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
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveItemURI;
import org.nrg.xnat.helpers.uri.archive.AssessedURII;
import org.nrg.xnat.helpers.uri.archive.ReconURII;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.ResourcesProjSubjSessionURIA;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import java.util.Map;

public class ResourcesProjSubjAssReconURI extends ResourcesProjSubjSessionURIA  implements AssessedURII,ResourceURII,ArchiveItemURI,ReconURII{
	private XnatReconstructedimagedata recon=null;
	
	public ResourcesProjSubjAssReconURI(Map<String, Object> props, String uri) {
		super(props, uri);
	}

	protected void populateRecon() {
		super.populateSession();

		if(recon==null){
			final String reconID= (String)props.get(URIManager.RECON_ID);
			
			if(recon==null&& reconID!=null){
				recon=(XnatReconstructedimagedata)XnatReconstructedimagedata.getXnatReconstructedimagedatasById(reconID, null, false);
			}
		}
	}

	public XnatReconstructedimagedata getRecon(){
		this.populateRecon();
		return this.recon;
	}

	@Override
	public ArchivableItem getSecurityItem() {
		return getSession();
	}

	@Override
	public XnatAbstractresourceI getXnatResource() {
		if(this.getRecon()!=null){
			String type=(String)this.props.get(URIManager.TYPE);
			
			if(type==null){
				type="out";
			}
			
			if(type.equals("out")){
				for(XnatAbstractresourceI res:this.getRecon().getOut_file()){
					if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
						return res;
					}
				}
			}else if(type.equals("in")){
				for(XnatAbstractresourceI res:this.getRecon().getIn_file()){
					if(StringUtils.equals(res.getLabel(), this.getResourceLabel())){
						return res;
					}
				}
			}
		}
		
		return null;
	}
}