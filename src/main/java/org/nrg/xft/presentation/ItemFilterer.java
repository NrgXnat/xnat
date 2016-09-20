/*
 * core: org.nrg.xft.presentation.ItemFilterer
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.List;

public class ItemFilterer {
	final FlattenedItemA.FilterI filter;
	public ItemFilterer(FlattenedItemA.FilterI filter){
		this.filter=filter;
	}
	
	public List<FlattenedItemI> call(List<FlattenedItemI> items) throws Exception {
		return FlattenedItemA.filter(items, filter);
	}

	
	public static List<FlattenedItemI> filter(List<FlattenedItemI> items, FlattenedItemA.FilterI filter) throws Exception{
		return (new ItemFilterer(filter)).call(items);
	}
}
