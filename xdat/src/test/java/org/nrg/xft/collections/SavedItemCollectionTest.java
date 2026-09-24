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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.XFTField;
import org.nrg.xft.schema.XFTRule;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

/**
 * The saved-item collection must answer as the scan it replaces would: the first stored item, in insertion
 * order, whose primary key or unique values equal the probe's; nothing for a probe without them; an item that
 * was added before its key was generated, once the key is there; and an item whose keys could not be derived,
 * by comparing it as the scan did.
 */
public class SavedItemCollectionTest {
    private static final String SCAN     = "xnat:mrScanData";
    private static final String RESOURCE = "xnat:resourceCatalog";
    private static final String PK       = "xnat_imagescandata_id";

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

    @Test
    public void findsTheStoredItemSharingTheProbesUniqueValue() throws Exception {
        final SavedItemCollection saved     = new SavedItemCollection();
        final XFTItem             dicom     = labelled(RESOURCE, "DICOM");
        final XFTItem             snapshots = labelled(RESOURCE, "SNAPSHOTS");
        saved.add(dicom);
        saved.add(snapshots);

        assertSame(snapshots, saved.findByUnique(labelled(RESOURCE, "SNAPSHOTS"), false));
        assertTrue(saved.containsByUnique(labelled(RESOURCE, "DICOM"), false));
        assertNull(saved.findByUnique(labelled(RESOURCE, "NIFTI"), false));
        assertNull(saved.findByUnique(labelled(RESOURCE, null), false));
        assertFalse(saved.containsByUnique(labelled("xnat:resource", "DICOM"), false));
    }

    @Test
    public void returnsTheFirstOfTwoItemsSharingAUniqueValue() throws Exception {
        final SavedItemCollection saved  = new SavedItemCollection();
        final XFTItem             first  = labelled(RESOURCE, "DICOM");
        final XFTItem             second = labelled(RESOURCE, "DICOM");
        saved.add(first);
        saved.add(second);

        assertSame(first, saved.findByUnique(labelled(RESOURCE, "DICOM"), false));
    }

    @Test
    public void matchesAUniqueCompositeOnlyWhenEveryFieldHasAValue() throws Exception {
        final SavedItemCollection saved = new SavedItemCollection();
        final XFTItem             scan  = composite(SCAN, "1", "XNAT_E00001");
        saved.add(scan);

        assertSame(scan, saved.findByUnique(composite(SCAN, "1", "XNAT_E00001"), false));
        assertNull(saved.findByUnique(composite(SCAN, "2", "XNAT_E00001"), false));
        assertNull(saved.findByUnique(composite(SCAN, "1", null), false));
    }

    @Test
    public void stillFindsAnItemWhoseKeysCouldNotBeDerived() throws Exception {
        final SavedItemCollection saved   = new SavedItemCollection();
        final XFTItem             unkeyed = labelled(RESOURCE, "DICOM");
        when(unkeyed.getGenericSchemaElement().getUniqueFields()).thenThrow(new IllegalStateException("schema not loaded"));
        saved.add(unkeyed);

        assertSame(unkeyed, saved.findByUnique(labelled(RESOURCE, "DICOM"), false));
        assertNull(saved.findByUnique(labelled(RESOURCE, "NIFTI"), false));
    }

    /** A mock item of the given type with a single-column primary key (unset when null) and no unique fields. */
    private static XFTItem item(final String type, final Integer pk) throws Exception {
        final XFTItem item = mock(XFTItem.class);
        when(item.getItem()).thenReturn(item);
        when(item.getXSIType()).thenReturn(type);
        when(item.hasProperties()).thenReturn(true);
        when(item.getPkNames()).thenReturn(new ArrayList<>(Collections.singletonList(PK)));
        final GenericWrapperElement element = mock(GenericWrapperElement.class);
        when(element.getFullXMLName()).thenReturn(type);
        when(element.getUniqueFields()).thenReturn(new ArrayList<>());
        when(element.getUniqueCompositeFields()).thenReturn(new Hashtable<>());
        when(item.getGenericSchemaElement()).thenReturn(element);
        primaryKey(item, pk);
        return item;
    }

    /** A mock item without a primary key whose element has one unique field, label, with the given value. */
    private static XFTItem labelled(final String type, final String label) throws Exception {
        final XFTItem             item  = item(type, null);
        final GenericWrapperField field = field(type, "label");
        when(item.hasUniques()).thenReturn(true);
        when(item.getGenericSchemaElement().getUniqueFields()).thenReturn(new ArrayList<>(Collections.singletonList(field)));
        when(item.getProperty(type + "/label")).thenReturn(label);
        return item;
    }

    /** A mock item without a primary key whose element has one unique composite of ID and image_session_ID. */
    private static XFTItem composite(final String type, final String id, final String session) throws Exception {
        final XFTItem                                 item       = item(type, null);
        final Hashtable<String, List<GenericWrapperField>> composites = new Hashtable<>();
        composites.put("SESSION_SCAN", new ArrayList<>(Arrays.asList(field(type, "ID"), field(type, "image_session_ID"))));
        when(item.hasUniques()).thenReturn(true);
        when(item.getGenericSchemaElement().getUniqueCompositeFields()).thenReturn(composites);
        when(item.getProperty(type + "/ID")).thenReturn(id);
        when(item.getProperty(type + "/image_session_ID")).thenReturn(session);
        return item;
    }

    /** A mock string field: the value parser reads its XML type and, for strings, its rule's base type. */
    private static GenericWrapperField field(final String type, final String name) {
        final GenericWrapperField field   = mock(GenericWrapperField.class);
        final XMLType             xmlType = mock(XMLType.class);
        final XFTField            wrapped = mock(XFTField.class);
        final XFTRule             rule    = mock(XFTRule.class);
        when(xmlType.getLocalType()).thenReturn("string");
        when(rule.getBaseType()).thenReturn("xs:string");
        when(wrapped.getRule()).thenReturn(rule);
        when(field.getWrapped()).thenReturn(wrapped);
        when(field.isReference()).thenReturn(false);
        when(field.getXMLType()).thenReturn(xmlType);
        when(field.getXMLPathString(type)).thenReturn(type + "/" + name);
        return field;
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
