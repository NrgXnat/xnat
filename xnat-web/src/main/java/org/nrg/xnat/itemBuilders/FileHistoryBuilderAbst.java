/*
 * org.nrg.xnat.itemBuilders.FileHistoryBuilderAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.itemBuilders;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class FileHistoryBuilderAbst {
	static Logger logger = Logger.getLogger(FileHistoryBuilderAbst.class);
	final Map<String,List<FlattenedItemI>> found= new Hashtable<String,List<FlattenedItemI>>();

	public void modify(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, XMLWrapperElement root, List<FlattenedItemA.ItemObject> parents, FlattenedItemI fi) {
		if(root.instanceOf(XnatResourcecatalog.SCHEMA_ELEMENT_NAME)){
			String uri=(String)fi.getFields().getParams().get("URI");
			if((!StringUtils.isEmpty(uri)) && FileUtils.IsAbsolutePath(uri)){
				List<FlattenedItemA.ItemObject> local=new ArrayList<FlattenedItemA.ItemObject>();
				local.addAll(parents);
				local.add(fi.getItemObject());
				
				fi.addChildCollection("system.file","file",getFiles(uri,idGenerator,local));
				
				
			}
		}
	}

	private List<FlattenedItemI> getFiles(String uri, Callable<Integer> idGenerator, List<FlattenedItemA.ItemObject> parents) {
		if(!found.containsKey(uri)){
			List<FlattenedItemI> files = new ArrayList<FlattenedItemI>();
			final File catFile=new File(uri);
			if(catFile.exists()){
				try {
					files.addAll(handleCatFile(catFile,false,idGenerator,parents));
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			
			List<File> historicalCats=CatalogUtils.findHistoricalCatFiles(catFile);
			for(File f:historicalCats){
				try {
					files.addAll(handleCatFile(f,true,idGenerator,parents));
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			
			found.put(uri, files);
		}
		return found.get(uri);
	}

	public FileHistoryBuilderAbst() {
		super();
	}
	
	public abstract List<FlattenedItemI> handleCatFile(File catFile,boolean isHistory,Callable<Integer> idGenerator, List<FlattenedItemA.ItemObject> parents) throws Exception;

}