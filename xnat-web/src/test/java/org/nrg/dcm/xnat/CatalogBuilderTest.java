/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CatalogBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.*;
import org.nrg.util.FileURIOpener;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CatalogBuilderTest extends Scan4TestCase {
    @Before
    public void setUp() throws IOException {
        _scan4Dir = initializeScan4WorkingCopy();
    }

    @After
    public void tearDown() {
        tearDownWorkingCopy(_scan4Dir);
    }

    @Test
    public void testCall() throws IOException, SQLException {
        final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, FileURIOpener.getInstance());
        store.add(Sets.newHashSet(_scan4Dir.toURI()));
        final CatalogBuilder builder  = new CatalogBuilder("4", null, store, _scan4Dir, false);
        final Map<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> catalogs = builder.call();
        assertEquals(1, catalogs.size());
        final Map.Entry<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> me = catalogs.entrySet().iterator().next();
        final XnatResourcecatalogBean resource = me.getValue().getKey();
        assertEquals("DICOM", resource.getLabel());
        assertEquals("DICOM", resource.getFormat());
        assertEquals("RAW", resource.getContent());

        final CatDcmcatalogBean catalog = me.getValue().getValue();
        assertEquals(Integer.valueOf(9), catalog.getDimensions_z());
        assertEquals(Integer.valueOf(256), catalog.getDimensions_x());
        assertEquals(Integer.valueOf(256), catalog.getDimensions_y());
    }

    @Test
    public void testCallWithRead() throws IOException, SQLException {
        final DicomMetadataStore                              store    = EnumeratedMetadataStore.createHSQLDBBacked(TAGS, ADD_COLS, FileURIOpener.getInstance());
        final CatalogBuilder                                  builder  = new CatalogBuilder("4", null, store, _scan4Dir, true);
        final Map<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> catalogs = builder.call();
        assertEquals(1, catalogs.size());
        final Map.Entry<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> me = catalogs.entrySet().iterator().next();
        final XnatResourcecatalogBean resource = me.getValue().getKey();
        assertEquals("DICOM", resource.getLabel());
        assertEquals("DICOM", resource.getFormat());
        assertEquals("RAW", resource.getContent());

        final CatDcmcatalogBean catalog = me.getValue().getValue();
        assertEquals(Integer.valueOf(9), catalog.getDimensions_z());
        assertEquals(Integer.valueOf(256), catalog.getDimensions_x());
        assertEquals(Integer.valueOf(256), catalog.getDimensions_y());
    }

    // TODO: test with secondary files
    // TODO: test some failure recovery?

    private static final Collection<String> ADD_COLS = Collections.singletonList(SOPModel.XNAT_SCAN_COLUMN);
    private static final Collection<DicomAttributeIndex> TAGS;

    static {
        final Collection<DicomAttributeIndex> tags = Lists.newArrayList();
        tags.add(NamedAttributes.SOPClassUID);
        tags.addAll(ImageFileAttributes.get().getNativeAttrs());
        tags.addAll(CatalogAttributes.get().getNativeAttrs());
        TAGS = ImmutableList.copyOf(tags);
    }

    private File _scan4Dir;
}
