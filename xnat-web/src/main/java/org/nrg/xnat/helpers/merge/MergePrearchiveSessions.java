/*
 * org.nrg.xnat.helpers.merge.MergePrearchiveSessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/4/13 9:59 AM
 */
package org.nrg.xnat.helpers.merge;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.*;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.restlet.data.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class MergePrearchiveSessions extends MergeSessionsA<XnatImagesessiondataBean>  {

	public MergePrearchiveSessions(Object control,final File srcDIR, final XnatImagesessiondataBean src, final String srcRootPath, final File destDIR, final XnatImagesessiondataBean existing, final String destRootPath, boolean allowSessionMerge, boolean overwriteFiles, SaveHandlerI<XnatImagesessiondataBean> saver, final UserI u) {
		super(control, srcDIR, src, srcRootPath, destDIR, existing, destRootPath, allowSessionMerge, overwriteFiles, saver,u,null);
		super.setAnonymizer(new SiteWideAnonymizer(src, true));
	}

	public String getCacheBKDirName() { 
		return "prearc_merge";
	}
	

	public org.nrg.xnat.helpers.merge.MergeSessionsA.Results<XnatImagesessiondataBean> mergeSessions(final XnatImagesessiondataBean src, final String srcRootPath, final XnatImagesessiondataBean dest, final String destRootPath,final File rootbackup) throws ClientException, ServerException {
		if(dest==null)return new Results<XnatImagesessiondataBean>(src);

		final Results<XnatImagesessiondataBean> result=new Results<XnatImagesessiondataBean>(dest);
		final List<XnatImagescandataI> srcScans=src.getScans_scan();
		final List<XnatImagescandataI> destScans=dest.getScans_scan();
	
		final List<File> toDelete=new ArrayList<File>();
		processing("Merging new meta-data into existing meta-data.");
		try {
			for(final XnatImagescandataI srcScan: srcScans){
				final XnatImagescandataI destScan = MergeUtils.getMatchingScan(srcScan,destScans);
				if(destScan==null){
					dest.addScans_scan(srcScan); 
				}else{
					final List<XnatAbstractresourceI> srcRess=srcScan.getFile();
					final List<XnatAbstractresourceI> destRess=destScan.getFile();
					
					for(final XnatAbstractresourceI srcRes:srcRess){
						final XnatAbstractresourceI destRes=MergeUtils.getMatchingResource(srcRes,destRess);
						if(destRes==null){
							destScan.addFile(srcRes);
						}else{
							if(destRes instanceof XnatResourcecatalogI){
								MergeSessionsA.Results<File> r=mergeCatalogs(srcRootPath,(XnatResourcecatalogI)srcRes,destRootPath,(XnatResourcecatalogI)destRes);
								if(r!=null){
									toDelete.add(r.result);
									result.addAll(r);
								}
							}else if(destRes instanceof XnatResourceseriesI){
								srcRes.setLabel(srcRes.getLabel()+"2");
								destScan.addFile(srcRes);
							}else if(destRes instanceof XnatResourceI){
								srcRes.setLabel(srcRes.getLabel()+"2");
								destScan.addFile(srcRes);
							}
						}
					}
				}
			}
		} catch (MergeCatCatalog.DCMEntryConflict e) {
			failed("Duplicate DCM UID cannot be merged at this time.");
			throw new ClientException(Status.CLIENT_ERROR_CONFLICT,e.getMessage(), e);
		} catch (Exception e) {
			failed("Failed to merge upload into existing prearchive.");
			throw new ServerException(e.getMessage(), e);
		}
		
		final File backup = new File(rootbackup,"catalog_bk");
		backup.mkdirs();
		
		final List<Callable<Boolean>> followup=new ArrayList<Callable<Boolean>>();
		followup.add(new Callable<Boolean>(){
			@Override
			public Boolean call() throws Exception {
				try {
					int count=0;
					for(File f:toDelete){
						File catBkDir=new File(backup,""+count++);
						catBkDir.mkdirs();
						
						FileUtils.MoveFile(f, new File(catBkDir,f.getName()), false);
					}
					return Boolean.TRUE;
				} catch (Exception e) {
					throw new ServerException(e.getMessage(),e);
				}
			}});
		
		result.getBeforeDirMerge().addAll(followup);
		return result;
	}



	@Override
	public void finalize(XnatImagesessiondataBean session)
			throws ClientException, ServerException {
		session.setPrearchivepath(this.destRootPath);
	}



}
