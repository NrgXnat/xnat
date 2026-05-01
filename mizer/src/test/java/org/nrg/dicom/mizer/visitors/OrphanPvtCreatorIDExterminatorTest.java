package org.nrg.dicom.mizer.visitors;

import junit.framework.TestCase;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.nrg.dicom.mizer.TestUtils.*;

public class OrphanPvtCreatorIDExterminatorTest extends TestCase {

    /**
     * <pre>
     * (0010,0010) LO #2 [t1] Patient’s Name                 retain
     * (0019,0011) LO #4 [pb1] Private Creator Data Element  retain
     * (0019,0012) LO #4 [pb2] Private Creator Data Element  delete
     * (0019,1101) LO #6 [pb1_1] ?                           retain
     * (0019,1102) LO #6 [pb1_2] ?                           retain
     * </pre>
     */
    @Test
    public void testSimple() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag pb1 = new TestTag(0x00190011, "pb1");
        TestTag pb1_1 = new TestTag(0x00191101, "pb1_1");
        TestTag pb1_2 = new TestTag(0x00191102, "pb1_2");
        TestTag pb2 = new TestTag(0x00190012, "pb2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, pb1);
        put(dobj, pb1_1);
        put(dobj, pb1_2);
        put(dobj, pb2);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb1_2.tag));
        assertTrue( dobj.contains( pb2.tag));

        OrphanPvtCreatorIDExterminator exterminator = new OrphanPvtCreatorIDExterminator();
        exterminator.visit( dobj);

        assertTrue( dobj.contains(t1.tag));
        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb1_2.tag));
        assertFalse( dobj.contains(pb2.tag));
    }

    /**
     * <pre>
     * (0009,0012) LO #4 [pb1] Private Creator Data Element        retain
     * (0009,1200) SQ #-1 [1 item] ?                               retain
     *   >ITEM #1:
     *   >(0023,0020) LO #4 [pb2] Private Creator Data Element     retain
     *   >(0023,2015) SQ #-1 [1 item] ?                            retain
     *     >>ITEM #1:
     *     >>(0023,0020) LO #4 [pb3] Private Creator Data Element  delete
     * </pre>
     */
    @Test
    public void testNestedPvtSeq() {
        TestTag pb1 = new TestTag(0x00090012, "pb1");
        TestTag pb1_1 = new TestTag(0x00091200, "pb1_1");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015}, "pb2_1");
        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,0,0x00230020}, "pb3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, pb3);

        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(pb3.tag));

        OrphanPvtCreatorIDExterminator exterminator = new OrphanPvtCreatorIDExterminator();
        exterminator.visit( dobj);

        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertFalse( dobj.contains(pb3.tag));
    }

    /**
     * <pre>
     * (0009,0012) LO #4 [pb1] Private Creator Data Element        retain
     * (0009,1200) SQ #-1 [1 item] ?                               retain
     *   >ITEM #1:
     *   >(0023,0020) LO #4 [pb2] Private Creator Data Element     retain
     *   >(0023,2015) SQ #-1 [3 items] ?                           retain
     *     >>ITEM #1:
     *     >>(0023,0020) LO #4 [pb3] Private Creator Data Element  retain
     *     >>(0023,2001) LO #6 [pb3_1] ?                           retain
     *     >>ITEM #2:
     *     >>(0023,0020) LO #4 [pb4] Private Creator Data Element  delete
     *     >>ITEM #3:
     *     >>(0023,0020) LO #4 [pb5] Private Creator Data Element  retain
     *     >>(0023,2001) LO #6 [pb5_1] ?                           retain
     * </pre>
     */
    @Test
    public void testNestedPvtSeqMultiItems() {
        TestTag pb1 = new TestTag(0x00090012, "pb1");
        TestTag pb1_1 = new TestTag(0x00091200, "pb1_1");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015}, "pb2_1");
        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,0,0x00230020}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,0,0x00232001}, "pb3_1");
        TestSeqTag pb4 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,1,0x00230020}, "pb4");
//        TestSeqTag pb4_1 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,1,0x00232001}, "pb4_1");
        TestSeqTag pb5 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,2,0x00230020}, "pb5");
        TestSeqTag pb5_1 = new TestSeqTag( new int[] {0x00091200,0,0x000232015,2,0x00232001}, "pb5_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, pb3);
        put( dobj, pb3_1);
        put( dobj, pb4);
        put( dobj, pb5);
        put( dobj, pb5_1);

        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertTrue( dobj.contains(pb4.tag));
        assertTrue( dobj.contains(pb5.tag));
        assertTrue( dobj.contains(pb5_1.tag));

        OrphanPvtCreatorIDExterminator exterminator = new OrphanPvtCreatorIDExterminator();
        exterminator.visit( dobj);

        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertFalse( dobj.contains(pb4.tag));
        assertTrue( dobj.contains(pb5.tag));
        assertTrue( dobj.contains(pb5_1.tag));
    }

    /**
     * <pre>
     * (0010,0010) LO #2 [t1] Patient’s Name                     retain
     * (0019,0011) LO #4 [pb1] Private Creator Data Element      retain
     * (0019,0012) LO #4 [pb2] Private Creator Data Element      retain
     * (0019,1101) LO #6 [pb1_1] ?                               retain
     * (0019,1102) LO #6 [pb1_2] ?                               retain
     * (0019,1201) LO #6 [pb1_1] ?                               retain
     * (0019,1202) SQ #-1 [1 item] ?                             retain
     *   >ITEM #1:
     *   >(0023,0013) LO #4 [pb3] Private Creator Data Element   retain
     *   >(0023,0014) LO #4 [pb4] Private Creator Data Element   delete
     *   >(0023,1301) LO #6 [pb3_1] ?                            retain
     * </pre>
     */
    @Test
    public void testPvtTagInPvtSeq() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag pb1 = new TestTag(0x00190011, "pb1");
        TestTag pb1_1 = new TestTag(0x00191101, "pb1_1");
        TestTag pb1_2 = new TestTag(0x00191102, "pb1_2");
        TestTag pb2 = new TestTag(0x00190012, "pb2");
        TestTag pb2_1 = new TestTag(0x00191201, "pb1_1");
        TestTag pb2_2 = new TestTag(0x00191202, "pb1_2");
        TestSeqTag pb3 =   new TestSeqTag( new int[] {0x00191202,0,0x00230013}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00191202,0,0x00231301}, "pb3_1");
        TestSeqTag pb4 =   new TestSeqTag( new int[] {0x00191202,0,0x00230014}, "pb4");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, t1);
        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb1_2);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, pb2_2);
        put( dobj, pb3);
        put( dobj, pb3_1);
        put( dobj, pb4);

        assertTrue( dobj.contains(t1.tag));
        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb1_2.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(pb2_2.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertTrue( dobj.contains(pb4.tag));

        OrphanPvtCreatorIDExterminator exterminator = new OrphanPvtCreatorIDExterminator();
        exterminator.visit( dobj);

        assertTrue( dobj.contains(t1.tag));
        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb1_2.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(pb2_2.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertFalse( dobj.contains(pb4.tag));
    }

    /**
     * <pre>
     * (0010,0010) LO #2 [t1] Patient’s Name                     retain
     * (0019,0011) LO #4 [pb1] Private Creator Data Element      retain
     * (0019,0012) LO #4 [pb2] Private Creator Data Element      retain
     * (0019,1101) LO #6 [pb1_1] ?                               retain
     * (0019,1102) LO #6 [pb1_2] ?                               retain
     * (0019,1201) LO #6 [pb1_1] ?                               retain
     * (0040,0040) SQ #-1 [1 item] ?                             retain
     *   >ITEM #1:
     *   >(0023,0013) LO #4 [pb3] Private Creator Data Element   retain
     *   >(0023,0014) LO #4 [pb4] Private Creator Data Element   delete
     *   >(0023,1301) LO #6 [pb3_1] ?                            retain
     * </pre>
     */
    @Test
    public void testPvtTagInPublicSeq() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag pb1 = new TestTag(0x00190011, "pb1");
        TestTag pb1_1 = new TestTag(0x00191101, "pb1_1");
        TestTag pb1_2 = new TestTag(0x00191102, "pb1_2");
        TestTag pb2 = new TestTag(0x00190012, "pb2");
        TestTag pb2_1 = new TestTag(0x00191201, "pb1_1");
        TestTag t2 = new TestTag(0x00400040, "t2");
        TestSeqTag pb3 =   new TestSeqTag( new int[] {0x00400040,0,0x00230013}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00400040,0,0x00231301}, "pb3_1");
        TestSeqTag pb4 =   new TestSeqTag( new int[] {0x00400040,0,0x00230014}, "pb4");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, t1);
        put( dobj, pb1);
        put( dobj, pb1_1);
        put( dobj, pb1_2);
        put( dobj, pb2);
        put( dobj, pb2_1);
        put( dobj, t2);
        put( dobj, pb3);
        put( dobj, pb3_1);
        put( dobj, pb4);

        assertTrue( dobj.contains(t1.tag));
        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb1_2.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(t2.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertTrue( dobj.contains(pb4.tag));

        OrphanPvtCreatorIDExterminator exterminator = new OrphanPvtCreatorIDExterminator();
        exterminator.visit( dobj);

        assertTrue( dobj.contains(t1.tag));
        assertTrue( dobj.contains(pb1.tag));
        assertTrue( dobj.contains(pb1_1.tag));
        assertTrue( dobj.contains(pb1_2.tag));
        assertTrue( dobj.contains(pb2.tag));
        assertTrue( dobj.contains(pb2_1.tag));
        assertTrue( dobj.contains(t2.tag));
        assertTrue( dobj.contains(pb3.tag));
        assertTrue( dobj.contains(pb3_1.tag));
        assertFalse( dobj.contains(pb4.tag));
    }

}