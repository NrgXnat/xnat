/*
 * core: org.nrg.xft.collections.SavedItemCollectionTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

/**
 * The saved-item collection must answer as the scan it replaces would: the first stored item, in insertion
 * order, whose primary key equals the probe's; nothing for a probe without one; and an item that was added
 * before its key was generated, once the key is there.
 */
public class SavedItemCollectionTest {
    private static final String SCAN = "xnat:mrScanData";
    private static final String PK   = "xnat_imagescandata_id";

    @Test
    public void findsTheStoredItemWithTheProbesPrimaryKey() throws Exception {
        final SavedItemCollection saved  = new SavedItemCollection();
        final XFTItem             first  = item(SCAN, 1);
        final XFTItem             second = item(SCAN, 2);
        saved.add(first);
        saved.add(second);

        assertSame(second, saved.findByPK(item(SCAN, 2), false));
        assertTrue(saved.containsByPK(item(SCAN, 1), false));
        assertNull(saved.findByPK(item(SCAN, 3), false));
        assertFalse(saved.containsByPK(item("xnat:petScanData", 2), false));
    }

    @Test
    public void aProbeWithoutAPrimaryKeyMatchesNothing() throws Exception {
        final SavedItemCollection saved = new SavedItemCollection();
        saved.add(item(SCAN, 1));

        assertNull(saved.findByPK(item(SCAN, null), false));
    }

    @Test
    public void findsAnItemWhoseKeyWasGeneratedAfterItWasAdded() throws Exception {
        final SavedItemCollection saved = new SavedItemCollection();
        final XFTItem             late  = item(SCAN, null);
        saved.add(late);
        assertNull(saved.findByPK(item(SCAN, 7), false));

        primaryKey(late, 7);
        assertSame(late, saved.findByPK(item(SCAN, 7), false));
    }

    @Test
    public void returnsTheFirstOfTwoItemsStoredUnderOneKey() throws Exception {
        final SavedItemCollection saved  = new SavedItemCollection();
        final XFTItem             first  = item(SCAN, 5);
        final XFTItem             second = item(SCAN, 5);
        saved.add(first);
        saved.add(second);

        assertSame(first, saved.findByPK(item(SCAN, 5), false));
    }

    @Test
    public void clearForgetsTheStoredItems() throws Exception {
        final SavedItemCollection saved = new SavedItemCollection();
        saved.add(item(SCAN, 1));
        saved.clear();

        assertNull(saved.findByPK(item(SCAN, 1), false));
    }

    /** A mock item of the given type with a single-column primary key (unset when null) and no unique fields. */
    private static XFTItem item(final String type, final Integer pk) throws Exception {
        final XFTItem item = mock(XFTItem.class);
        when(item.getItem()).thenReturn(item);
        when(item.getXSIType()).thenReturn(type);
        when(item.hasProperties()).thenReturn(true);
        when(item.getPkNames()).thenReturn(new ArrayList<>(Collections.singletonList(PK)));
        final GenericWrapperElement element = mock(GenericWrapperElement.class);
        when(element.getUniqueFields()).thenReturn(new ArrayList<>());
        when(element.getUniqueCompositeFields()).thenReturn(new Hashtable<>());
        when(item.getGenericSchemaElement()).thenReturn(element);
        primaryKey(item, pk);
        return item;
    }

    private static void primaryKey(final XFTItem item, final Integer pk) throws Exception {
        final Map<String, Object> pks = new HashMap<>();
        if (pk != null) {
            pks.put(PK, pk);
        }
        when(item.getPkValues()).thenReturn(pks);
        when(item.getProperty(item.getXSIType() + "/" + PK)).thenReturn(pk);
    }
}
