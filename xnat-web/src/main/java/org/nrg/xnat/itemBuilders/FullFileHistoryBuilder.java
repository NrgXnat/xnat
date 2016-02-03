/*
 * org.nrg.xnat.itemBuilders.FullFileHistoryBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.itemBuilders;

import org.apache.log4j.Logger;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xft.presentation.FlattenedItem;
import org.nrg.xft.presentation.FlattenedItem.FlattenedItemModifierI;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class FullFileHistoryBuilder extends FileHistoryBuilderAbst implements FlattenedItemModifierI {
	static Logger logger = Logger.getLogger(FullFileHistoryBuilder.class);
		
		
	public List<FlattenedItemI> handleCatFile(File catFile,boolean isHistory,Callable<Integer> idGenerator, List<FlattenedItemA.ItemObject> parents) throws Exception{
		final CatCatalogBean cat = CatalogUtils.getCatalog(catFile);
		
		List<FlattenedItemI> files=new ArrayList<FlattenedItemI>();
		
		if(cat!=null){
			final Collection<CatEntryI> entries=CatalogUtils.getEntriesByFilter(cat, null);
			for(final CatEntryI entry:entries){
				if(!isHistory || FileUtils.IsAbsolutePath(entry.getUri())){
					files.add(BuildFlattenedFile(entry, isHistory, idGenerator, parents));
				}
			}
		}
		
		return files;
	}

	public static FlattenedItem.FlattenedFile BuildFlattenedFile(CatEntryI entry, boolean isHistory,Callable<Integer> idGenerator,List<FlattenedItemA.ItemObject> parents) throws Exception{
		FlattenedItemA.FieldTracker ft=new FlattenedItemA.FieldTracker();
		
		Date last_modified=FlattenedItemA.parseDate(entry.getModifiedtime());
		Date insert_date=FlattenedItemA.parseDate(entry.getCreatedtime());

		return new FlattenedItem.FlattenedFile(ft,isHistory,last_modified,insert_date,idGenerator.call(),"system:file",entry.getCreatedby(),entry.getModifiedeventid(),entry.getCreatedeventid(),getLabel(entry),parents,entry.getCreatedby());
	}

	public static String getLabel(CatEntryI entry){
		if(entry.getName()!=null){
			return entry.getName();
		}else{
			if(FileUtils.IsAbsolutePath(entry.getUri())){
				return new File(entry.getUri()).getName();
			}else{
				return entry.getUri();
			}
		}
	}
}
