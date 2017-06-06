/*
 * dicomtools: org.nrg.dicomtools.utilities.DicomUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.utilities;

import org.dcm4che2.data.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.RequiredAttributeUnsetException;
import org.nrg.dcm.TestFiles;
import org.nrg.dicomtools.exceptions.AttributeVRMismatchException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;

import static org.junit.Assert.*;

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
     * Test method for {@link DicomUtils#getStringRequired(DicomObject, int)}.
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
     * Test method for {@link DicomUtils#stripTrailingChars(StringBuilder, char)}.
     */
    @Test
    public void testStripTrailingChars() {
        assertEquals("x", DicomUtils.stripTrailingChars(new StringBuilder("xyy"), 'y').toString());
        assertEquals("", DicomUtils.stripTrailingChars(new StringBuilder("yy"), 'y').toString());
        assertEquals("yx", DicomUtils.stripTrailingChars(new StringBuilder("yxyyy"), 'y').toString());
        assertEquals("yx", DicomUtils.stripTrailingChars(new StringBuilder("yx"), 'y').toString());
        assertEquals("x", DicomUtils.stripTrailingChars("xyy", 'y'));
    }

    /**
     * Test method for {@link DicomUtils#getString(org.dcm4che2.data.DicomObject, int)}.
     */
    @Test
    public void testGetString() throws Exception {
        final DicomObject d = new BasicDicomObject();
        assertNull(DicomUtils.getString(d, Tag.ImageType));

        d.putStrings(Tag.ImageType, VR.CS, new String[]{"A", "B", "C"});
        assertEquals("A\\B\\C", DicomUtils.getString(d, Tag.ImageType));

        d.putSequence(Tag.InstitutionCodeSequence);
        try {
            DicomUtils.getString(d, Tag.InstitutionCodeSequence);
            fail("Missed expected AttributeVRMismatchException for element type SQ");
        } catch (AttributeVRMismatchException ignored) {}

        d.putBytes(0x00170001, VR.UN, "foo".getBytes());
        assertEquals("foo", DicomUtils.getString(d, 0x00170001));

        d.putInts(0x00170002, VR.AT, new int[]{0x01, 0x10, 0x1f, 0xff});
        assertEquals("1\\16\\31\\255", DicomUtils.getString(d, 0x00170002));

        d.putBytes(0x00170003, VR.OB, new byte[]{(byte)0x0d, (byte)0xe1, (byte)0xfe, (byte)0x1f});
        assertEquals("0D\\E1\\FE\\1F", DicomUtils.getString(d, 0x00170003));
    }

    /**
     * Test method for {@link DicomUtils#isValidUID(java.lang.CharSequence)}.
     */
    @Test
    public void testIsValidUID() {
        assertFalse(DicomUtils.isValidUID(null));
        assertFalse(DicomUtils.isValidUID(""));
        assertFalse(DicomUtils.isValidUID("a"));
        assertTrue(DicomUtils.isValidUID("0"));
        assertTrue(DicomUtils.isValidUID("1"));
        assertFalse(DicomUtils.isValidUID("01"));
        assertTrue(DicomUtils.isValidUID("10"));
        assertTrue(DicomUtils.isValidUID("0.1"));
        assertTrue(DicomUtils.isValidUID("0.12"));
        assertFalse(DicomUtils.isValidUID("0.x2"));
        assertTrue(DicomUtils.isValidUID("0.1234567890"));
        assertTrue(DicomUtils.isValidUID("0.1.2.3.4.5.6.7.8.9.10"));
        assertTrue(DicomUtils.isValidUID("0.1234567890.1234567890.1234567890.1234567890.1234567890.1234567"));
        assertFalse(DicomUtils.isValidUID("0.1234567890.1234567890.1234567890.1234567890.1234567890.12345678")); // too long
    }

    /**
     * Test method for {@link DicomUtils#getTransferSyntaxUID(org.dcm4che2.data.DicomObject)}.
     */
    @Test
    public void testGetTransferSyntaxUID() {
        final DicomObject o1 = new BasicDicomObject();
        o1.putString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRBigEndian);
        assertEquals(UID.ExplicitVRBigEndian, DicomUtils.getTransferSyntaxUID(o1));

        final DicomObject o2 = new BasicDicomObject();
        assertEquals(UID.ImplicitVRLittleEndian, DicomUtils.getTransferSyntaxUID(o2));
    }

	/*
	 * TODO: tests for all the read methods
	 */

    /**
     * Test method for {@link DicomUtils#getDateTime(org.dcm4che2.data.DicomObject, int, int)}.
     */
    @Test
    public void testGetDateTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);

        final DicomObject o = new BasicDicomObject();

        o.putString(Tag.StudyDate, VR.DA, "20110121");
        calendar.set(2011, Calendar.JANUARY, 21, 0, 0, 0);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        o.putString(Tag.StudyTime, VR.TM, "151415");
        calendar.set(2011, Calendar.JANUARY, 21, 15, 14, 15);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        calendar.set(Calendar.MILLISECOND, 927);
        o.putString(Tag.StudyTime, VR.TM, "151415.927");
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        o.remove(Tag.StudyDate);
        calendar.set(1970, Calendar.JANUARY, 1);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));
    }
}
