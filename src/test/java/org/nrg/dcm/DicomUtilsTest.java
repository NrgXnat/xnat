/*
 * dicomtools: org.nrg.dcm.DicomUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomUtilsTest extends TestFiles {
    private File        _sample;
    private File        _sampleGz;
    private DicomObject _dicomObject;

    @Before
    public void setUp() throws IOException {
        _sample = copySampleFileToTestTarget(_sampleDicomFile);
        _sampleGz = copySampleFileToTestTarget(_sampleDicomFileGzip);
        _dicomObject = DicomUtils.read(_sample);
    }

    @After
    public void tearDown() {
        if (null != _sample) {
            _sample.delete();
        }
        if (null != _sampleGz) {
            _sampleGz.delete();
        }
    }

    /**
     * Test method for {@link org.nrg.dcm.DicomUtils#getStringRequired(org.dcm4che2.data.DicomObject, int)}.
     */
    @Test(expected = RequiredAttributeUnsetException.class)
    public void testGetStringRequired() throws RequiredAttributeUnsetException {
        assertEquals("SIEMENS", DicomUtils.getStringRequired(_dicomObject, Tag.Manufacturer));
        DicomUtils.getStringRequired(_dicomObject, 0x00080071);
        fail("missed expected RequiredAttributeUnsetException");
    }

    @Test(expected = IOException.class)
    public void testReadFile() throws IOException {
        final DicomObject o = DicomUtils.read(_sample);
        assertEquals(UID.MRImageStorage, o.getString(Tag.SOPClassUID));
        assertEquals("Hospital", o.getString(Tag.InstitutionName));

        final DicomObject ogz = DicomUtils.read(_sampleGz);
        assertEquals(o, ogz);

        DicomUtils.read(new File("/no/such/file"));
        fail("expected IOException reading from nonexistent File");
    }

    @Test
    public void testReadFileInteger() throws IOException {
        final DicomObject dicomObject = DicomUtils.read(_sample, Tag.Manufacturer);
        assertEquals("SIEMENS", dicomObject.getString(Tag.Manufacturer));
        assertNull(dicomObject.getString(Tag.InstitutionName));
    }

    @Test
    public void testReadFileURI() throws IOException {
        final URI         uri = _sample.toURI();
        final DicomObject o   = DicomUtils.read(uri);
        assertEquals(UID.MRImageStorage, o.getString(Tag.SOPClassUID));
    }

    /**
     * Test method for {@link org.nrg.dcm.DicomUtils#stripTrailingChars(java.lang.StringBuilder, char)}.
     */
    @Test
    public void testStripTrailingChars() {
        assertEquals("x", DicomUtils.stripTrailingChars(new StringBuilder("xyy"), 'y').toString());
        assertEquals("", DicomUtils.stripTrailingChars(new StringBuilder("yy"), 'y').toString());
        assertEquals("yx", DicomUtils.stripTrailingChars(new StringBuilder("yxyyy"), 'y').toString());
        assertEquals("yx", DicomUtils.stripTrailingChars(new StringBuilder("yx"), 'y').toString());

        assertEquals("x", DicomUtils.stripTrailingChars("xyy", 'y'));
    }
}
