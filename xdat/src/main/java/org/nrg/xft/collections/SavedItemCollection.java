/*
 * core: org.nrg.xft.collections.SavedItemCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.collections;

import org.apache.log4j.Logger;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.compare.ItemUniqueEquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The items one database transaction has stored so far (see {@code DBItemCache.getSaved()}), kept so that
 * {@code DBAction.StoreItem} can tell an item it has already stored from a new one. StoreItem asks that of
 * every item it walks, and the plain {@link ItemCollection} answers by comparing the probe with every stored
 * item, so a session of 200 scans compared some forty thousand pairs, each pair reading and formatting the
 * items' unique fields. This collection indexes its items by type and by the keys of
 * {@link ItemUniqueEquality#uniqueKeys}, finds the few candidates that could match and confirms each with the
 * same comparison the scan used, so its answers are the scan's answers: a probe with no primary-key values
 * matches nothing by primary key (as {@code ItemPKEquality.doCheck} says), a probe matches by unique values only
 * an item sharing one of its keys, and where the keys cannot speak for the comparison (a field that cannot be
 * read) the scan is performed. Extension-aware lookups ({@code checkExtensions}) are left to the scan.
 */
public class SavedItemCollection extends ItemCollection {
    private static final Logger logger = Logger.getLogger(SavedItemCollection.class);

    private final Map<String, List<ItemI>>        byType      = new HashMap<>();
    private final Map<String, List<ItemI>>        byUniqueKey = new HashMap<>();
    private final IdentityHashMap<ItemI, Integer> order       = new IdentityHashMap<>();
    private final List<ItemI>                     unindexed   = new ArrayList<>();   // items whose keys could not be derived

    @Override
    public void addItem(final ItemI item) {
        super.addItem(item);
        index(item);
    }

    @Override
    public void addAll(final List list) {
        super.addAll(list);
        for (final Object item : list) {
            index((ItemI) item);
        }
    }

    @Override
    public void clear() {
        super.clear();
        byType.clear();
        byUniqueKey.clear();
        order.clear();
        unindexed.clear();
    }

    private void index(final ItemI item) {
        final XFTItem xftItem = item.getItem();
        order.put(item, order.size());
        byType.computeIfAbsent(xftItem.getXSIType().toLowerCase(), k -> new ArrayList<>()).add(item);
        try {
            for (final String key : ItemUniqueEquality.uniqueKeys(xftItem)) {
                byUniqueKey.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
        } catch (Exception e) {
            // Without its keys the item can still be found by the scan, which the lookups fall back to.
            logger.error("Unable to derive the unique keys of a stored " + xftItem.getXSIType() + "; lookups against it will scan", e);
            unindexed.add(item);
        }
    }

    @Override
    public boolean containsByPK(final ItemI item, final boolean checkExtensions) {
        return checkExtensions ? super.containsByPK(item, true) : findByPK(item, false) != null;
    }

    @Override
    public ItemI findByPK(final ItemI item, final boolean checkExtensions) {
        if (checkExtensions) {
            return super.findByPK(item, true);
        }
        final XFTItem probe = item.getItem();
        try {
            if (probe.getPkValues().isEmpty()) {
                return null;   // ItemPKEquality.doCheck matches nothing for an item without primary-key values
            }
        } catch (Exception e) {
            logger.error("", e);
            return super.findByPK(item, false);
        }
        for (final ItemI candidate : byType.getOrDefault(probe.getXSIType().toLowerCase(), Collections.emptyList())) {
            try {
                if (XFTItem.CompareItemsByPKs(probe, candidate.getItem(), false, false)) {
                    return candidate;
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }

    @Override
    public boolean containsByUnique(final ItemI item, final boolean checkExtensions) {
        return checkExtensions ? super.containsByUnique(item, true) : findByUnique(item, false) != null;
    }

    @Override
    public ItemI findByUnique(final ItemI item, final boolean checkExtensions) {
        if (checkExtensions) {
            return super.findByUnique(item, true);
        }
        final XFTItem probe = item.getItem();
        final List<String> keys;
        try {
            if (!probe.hasUniques()) {
                return null;   // the scan compares nothing for an item without unique values
            }
            keys = ItemUniqueEquality.uniqueKeys(probe);
        } catch (Exception e) {
            logger.error("", e);
            return super.findByUnique(item, false);
        }
        // Candidates in the order the scan would have reached them: items sharing a key, then any item whose
        // keys could not be derived.
        final TreeMap<Integer, ItemI> candidates = new TreeMap<>();
        for (final String key : keys) {
            for (final ItemI candidate : byUniqueKey.getOrDefault(key, Collections.emptyList())) {
                candidates.putIfAbsent(order.get(candidate), candidate);
            }
        }
        for (final ItemI candidate : unindexed) {
            candidates.putIfAbsent(order.get(candidate), candidate);
        }
        for (final ItemI candidate : candidates.values()) {
            try {
                if (XFTItem.CompareItemsByUniques(probe, candidate.getItem(), false)) {
                    return candidate;
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return null;
    }
}
