package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.nrg.dicom.dicomedit.TestUtils.TestSeqTag;

public class DupPrivateBlockTerminatorTest {

    @Test
    public void test() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( 0x00100010, "snafu");
        dobj.putString( 0x00190010, "Mine");
        dobj.putString( 0x00191000, "oh boy");

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));

        DupPrivateBlockTerminator visitor = new DupPrivateBlockTerminator();
        visitor.visit( dobj);

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
    }

    @Test
    public void testDup() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( 0x00100010, "snafu");
        dobj.putString( 0x00190010, "Mine");
        dobj.putString( 0x00191000, "oh boy");
        dobj.putString( 0x001900E1, "Mine");
        dobj.putString( 0x0019E100, "oh boy");

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
        assertEquals( "Mine", dobj.getString( 0x001900E1));
        assertEquals( "oh boy", dobj.getString( 0x0019E100));

        DupPrivateBlockTerminator visitor = new DupPrivateBlockTerminator();
        visitor.visit( dobj);

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
        assertFalse( dobj.contains( 0x001900E1));
        assertFalse( dobj.contains( 0x0019E100));
    }

    @Test
    public void testSequenceList() {
        int[] s00 = {0x00120064,0,0x00080100};
        int[] s01 = {0x00120064,0,0x00080101};
        int[] s02 = {0x00120064,0,0x00290010};
        int[] s03 = {0x00120064,0,0x00291011};
        int[] s02d = {0x00120064,0,0x00290011};
        int[] s03d = {0x00120064,0,0x00291111};
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( s00, "item 0 1");
        dobj.putString( s01, "item 0 2");
        dobj.putString( s02, "item 0 pcid");
        dobj.putString( s03, "item 0 p1 1");
        dobj.putString( s02d, "item 0 pcid");
        dobj.putString( s03d, "item 0 p1 2");

        assertEquals( "item 0 1", dobj.getString( s00));
        assertEquals( "item 0 2", dobj.getString( s01));
        assertEquals( "item 0 pcid", dobj.getString( s02));
        assertEquals( "item 0 p1 1", dobj.getString( s03));
        assertEquals( "item 0 pcid", dobj.getString( s02d));
        assertEquals( "item 0 p1 2", dobj.getString( s03d));

        DupPrivateBlockTerminator visitor = new DupPrivateBlockTerminator();
        visitor.visit( dobj);

        assertEquals( "item 0 1", dobj.getString( s00));
        assertEquals( "item 0 2", dobj.getString( s01));
        assertEquals( "item 0 pcid", dobj.getString( s02));
        assertEquals( "item 0 p1 1", dobj.getString( s03));
        assertFalse( dobj.contains( s02d));
        assertFalse( dobj.contains( s03d));
    }

    @Test
    public void testSequenceList2() {
        TestSeqTag  s00 = new TestSeqTag(  new int[]{0x00120064,0,0x00080100}, "item 0 1");
        TestSeqTag  s01 = new TestSeqTag(  new int[]{0x00120064,0,0x00080101}, "item 0 2");
        TestSeqTag  s02 = new TestSeqTag(  new int[]{0x00120064,0,0x00290010}, "item 0 pcid");
        TestSeqTag  s03 = new TestSeqTag(  new int[]{0x00120064,0,0x00291011}, "item 0 p1 1");
        TestSeqTag  s02d = new TestSeqTag( new int[]{0x00120064,0,0x00290011}, "item 0 pcid");
        TestSeqTag  s03d = new TestSeqTag( new int[]{0x00120064,0,0x00291111}, "item 0 p1 2");
        TestSeqTag  s10 = new TestSeqTag(  new int[]{0x00120064,1,0x00080100}, "item 1 1");
        TestSeqTag  s11 = new TestSeqTag(  new int[]{0x00120064,1,0x00080101}, "item 1 2");
        TestSeqTag  s12 = new TestSeqTag(  new int[]{0x00120064,1,0x00290010}, "item 1 pcid");
        TestSeqTag  s13 = new TestSeqTag(  new int[]{0x00120064,1,0x00291011}, "item 1 p1 1");
        TestSeqTag  s12d = new TestSeqTag( new int[]{0x00120064,1,0x00290011}, "item 1 pcid");
        TestSeqTag  s13d = new TestSeqTag( new int[]{0x00120064,1,0x00291111}, "item 1 p1 2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( s00.tag, s00.initialValue);
        dobj.putString( s01.tag, s01.initialValue);
        dobj.putString( s02.tag, s02.initialValue);
        dobj.putString( s03.tag, s03.initialValue);
        dobj.putString( s02d.tag, s02d.initialValue);
        dobj.putString( s03d.tag, s03d.initialValue);
        dobj.putString( s10.tag, s10.initialValue);
        dobj.putString( s11.tag, s11.initialValue);
        dobj.putString( s12.tag, s12.initialValue);
        dobj.putString( s13.tag, s13.initialValue);
        dobj.putString( s12d.tag, s12d.initialValue);
        dobj.putString( s13d.tag, s13d.initialValue);

        assertEquals( s00.initialValue, dobj.getString( s00.tag));
        assertEquals( s01.initialValue, dobj.getString( s01.tag));
        assertEquals( s02.initialValue, dobj.getString( s02.tag));
        assertEquals( s03.initialValue, dobj.getString( s03.tag));
        assertEquals( s02d.initialValue, dobj.getString( s02d.tag));
        assertEquals( s03d.initialValue, dobj.getString( s03d.tag));
        assertEquals( s10.initialValue, dobj.getString( s10.tag));
        assertEquals( s11.initialValue, dobj.getString( s11.tag));
        assertEquals( s12.initialValue, dobj.getString( s12.tag));
        assertEquals( s13.initialValue, dobj.getString( s13.tag));
        assertEquals( s12d.initialValue, dobj.getString( s12d.tag));
        assertEquals( s13d.initialValue, dobj.getString( s13d.tag));

        DupPrivateBlockTerminator visitor = new DupPrivateBlockTerminator();
        visitor.visit( dobj);

        assertEquals( s00.postValue, dobj.getString( s00.tag));
        assertEquals( s01.postValue, dobj.getString( s01.tag));
        assertEquals( s02.postValue, dobj.getString( s02.tag));
        assertEquals( s03.postValue, dobj.getString( s03.tag));
        assertFalse( dobj.contains( s02d.tag));
        assertFalse( dobj.contains( s03d.tag));
        assertEquals( s10.postValue, dobj.getString( s10.tag));
        assertEquals( s11.postValue, dobj.getString( s11.tag));
        assertEquals( s12.postValue, dobj.getString( s12.tag));
        assertEquals( s13.postValue, dobj.getString( s13.tag));
        assertFalse( dobj.contains( s12d.tag));
        assertFalse( dobj.contains( s13d.tag));
    }

    @Test
    public void testRepeatIDs() {
        TestUtils.TestTag p9 = new TestUtils.TestTag( 0x00090010, "my id");
        TestUtils.TestTag p9_1 = new TestUtils.TestTag( 0x00091001, "p91_1");
        TestUtils.TestTag p11 = new TestUtils.TestTag( 0x00110010, "my id");
        TestUtils.TestTag p11_1 = new TestUtils.TestTag( 0x00111001, "p11_1");
        TestUtils.TestTag p92 = new TestUtils.TestTag( 0x00090011, "my id2");
        TestUtils.TestTag p92_1 = new TestUtils.TestTag( 0x00091101, "p92_1");
        TestUtils.TestTag p92d = new TestUtils.TestTag( 0x00090012, "my id2");
        TestUtils.TestTag p92d_1 = new TestUtils.TestTag( 0x00091201, "p92_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p9.tag, p9.initialValue);
        dobj.putString( p9_1.tag, p9_1.initialValue);
        dobj.putString( p11.tag, p11.initialValue);
        dobj.putString( p11_1.tag, p11_1.initialValue);
        dobj.putString( p92.tag, p92.initialValue);
        dobj.putString( p92_1.tag, p92_1.initialValue);
        dobj.putString( p92d.tag, p92d.initialValue);
        dobj.putString( p92d_1.tag, p92d_1.initialValue);

        assertEquals( p9.initialValue, dobj.getString( p9.tag));
        assertEquals( p9_1.initialValue, dobj.getString( p9_1.tag));
        assertEquals( p11.initialValue, dobj.getString( p11.tag));
        assertEquals( p11_1.initialValue, dobj.getString( p11_1.tag));
        assertEquals( p92.initialValue, dobj.getString( p92.tag));
        assertEquals( p92_1.initialValue, dobj.getString( p92_1.tag));
        assertEquals( p92d.initialValue, dobj.getString( p92d.tag));
        assertEquals( p92d_1.initialValue, dobj.getString( p92d_1.tag));

        DupPrivateBlockTerminator visitor = new DupPrivateBlockTerminator();
        visitor.visit( dobj);

        assertEquals( p9.postValue, dobj.getString( p9.tag));
        assertEquals( p9_1.postValue, dobj.getString( p9_1.tag));
        assertEquals( p11.postValue, dobj.getString( p11.tag));
        assertEquals( p11_1.postValue, dobj.getString( p11_1.tag));
        assertEquals( p92.postValue, dobj.getString( p92.tag));
        assertEquals( p92_1.postValue, dobj.getString( p92_1.tag));
        assertFalse( dobj.contains( p92d.tag));
        assertFalse( dobj.contains( p92d_1.tag));
    }

}