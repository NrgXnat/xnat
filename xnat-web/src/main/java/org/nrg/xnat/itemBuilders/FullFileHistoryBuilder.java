/*
 * web: org.nrg.xnat.itemBuilders.FullFileHistoryBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.itemBuilders;

import org.apache.commons.lang3.StringUtils;
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
		final CatCatalogBean cat = CatalogUtils.getCatalog(catFile, null);
		
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

		return new FlattenedItem.FlattenedFile(ft,isHistory,last_modified,insert_date,idGenerator.call(),"system:file",entry.getCreatedby(),entry.getModifiedeventid(),entry.getCreatedeventid(),getIdentifier(entry),parents,entry.getCreatedby());
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

	/**
	 * Identifies an entry within its resource, and labels it on screen: the path the catalog recorded for
	 * it, not its name. Two files in different subdirectories of one resource share a name, and a name is
	 * a file's only tracked field, so identifying them by it makes the pair identical and the merge throws
	 * the second away. A file at the root of its resource records its name as its path, so the common case
	 * reads exactly as before.
	 *
	 * <p>The recorded path is also what still matches an entry to its earlier versions. A history entry
	 * is a copy of the entry it superseded with only the URI rewritten, to an absolute path under
	 * {@code history/}, so the URI cannot do it. An entry old enough to record no path falls back to the
	 * label, which is what identified every entry before.
	 */
	public static String getIdentifier(CatEntryI entry){
		final String path = StringUtils.defaultIfBlank(entry.getId(), entry.getCachepath());
		if(StringUtils.isNotBlank(path)){
			return path;
		}

		final String uri = entry.getUri();
		return (StringUtils.isNotBlank(uri) && !FileUtils.IsAbsolutePath(uri)) ? uri : getLabel(entry);
	}
}
