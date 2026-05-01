package org.nrg.dicom.mizer.visitors;

import junit.framework.TestCase;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.nrg.dicom.mizer.TestUtils.*;

public class OrphanPvtTagLocatorTest extends TestCase {
    @Test
    public void testEmpty() {
        TestTag pb1 = new TestTag(0x00090012, "pb1");
        TestTag pb1_1 = new TestTag(0x00091200, "pb1_1");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015}, "pb2_1");
        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00230020}, "pb3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, pb3);

        OrphanPvtTagLocator locator = new OrphanPvtTagLocator();

        List<List<Integer>> orphanTagArrays = locator.getOrphanPvtTags( dobj);

        assertTrue( orphanTagArrays.isEmpty());
    }

    @Test
    public void testOneOrphanInSeq() {
        TestTag pb1 = new TestTag(0x00090012, "pb1");
        TestTag pb1_1 = new TestTag(0x00091200, "pb1_1");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015}, "pb2_1");
//        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00230020}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232000}, "pb3_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, pb3_1);

        OrphanPvtTagLocator locator = new OrphanPvtTagLocator();

        List<List<Integer>> orphanTagArrays = locator.getOrphanPvtTags( dobj);

        assertTrue( orphanTagArrays.contains( Arrays.stream(pb3_1.tag).boxed().collect(Collectors.toList())));
    }

}