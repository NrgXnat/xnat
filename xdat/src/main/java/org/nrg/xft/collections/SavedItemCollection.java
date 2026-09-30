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
import org.nrg.xft.utils.SaveLaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The items one database transaction has stored so far (see {@code DBItemCache.getSaved()}), kept so that
 * {@code DBAction.StoreItem} can tell an item it has already stored from a new one. StoreItem asks that of
 * every item it walks, and the plain {@link ItemCollection} answers by comparing the probe with every stored
 * item, so a session of 200 scans compared some forty thousand pairs, each pair reading and formatting the
 * items' key fields. This collection indexes its items by their primary-key values and by the keys of
 * {@link ItemUniqueEquality#uniqueKeys}, finds the few candidates that could match and confirms each with the
 * same comparison the scan used, so its answers are the scan's answers: a probe with no primary-key values
 * matches nothing by primary key (as {@code ItemPKEquality.doCheck} says), a probe matches by unique values only
 * an item sharing one of its keys, the first match in insertion order wins, and where the keys cannot speak for
 * the comparison (a field that cannot be read) the scan is performed. Extension-aware lookups
 * ({@code checkExtensions}) are left to the scan.
 */
public class SavedItemCollection extends ItemCollection {
    private static final Logger logger = Logger.getLogger(SavedItemCollection.class);

    private final Map<String, List<ItemI>>        byPk        = new HashMap<>();
    private final List<ItemI>                     pkPending   = new ArrayList<>();   // items added before their key was set
    private final Map<String, List<ItemI>>        byUniqueKey = new HashMap<>();
    private final IdentityHashMap<ItemI, Integer> order       = new IdentityHashMap<>();
    private final List<ItemI>                     unindexed   = new ArrayList<>();   // items whose unique keys could not be derived

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
        byPk.clear();
        pkPending.clear();
        byUniqueKey.clear();
        order.clear();
        unindexed.clear();
    }

    private void index(final ItemI item) {
        final XFTItem xftItem = item.getItem();
        order.put(item, order.size());
        indexByPk(item);
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

    /**
     * Files the item under each of its primary-key values. An item that has primary-key fields but no values
     * yet (InsertItem adds an item before the immediate insert path returns its generated key) is held and filed
     * at the next lookup, when the key is there. An item whose key cannot be read is filed nowhere, as the scan's
     * comparison against it would fail the same way.
     */
    private void indexByPk(final ItemI item) {
        final XFTItem xftItem = item.getItem();
        try {
            final List<String> keys = pkKeys(xftItem);
            if (keys.isEmpty()) {
                if (!xftItem.getPkNames().isEmpty()) {
                    pkPending.add(item);
                }
                return;
            }
            for (final String key : keys) {
                byPk.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
        } catch (Exception e) {
            logger.error("Unable to read the primary key of a stored " + xftItem.getXSIType(), e);
        }
    }

    private void indexPending() {
        if (pkPending.isEmpty()) {
            return;
        }
        final Iterator<ItemI> pending = pkPending.iterator();
        while (pending.hasNext()) {
            final ItemI item = pending.next();
            try {
                final List<String> keys = pkKeys(item.getItem());
                if (!keys.isEmpty()) {
                    for (final String key : keys) {
                        byPk.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                    }
                    pending.remove();
                }
            } catch (Exception e) {
                logger.error("Unable to read the primary key of a stored " + item.getItem().getXSIType(), e);
                pending.remove();
            }
        }
    }

    /**
     * "P|type|column|value" for each primary-key value the item has, read as {@code ItemPKEquality.doCheck}
     * reads them ({@link XFTItem#getPkValues()} leaves out the columns without a value). Two values doCheck
     * finds equal print the same, so two items it would match share every key; two that merely print the same
     * share a key and are told apart by the comparison that confirms each candidate.
     */
    private static List<String> pkKeys(final XFTItem item) throws Exception {
        final String       type = item.getXSIType().toLowerCase();
        final List<String> keys = new ArrayList<>();
        for (final Map.Entry<String, Object> pk : item.getPkValues().entrySet()) {
            keys.add("P|" + type + '|' + pk.getKey() + '|' + pk.getValue());
        }
        return keys;
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
        final long started = SaveLaps.start();
        try {
            return findByPKIndexed(item);
        } finally {
            SaveLaps.add(SaveLaps.Lap.SAVED_LOOKUPS, started);
        }
    }

    private ItemI findByPKIndexed(final ItemI item) {
        final XFTItem      probe = item.getItem();
        final List<String> keys;
        try {
            keys = pkKeys(probe);
        } catch (Exception e) {
            logger.error("", e);
            return super.findByPK(item, false);
        }
        if (keys.isEmpty()) {
            return null;   // ItemPKEquality.doCheck matches nothing for an item without primary-key values
        }
        indexPending();
        // doCheck needs every primary-key value to agree, so the items sharing the probe's first hold every match.
        return firstMatchByPk(probe, byPk.getOrDefault(keys.getFirst(), Collections.emptyList()));
    }

    private ItemI firstMatchByPk(final XFTItem probe, final List<ItemI> candidates) {
        for (final ItemI candidate : inOrder(candidates)) {
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

    /** The candidates in the order the scan would have reached them (an item filed late sits out of order). */
    private Iterable<ItemI> inOrder(final List<ItemI> candidates) {
        if (candidates.size() < 2) {
            return candidates;
        }
        final TreeMap<Integer, ItemI> sorted = new TreeMap<>();
        for (final ItemI candidate : candidates) {
            sorted.putIfAbsent(order.get(candidate), candidate);
        }
        return sorted.values();
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
        final long started = SaveLaps.start();
        try {
            return findByUniqueIndexed(item);
        } finally {
            SaveLaps.add(SaveLaps.Lap.SAVED_LOOKUPS, started);
        }
    }

    private ItemI findByUniqueIndexed(final ItemI item) {
        final XFTItem      probe = item.getItem();
        final List<String> keys;
        try {
            keys = ItemUniqueEquality.uniqueKeys(probe);
        } catch (Exception e) {
            logger.error("", e);
            return super.findByUnique(item, false);
        }
        if (keys.isEmpty()) {
            // No unique field with a value and no complete unique composite: doCheck matches nothing, as the
            // scan's own hasUniques() gate concluded before comparing (it read the same properties again).
            return null;
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
