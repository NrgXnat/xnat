/*
 * core: org.nrg.xft.presentation.ItemPropBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.presentation;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.presentation.FlattenedItem.FlattenedItemModifierI;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.ItemSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.nrg.xft.compare.ItemComparator.DEFAULT_ITEM_COMPARATOR;

@NoArgsConstructor
@Slf4j
public class ItemPropBuilder {
    public List<FlattenedItemI> call(final XFTItem item, FlattenedItemA.HistoryConfigI includeHistory, List<? extends FlattenedItemModifierI> modifiers) throws Exception {
        final List<XFTItem> items = new ArrayList<>();
        items.add(item);
        if (item.isHistory()) {
            //manually load other history items
            final CriteriaCollection criteria = new CriteriaCollection("AND");
            for (final String primaryKey : GenericWrapperElement.GetElement(item.getXSIType().substring(0, item.getXSIType().indexOf("_history"))).getPkNames()) {
                final Object object = item.getProperty(primaryKey);
                if (object == null) {
                    throw new Exception("History format exception");
                }
                criteria.addClause(item.getXSIType() + "/" + primaryKey, object);
            }
            final ItemCollection histories = ItemSearch.GetItems(criteria, null, false);
            if (histories != null) {
                for (final ItemI history : histories.getItems()) {
                    if (!history.getProperty("history_id").equals(item.getProperty("history_id"))) {
                        items.add(history.getItem());
                    }
                }
            }
        } else if (item.hasHistory() && (includeHistory == null || includeHistory.getIncludeHistory())) {
            items.addAll(GenericUtils.convertToTypedList(item.getHistoryItems(), XFTItem.class));
        }

        items.sort(DEFAULT_ITEM_COMPARATOR);

        return FlattenedItem.buildLayeredProps(items, includeHistory, new ItemCounter(), null, null, Collections.emptyList(), modifiers, new ItemCache());
    }

    public static List<FlattenedItemI> build(final XFTItem item, final FlattenedItemA.HistoryConfigI includeHistory, final List<? extends FlattenedItemModifierI> modifiers) throws Exception {
        return (new ItemPropBuilder()).call(item, includeHistory, modifiers);
    }

    private static class ItemCounter implements Callable<Integer> {
        private final AtomicInteger count = new AtomicInteger();

        public Integer call() {
            return count.getAndIncrement();
        }
    }
}
