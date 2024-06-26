/*
 * web: org.nrg.xnat.helpers.merge.SessionOverwriteCheck
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.*;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.CatalogUtils;

import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
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

		final String srcProject = src.getProject();
		final String destProject = dest.getProject();
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
							try {
								final CatalogUtils.CatalogData srcCatalogData =
										CatalogUtils.CatalogData.getOrCreateAndClean(srcRootPath, (XnatResourcecatalogI) srcRes, false, srcProject,
                                                u, c);

								final CatCatalogBean srcCat = srcCatalogData.catBean;
								final CatalogUtils.CatalogData destCatalogData =
										CatalogUtils.CatalogData.getOrCreateAndClean(destRootPath, (XnatResourcecatalogI) destRes, false, destProject,
                                                u, c);
								final CatCatalogBean destCat = destCatalogData.catBean;
								if (detectOverwrite(srcCat,destCat)) {
									return true;
								}
							} catch (ServerException e) {
								log.error("Unable to create or read catalog", e);
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
