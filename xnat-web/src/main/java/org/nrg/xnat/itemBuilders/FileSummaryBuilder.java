/*
 * web: org.nrg.xnat.itemBuilders.FileSummaryBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.itemBuilders;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.presentation.FlattenedItem.FlattenedItemModifierI;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemA.FileSummary;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;

public class FileSummaryBuilder implements FlattenedItemModifierI{

    private static final Logger _log = LoggerFactory.getLogger(FileSummaryBuilder.class);

	private final Map<String,List<FileSummary>> found = new Hashtable<>();

	public void modify(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory, Callable<Integer> idGenerator, FlattenedItemA.FilterI filter, XMLWrapperElement root, List<FlattenedItemA.ItemObject> parents, FlattenedItemI fi) {
		if(root.instanceOf(XnatResourcecatalog.SCHEMA_ELEMENT_NAME)){
			String uri=(String)fi.getFields().getParams().get("URI");
			if(FileUtils.IsAbsolutePath(uri)){				
				fi.getMisc().addAll(getFiles(uri));
			}
		}
	}

	private List<FileSummary> getFiles(String uri) {
		if(!found.containsKey(uri)){
			final File catFile=new File(uri);
		
			Map<String,Map<String,Integer>> summary=new HashMap<>();
			
			merge(summary,CatalogUtils.retrieveAuditySummary(CatalogUtils.getCatalog(catFile)));
			
			List<File> historicalCats=CatalogUtils.findHistoricalCatFiles(catFile);
			for(File hisCatFile:historicalCats){
				merge(summary,CatalogUtils.retrieveAuditySummary(CatalogUtils.getCatalog(hisCatFile)));
			}
			
			List<FileSummary> files = new ArrayList<>();
							
			for(Map.Entry<String,Map<String,Integer>> sub:summary.entrySet()){
				String[] ids=sub.getKey().split(":");
				Integer change= (StringUtils.isEmpty(ids[0]) || ids[0].equalsIgnoreCase("null")) ? null : Integer.valueOf(ids[0]);
				Date d = null;
				try {
					d = (StringUtils.isEmpty(ids[1]) || ids[1].equalsIgnoreCase("null")) ? null : DateUtils.parseDateTime(ids[1]);
				} catch (ParseException e1) {
                    _log.warn("Invalid date-time format in " + ids[1], e1);
				}
				for(Map.Entry<String,Integer> sub2:sub.getValue().entrySet()){
					files.add(new FileSummary(change, d, sub2.getKey(), sub2.getValue()));
				}
			}

			found.put(uri, files);
		}
		return found.get(uri);
	}
	
	private static void merge(Map<String,Map<String,Integer>> _new, Map<String,Map<String,Integer>> old){
		for(Map.Entry<String,Map<String,Integer>> sub:old.entrySet()){
			if(_new.containsKey(sub.getKey())){
				for(Map.Entry<String,Integer> sub2:sub.getValue().entrySet()){
					CatalogUtils.addAuditEntry(_new, sub.getKey(), sub2.getKey(),sub2.getValue());
				}
			}else{
				_new.put(sub.getKey(), sub.getValue());
			}
		}
	}
}
