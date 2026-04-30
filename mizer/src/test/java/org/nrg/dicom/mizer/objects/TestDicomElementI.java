package org.nrg.dicom.mizer.objects;

import org.junit.Test;
import static org.junit.Assert.*;
import org.nrg.dicom.mizer.TestUtils.TestTag;

import static org.nrg.dicom.mizer.TestUtils.put;

public class TestDicomElementI {
    @Test
    public void testGetValueAsString() {
        final TestTag t1 = new TestTag(0x80008, "ORIGINAL\\TEST\\PASSING");
        final TestTag t2 = new TestTag(0x80012, "20251014");

        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2);

        final DicomElementI de1 = dobj.getElement(t1.tag);
        assertEquals(t1.initialValue, de1.getValueAsString());
        final DicomElementI de2 = dobj.getElement(t2.tag);
        assertEquals(t2.initialValue, de2.getValueAsString());
     }

    @Test
    public void testGetStrings() {
        final TestTag t1 = new TestTag(0x80008, "ORIGINAL\\TEST\\PASSING");
        final TestTag t2 = new TestTag(0x80012, "20251014");

        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2);

        final DicomElementI de1 = dobj.getElement(t1.tag);
        assertArrayEquals(new String[]{"ORIGINAL", "TEST", "PASSING"}, de1.getStrings());
        final DicomElementI de2 = dobj.getElement(t2.tag);
        assertArrayEquals(new String[]{"20251014"}, de2.getStrings());
    }
}
