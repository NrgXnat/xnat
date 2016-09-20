/*
 * core: org.nrg.xft.presentation.ItemPropBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.compare.ItemComparator;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.presentation.FlattenedItem.FlattenedItemModifierI;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.ItemSearch;

@SuppressWarnings("unchecked")
public class ItemPropBuilder {
	public ItemPropBuilder(){
	}
	
	public List<FlattenedItemI> call(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory,List<? extends FlattenedItemModifierI> modifiers) throws Exception {		
		final List<XFTItem> items=new ArrayList<XFTItem>();
		items.add(i);
		if(i.isHistory()){
			//manually load other history items
			GenericWrapperElement gwe = GenericWrapperElement.GetElement(i.getXSIType().substring(0, i.getXSIType().indexOf("_history")));
			List<String> pks=gwe.getPkNames();
			CriteriaCollection cc= new CriteriaCollection("AND");
			for(String pk: pks){
				Object o=i.getProperty(pk);
				if(o==null){
					throw new Exception("History format exception");
				}
				cc.addClause(i.getXSIType()+"/"+pk, o);
			}
			
			ItemCollection other_histories =ItemSearch.GetItems(cc, null, false);
			if(other_histories!=null){
				for(ItemI hist: other_histories.getItems()){
					if(!hist.getProperty("history_id").equals(i.getProperty("history_id"))){
						items.add(hist.getItem());
					}
				}
			}
		}else{
			if (i.hasHistory() && (includeHistory==null || includeHistory.getIncludeHistory()))
			{
				items.addAll(i.getHistoryItems());
			}
		}
		
		Collections.sort(items,new ItemComparator());
		
		return FlattenedItem.buildLayeredProps(items,includeHistory, new Callable<Integer>(){
			private int count=0;
			public Integer call() throws Exception {
				return count++;
			}},null,FlattenedItem.convertElement(i.getGenericSchemaElement()),null,new ArrayList<FlattenedItemA.ItemObject>(),modifiers, new ItemCache());
	}

	
	public static List<FlattenedItemI> build(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory,List<? extends FlattenedItemModifierI> modifiers) throws Exception{
		return (new ItemPropBuilder()).call(i,includeHistory,modifiers);
	}
	
}
