/*
 * org.nrg.xnat.helpers.merge.SessionOverwriteCheck
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.merge;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.*;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.CatalogUtils;

import java.util.List;
import java.util.concurrent.Callable;

public class SessionOverwriteCheck implements Callable<Boolean> {
	final XnatImagesessiondataI src,dest;
	final String srcRootPath,destRootPath;
	final UserI u;
	final EventMetaI c;
	
	public SessionOverwriteCheck(XnatImagesessiondataI src, XnatImagesessiondataI dest,String srcRootPath,String destRootPath, UserI user, EventMetaI now){
		this.src=src;
		this.dest=dest;
		this.srcRootPath=srcRootPath;
		this.destRootPath=destRootPath;
		this.u=user;
		this.c=now;
	}
	
	@Override
	public Boolean call(){
		final List<XnatImagescandataI> srcScans=src.getScans_scan();
		final List<XnatImagescandataI> destScans=dest.getScans_scan();
		
		for(final XnatImagescandataI srcScan: srcScans){
			final XnatImagescandataI destScan = MergeUtils.getMatchingScan(srcScan,destScans);
			if(destScan==null){
			}else{
				final List<XnatAbstractresourceI> srcRess=srcScan.getFile();
				final List<XnatAbstractresourceI> destRess=destScan.getFile();
				
				for(final XnatAbstractresourceI srcRes:srcRess){
					final XnatAbstractresourceI destRes=MergeUtils.getMatchingResource(srcRes,destRess);
					if(destRes==null){
					}else{
						if(destRes instanceof XnatResourcecatalogI){
							final CatCatalogBean srcCat=CatalogUtils.getCleanCatalog(srcRootPath, (XnatResourcecatalogI)srcRes, false,u,c);

							final CatCatalogBean destCat=CatalogUtils.getCleanCatalog(destRootPath, (XnatResourcecatalogI)destRes, false,u,c);
							
							if(detectOverwrite(srcCat,destCat)){
								return true;
							}
						}else if(destRes instanceof XnatResourceseriesI){
							return true;
						}else if(destRes instanceof XnatResourceI){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	
	private static boolean detectOverwrite(final CatCatalogI src, final CatCatalogI dest)  {
		boolean merge=false;
		for(final CatCatalogI subCat:src.getSets_entryset()){
			if(detectOverwrite(subCat,dest)){
				return true;
			}
		}
		
		for(final CatEntryI entry: src.getEntries_entry()){
			if(entry instanceof CatDcmentryI && !StringUtils.isEmpty(((CatDcmentryI)entry).getUid())){
				final CatDcmentryI destEntry=CatalogUtils.getDCMEntryByUID(dest, ((CatDcmentryI)entry).getUid());
				if(destEntry!=null){
					return true;
				}
			}
			
			final CatEntryI destEntry=CatalogUtils.getEntryByURI(dest, entry.getUri());
			
			if(destEntry!=null){
				return true;
			}
		}
		
		return merge;
	}
}
