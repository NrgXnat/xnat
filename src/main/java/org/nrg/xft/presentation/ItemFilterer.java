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
