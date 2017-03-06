/*
 * DicomDB: org.nrg.dcm.AttrAdapterTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.dcm4che2.data.Tag;
import org.junit.Before;
import org.junit.Test;
import org.nrg.attr.*;
import org.nrg.util.FileURIOpener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;


@SuppressWarnings("unchecked")
public class AttrAdapterTest {
    private final static DicomAttributeIndex STUDY_DATE = new FixedDicomAttributeIndex(Tag.StudyDate);
    private final static DicomAttributeIndex STUDY_TIME = new FixedDicomAttributeIndex(Tag.StudyTime);
    private final static DicomAttributeIndex PIXEL_SPACING = new FixedDicomAttributeIndex(Tag.PixelSpacing);
    private final static DicomAttributeIndex SLICE_THICKNESS = new FixedDicomAttributeIndex(Tag.SliceThickness);
    private final static DicomAttributeIndex SERIES_NUMBER = new FixedDicomAttributeIndex(Tag.SeriesNumber);
    private final static Collection<DicomAttributeIndex> tags = Arrays.asList(STUDY_DATE, STUDY_TIME, PIXEL_SPACING, SLICE_THICKNESS, SERIES_NUMBER);

    private DicomMetadataStore fs;

    @Before
    public void setUp() throws IOException, SQLException, URISyntaxException {
        fs = EnumeratedMetadataStore.createHSQLDBBacked(tags, FileURIOpener.getInstance());
        final URL resource = getClass().getClassLoader().getResource("dicom");
        assertNotNull(resource);
        fs.add(Collections.singleton(resource.toURI()));
    }

        @Test
    public final void testAttrAdapter() {
        AttrAdapter aa = new AttrAdapter(fs);
        assertNotNull(aa);
    }

    @Test
    public final void testAddAttr() {
        AttrAdapter aa = new AttrAdapter(fs);
        aa.add(new TestAttrDef.Text("just date", Tag.StudyDate));
    }

    @Test
    public final void testGetValuesGiven() throws ExtAttrException {
        final Map<ExtAttrDef<DicomAttributeIndex>, Throwable> failures = Maps.newHashMap();
        final AttrAdapter adapter = new AttrAdapter(fs);
        adapter.add(new TestAttrDef.Abstract<String>("voxelRes",
                PIXEL_SPACING, SLICE_THICKNESS) {
            public String start() {
                return null;
            }

            public String foldl(final String a, final Map<? extends DicomAttributeIndex, ? extends String> m)
                    throws NoUniqueValueException {
                final String vrxy = m.get(PIXEL_SPACING).replaceAll("\\\\", ", ");
                final String v = vrxy + ", " + m.get(SLICE_THICKNESS);
                if (null == a || a.equals(v)) {
                    return v;
                } else {
                    throw new NoUniqueValueException(getName(), new String[]{a, v});
                }
            }

            public Iterable<ExtAttrValue> apply(final String value) {
                return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(getName(), value));
            }
        });
        adapter.add(new TestAttrDef.Empty("empty"));
        Map<DicomAttributeIndex, String> scanSpec = Maps.newHashMap();
        scanSpec.put(SERIES_NUMBER, "4");
        List<ExtAttrValue> values = adapter.getValuesGiven(scanSpec, failures);
        assertTrue(failures.isEmpty());

        assertNotNull(values);
        assertEquals(2, values.size());
        final ExtAttrValue value = values.get(0);
        assertEquals("voxelRes", value.getName());
        assertEquals("1, 1, 1", value.getText());
        assertEquals("empty", values.get(1).getName());
    }

    @Test
    public final void testGetValues() throws ExtAttrException {
        final Map<ExtAttrDef<DicomAttributeIndex>, Throwable> failures = Maps.newHashMap();

        final AttrAdapter aa = new AttrAdapter(fs);
        aa.add(new TestAttrDef.Abstract<String>("date-and-time", STUDY_DATE, STUDY_TIME) {
            public String start() {
                return null;
            }

            public String foldl(final String a, final Map<? extends DicomAttributeIndex, ? extends String> m)
                    throws NoUniqueValueException {
                final String v = m.get(STUDY_DATE) + " at " + m.get(STUDY_TIME);
                if (null == a || a.equals(v)) {
                    return v;
                } else {
                    throw new NoUniqueValueException(getName(), new String[]{a, v});
                }
            }

            public Iterable<ExtAttrValue> apply(final String value) {
                return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(getName(), value));
            }
        });
        aa.add(new TestAttrDef.Empty("empty"));
        final List<ExtAttrValue> values = aa.getValues(failures);
        assertTrue(failures.isEmpty());

        assertNotNull(values);
        assertEquals(2, values.size());
        final ExtAttrValue value = values.get(0);
        assertEquals("date-and-time", value.getName());
        assertEquals("20061214 at 091206.156000", value.getText());
        assertEquals("empty", values.get(1).getName());
    }


    @Test
    public final void testGetMultipleValues() throws ExtAttrException {
        final Map<ExtAttrDef<DicomAttributeIndex>, Throwable> failures = Maps.newHashMap();
        final AttrAdapter adapter = new AttrAdapter(fs);
        adapter.add(MultiValueAttrDef.wrap(new TestAttrDef.Text("series number", SERIES_NUMBER)));
        adapter.add(new TestAttrDef.Empty("empty"));
        Map<DicomAttributeIndex, String> scanSpec = Maps.newHashMap();
        scanSpec.put(STUDY_DATE, "20061214");
        final List<ExtAttrValue> values = adapter.getValuesGiven(scanSpec, failures);
        assertNotNull(values);

        assertEquals(4, values.size());
        assertEquals(3, Collections2.filter(values, new Predicate<ExtAttrValue>() {
            public boolean apply(final ExtAttrValue v) {
                return "series number".equals(v.getName());
            }
        }).size());
    }
}
