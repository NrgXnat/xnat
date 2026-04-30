/*
 * dicomtools: org.nrg.dicomtools.utilities.DicomUtilsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.utilities;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dcm.RequiredAttributeUnsetException;
import org.nrg.dcm.TestFiles;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicomtools.exceptions.AttributeVRMismatchException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomUtilsTest extends TestFiles {
    private File        _sample;
    private File        _sampleGz;
    private Attributes _dicomObject;

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
     * Test method for {@link DicomUtils#getStringRequired(DicomObjectI, int)}.
     */
    @Test(expected = RequiredAttributeUnsetException.class)
    public void testGetStringRequired() throws RequiredAttributeUnsetException {
        assertEquals("SIEMENS", DicomUtils.getStringRequired(_dicomObject, Tag.Manufacturer));
        DicomUtils.getStringRequired(_dicomObject, 0x00080071);
        fail("missed expected RequiredAttributeUnsetException");
    }

    @Test(expected = IOException.class)
    public void testReadFile() throws IOException {
        final Attributes o = DicomUtils.read(_sample);
        assertEquals(UID.MRImageStorage, o.getString(Tag.SOPClassUID));
        assertEquals("Hospital", o.getString(Tag.InstitutionName));

        final Attributes ogz = DicomUtils.read(_sampleGz);
        assertEquals(o, ogz);

        DicomUtils.read(new File("/no/such/file"));
        fail("expected IOException reading from nonexistent File");
    }

    @Test
    @Ignore
    public void testReadFileInteger() throws IOException {
        final Attributes dicomObject = DicomUtils.read(_sample, Tag.Manufacturer);
        assertEquals("SIEMENS", dicomObject.getString(Tag.Manufacturer));
        assertNull(dicomObject.getString(Tag.InstitutionName));
    }

    @Test
    public void testReadFileURI() throws IOException {
        final URI         uri = _sample.toURI();
        final Attributes  o   = DicomUtils.read(uri);
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
     * Test method for {@link DicomUtils#getString(DicomObjectI, int)}.
     */
    @Test
    @Ignore
    public void testGetString() throws Exception {
        final DicomObjectI d = DicomObjectFactory.newInstance();
        assertNull(DicomUtils.getString(d, Tag.ImageType));

        d.putStrings(Tag.ImageType, new String[]{"A", "B", "C"});
        assertEquals("A\\B\\C", DicomUtils.getString(d, Tag.ImageType));

        d.getAttributes().newSequence(Tag.InstitutionCodeSequence,0);
        try {
            DicomUtils.getString(d, Tag.InstitutionCodeSequence);
            fail("Missed expected AttributeVRMismatchException for element type SQ");
        } catch (AttributeVRMismatchException ignored) {}

        d.getAttributes().setBytes(0x00170001, VR.UN, "foo".getBytes());
        assertEquals("foo", DicomUtils.getString(d, 0x00170001));

        d.getAttributes().setInt(0x00170002, VR.AT, new int[]{0x01, 0x10, 0x1f, 0xff});
        assertEquals("1\\16\\31\\255", DicomUtils.getString(d, 0x00170002));

        d.putBytes(0x00170003, VR.OB.toString(), new byte[]{(byte)0x0d, (byte)0xe1, (byte)0xfe, (byte)0x1f});
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
     * Test method for {@link DicomUtils#getTransferSyntaxUID(DicomObjectI)}.
     */
    @Test
    public void testGetTransferSyntaxUID() {
        final DicomObjectI o1 = DicomObjectFactory.newInstance();
        o1.putString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRBigEndian);
        assertEquals(UID.ExplicitVRBigEndian, DicomUtils.getTransferSyntaxUID(o1));

        final DicomObjectI o2 = DicomObjectFactory.newInstance();
        assertEquals(UID.ImplicitVRLittleEndian, DicomUtils.getTransferSyntaxUID(o2));
    }

	/*
	 * TODO: tests for all the read methods
	 */

    /**
     * Test method for {@link DicomUtils#getDateTime(org.nrg.dicom.mizer.objects.DicomObjectI, int, int)}.
     */
    @Test
    public void testGetDateTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);

        final DicomObjectI o = DicomObjectFactory.newInstance();

        o.putString(Tag.StudyDate, VR.DA, "20110121");
        calendar.set(2011, Calendar.JANUARY, 21, 0, 0, 0);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        o.putString(Tag.StudyTime, VR.TM, "151415");
        calendar.set(2011, Calendar.JANUARY, 21, 15, 14, 15);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        calendar.set(Calendar.MILLISECOND, 927);
        o.putString(Tag.StudyTime, VR.TM, "151415.927");
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));

        o.delete(Tag.StudyDate);
        calendar.set(1970, Calendar.JANUARY, 1);
        assertEquals(calendar.getTime(), DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime));
    }

    @Test
    public void testUNValueAsString() throws AttributeVRMismatchException {
        final Attributes a = new Attributes();
        final int tag = 0x00491010;
        final byte[] chocolatez = new byte[]{67, 72, 79, 67, 79, 76, 65, 84, 69, 0};
        a.setBytes(tag, VR.UN, chocolatez);
        assertEquals("67\\72\\79\\67\\79\\76\\65\\84\\69\\0", DicomUtils.getString(a, tag));
    }

}
