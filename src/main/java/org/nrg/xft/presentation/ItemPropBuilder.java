package org.nrg.xft.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.nrg.xft.XFTItem;
import org.nrg.xft.compare.ItemComparator;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.presentation.FlattenedItem.FlattenedItemModifierI;

@SuppressWarnings("unchecked")
public class ItemPropBuilder {
	public ItemPropBuilder(){
	}
	
	public List<FlattenedItemI> call(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory,List<? extends FlattenedItemModifierI> modifiers) throws Exception {		
		final List<XFTItem> items=new ArrayList<XFTItem>();
		items.add(i);
		if (i.hasHistory())
		{
			items.addAll(i.getHistoryItems());
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
