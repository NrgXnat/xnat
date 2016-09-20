/*
 * core: org.nrg.xft.presentation.ItemMerger
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.nrg.xft.utils.DateUtils;

public class ItemMerger {
	
	public FlattenedItemI call(List<FlattenedItemI> items) throws Exception {				
		return mergeItems(items);
	}
	
	public static FlattenedItemI merge(List<FlattenedItemI> items) throws Exception{
		return (new ItemMerger()).call(items);
	}
	
	/**
	 * Input is expected to represent different versions of the same data object (different timepoints)
	 * @param items
	 * @return
	 */
	private static FlattenedItemI mergeItems(List<FlattenedItemI> items){
		FlattenedItemI primary=null;
		
		for(final FlattenedItemI item:items){
			FlattenedItemI secondary;
			if(primary==null){
				secondary=item;
			}else{
				if(DateUtils.isOnOrAfter(primary.getStartDate(), item.getStartDate())){
					secondary=primary;
					primary=item;
				}else{
					secondary=item;
				}
			}
			
			if(primary==null){
				consolidate(secondary);
				primary=secondary;
				continue;
			}else{
				combine(primary,secondary);
			}
		}
		
		return primary;
	}
	
	/**
	 * Merges these two copies of the same object into one copy.  If the secondary differs from the primary, then the secondary will be added to the getHistory() of the primary.
	 * @param primary
	 * @param secondary
	 */
	private static void combine(FlattenedItemI primary, FlattenedItemI secondary){
		Map<String, FlattenedItemA.ChildCollection> children=secondary.getChildCollections();
		
		for(final Map.Entry<String,FlattenedItemA.ChildCollection> entry:children.entrySet()){
			final FlattenedItemA.ChildCollection pChild;
			if(primary.getChildCollections().containsKey(entry.getKey())){
				pChild=primary.getChildCollections().get(entry.getKey());
				pChild.getChildren().addAll(entry.getValue().getChildren());
				pChild.buildHeaders();
			}else{
				pChild=entry.getValue();
				primary.getChildCollections().put(entry.getKey(),entry.getValue());
			}
			
			consolidate(pChild);
		}

		secondary.setChildCollections(new Hashtable<String, FlattenedItemA.ChildCollection>());
		
		if(secondary.getHistory().size()>0){
			for(FlattenedItemI his:secondary.getHistory()){
				if(find(primary.getHistory(),his)==null && !FlattenedItemA.isEqualTo(primary,his)){
					primary.getHistory().add(his);
				}
			}
		}
		
		if(find(primary.getHistory(),secondary)==null && !FlattenedItemA.isEqualTo(primary,secondary)){
			primary.getHistory().add(secondary);
		}
	}
	
	/**
	 * Consolidates all of the children of the object, to populate the historical versions and leave one main copy.
	 * @param item
	 */
	private static void consolidate(FlattenedItemI item){
		for(Map.Entry<String,FlattenedItemA.ChildCollection> entry:item.getChildCollections().entrySet()){
			consolidate(entry.getValue());
		}
	}
	
	/**
	 * Merges the children together.
	 * @param child
	 */
	private static void consolidate(FlattenedItemA.ChildCollection child){
		final List<List<FlattenedItemI>> grouped=group(child.getChildren());
		final List<FlattenedItemI> merged=new ArrayList<FlattenedItemI>();
		for(List<FlattenedItemI> items:grouped){
			merged.add(mergeItems(items));
		}
		child.setChildren(merged);
	}
	
	private static FlattenedItemI find(List<FlattenedItemI> items, FlattenedItemI here){
		for (FlattenedItemI there: items){
			if(FlattenedItemA.isEqualTo(there,here)){
				return there;
			}
		}
		return null;
	}
	
	/**
	 * @param items
	 * @return
	 */
	private static List<List<FlattenedItemI>> group(List<FlattenedItemI> items){
		List<List<FlattenedItemI>> itemsByLike=new ArrayList<List<FlattenedItemI>>();
		for(FlattenedItemI item:items){
			boolean matched=false;
			for(List<FlattenedItemI> list:itemsByLike){
				if(list.size()>0){
					if(FlattenedItemA.isLike(item,list.get(0)))
					{
						list.add(item);
						
						if(item.getHistory().size()>0){
							list.addAll(item.getHistory());
							item.setHistory(new ArrayList<FlattenedItemI>());
						}
						
						matched=true;
					}
				}
			}
			
			
			
			if(!matched){
				List<FlattenedItemI> list=new ArrayList<FlattenedItemI>();
				list.add(item);
				
				if(item.getHistory().size()>0){
					list.addAll(item.getHistory());
					item.setHistory(new ArrayList<FlattenedItemI>());
				}
				
				itemsByLike.add(list);
			}
		}
		return itemsByLike;
	}
}
