package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.nrg.dicom.dicomedit.TestUtils.put;
import static org.nrg.dicom.dicomedit.TestUtils.TestTag;
import static org.nrg.dicom.dicomedit.TestUtils.TestSeqTag;

public class TestHashUIDListFunction {
    @Test
    public void test1() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1                     = new TestTag( 0x0020000D, "1.2.3.444.555","2.25.151084009948217556417582685022853404996");
        TestTag t2                     = new TestTag( 0x0020000E, "1.2.3.444.555");
        TestSeqTag t3_0_t1      = new TestSeqTag(new int[]{0x00420010,0,0x0020000D}, "1.2.3.444.555.0", "2.25.21621800940148076483240768691790573264");
        TestSeqTag t3_0_t2      = new TestSeqTag(new int[]{0x00420010,0,0x0020000E}, "1.2.3.444.556.0");
        TestSeqTag t3_0_t3_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00421111,0,0x0020000D}, "1.2.3.444.555.0.0", "2.25.246763911536756469410628959877942162447");
        TestSeqTag t3_0_t3_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00421111,0,0x0020000E}, "1.2.3.444.556.0.0","2.25.198653654406668325403458358514032735383");
        TestSeqTag t3_0_t3_1_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00421111,1,0x0020000D}, "1.2.3.444.555.0.1", "2.25.94494675630523809322742234824423095000");
        TestSeqTag t3_0_t3_1_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00421111,1,0x0020000E}, "1.2.3.444.556.0.1","2.25.213666456057713920646985407178089025968");
        TestSeqTag t3_1_t1      = new TestSeqTag(new int[]{0x00420010,1,0x0020000D}, "1.2.3.444.555.1", "2.25.31826092823790271919652939240837527882");
        TestSeqTag t3_1_t2      = new TestSeqTag(new int[]{0x00420010,1,0x0020000E}, "1.2.3.444.556.1");
        TestSeqTag t4_0_t1      = new TestSeqTag(new int[]{0x00421111,0,0x0020000D}, "1.2.3.444.556.4.0","2.25.293257625700872661070396948471210666012");
        TestSeqTag t4_1_t1      = new TestSeqTag(new int[]{0x00421111,1,0x0020000E}, "1.2.3.444.556.4.1", "2.25.181945547936710404564267554687537362369");
        TestSeqTag t4_1_t2_0_t1 = new TestSeqTag(new int[]{0x00421111,1,0x00421111,0,0x0020000E}, "1.2.3.444.556.4.1","2.25.181945547936710404564267554687537362369");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3_0_t1);
        put( dobj, t3_0_t2);
        put( dobj, t3_1_t1);
        put( dobj, t3_1_t2);
        put( dobj, t3_0_t3_0_t1);
        put( dobj, t3_0_t3_0_t2);
        put( dobj, t3_0_t3_1_t1);
        put( dobj, t3_0_t3_1_t2);
        put( dobj, t4_0_t1);
        put( dobj, t4_1_t1);
        put( dobj, t4_1_t2_0_t1);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3_0_t1.initialValue, dobj.getString( t3_0_t1.tag));
        assertEquals( t3_0_t2.initialValue, dobj.getString( t3_0_t2.tag));
        assertEquals( t3_1_t1.initialValue, dobj.getString( t3_1_t1.tag));
        assertEquals( t3_1_t2.initialValue, dobj.getString( t3_1_t2.tag));
        assertEquals( t3_0_t3_0_t1.initialValue, dobj.getString( t3_0_t3_0_t1.tag));
        assertEquals( t3_0_t3_0_t2.initialValue, dobj.getString( t3_0_t3_0_t2.tag));
        assertEquals( t3_0_t3_1_t1.initialValue, dobj.getString( t3_0_t3_1_t1.tag));
        assertEquals( t3_0_t3_1_t2.initialValue, dobj.getString( t3_0_t3_1_t2.tag));
        assertEquals( t4_0_t1.initialValue, dobj.getString( t4_0_t1.tag));
        assertEquals( t4_1_t1.initialValue, dobj.getString( t4_1_t1.tag));
        assertEquals( t4_1_t2_0_t1.initialValue, dobj.getString( t4_1_t2_0_t1.tag));

        String script = "hashUIDList[*/(0020,000D), */(0042,1111)/(0020,000E)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t3_0_t1.postValue, dobj.getString( t3_0_t1.tag));
        assertEquals( t3_0_t2.postValue, dobj.getString( t3_0_t2.tag));
        assertEquals( t3_1_t1.postValue, dobj.getString( t3_1_t1.tag));
        assertEquals( t3_1_t2.postValue, dobj.getString( t3_1_t2.tag));
        assertEquals( t3_0_t3_0_t1.postValue, dobj.getString( t3_0_t3_0_t1.tag));
        assertEquals( t3_0_t3_0_t2.postValue, dobj.getString( t3_0_t3_0_t2.tag));
        assertEquals( t3_0_t3_1_t1.postValue, dobj.getString( t3_0_t3_1_t1.tag));
        assertEquals( t3_0_t3_1_t2.postValue, dobj.getString( t3_0_t3_1_t2.tag));
        assertEquals( t4_0_t1.postValue, dobj.getString( t4_0_t1.tag));
        assertEquals( t4_1_t1.postValue, dobj.getString( t4_1_t1.tag));
        assertEquals( t4_1_t2_0_t1.postValue, dobj.getString( t4_1_t2_0_t1.tag));

    }

    @Test
    public void testPrivate() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag pc = new TestTag( 0x00490010, "PrivateCreator","PrivateCreator");
        TestTag p1 = new TestTag( 0x00491010, "1.2.3.444.555", "2.25.151084009948217556417582685022853404996");
        TestTag p2 = new TestTag( 0x00491011, "1.2.3.444.555.666", "2.25.112080059686574174424340996853252886096");

        put( dobj, pc);
        put( dobj, p1);
        put( dobj, p2);

        assertEquals( pc.initialValue, dobj.getString( pc.tag));
        assertEquals( p1.initialValue, dobj.getString( p1.tag));
        assertEquals( p2.initialValue, dobj.getString( p2.tag));

        String script = "hashUIDList[(0049,{PrivateCreator}10), (0049,{PrivateCreator}11)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( pc.postValue, dobj.getString( pc.tag));
        assertEquals( p1.postValue, dobj.getString( p1.tag));
        assertEquals( p2.postValue, dobj.getString( p2.tag));
    }

    @Test
    public void testMultipleMatch() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag pc = new TestTag( 0x00420010, "1.2.3","1.2.3");
        TestTag p1 = new TestTag( 0x00421010, "1.2.3.444.555", "2.25.151084009948217556417582685022853404996");
        TestTag p2 = new TestTag( 0x00421011, "1.2.3.444.555.666", "2.25.112080059686574174424340996853252886096");

        put( dobj, pc);
        put( dobj, p1);
        put( dobj, p2);

        assertEquals( pc.initialValue, dobj.getString( pc.tag));
        assertEquals( p1.initialValue, dobj.getString( p1.tag));
        assertEquals( p2.initialValue, dobj.getString( p2.tag));

        String script = "hashUIDList[(0042,10XX)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( pc.postValue, dobj.getString( pc.tag));
        assertEquals( p1.postValue, dobj.getString( p1.tag));
        assertEquals( p2.postValue, dobj.getString( p2.tag));
    }

    @Test
    public void testValueMultiplicity() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x0008001a,
                "1.2.3.4.5\\1.2.3.4.5.6\\99.99999.999",
                "2.25.281097975800109040096884226991818420711\\2.25.262228503153619312954819897114011402300\\2.25.231023445675908772609000485091754869630");

        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));

        String script = "hashUIDList[(0008,001a)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
    }

    @Test
    public void test2() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag u1 = new TestTag(0x00080014, "0.0.0", "2.25.179381058945121467305106941903336095433");
        TestTag u2 = new TestTag(0x00080018, "1.2.3.4", "2.25.12977335313771255011876057319581175462");
        TestTag u3 = new TestTag(0x00081155, "1.3.1.1", "2.25.258602106693983065446800266265806686854");
        TestTag u4 = new TestTag(0x0020000E, "1.2.3", "2.25.148370922854076938159295778886134140470");
        TestTag u5 = new TestTag(0x0020000D, "1.2","2.25.171578026969864389535347314560684512280");
        TestTag m1 = new TestTag( 0x0008001a,
                "0.0.0\\1.2.3.4\\1.3.1.1",
                "2.25.179381058945121467305106941903336095433\\2.25.12977335313771255011876057319581175462\\2.25.258602106693983065446800266265806686854");
        put( dobj, u1);
        put( dobj, u2);
        put( dobj, u3);
        put( dobj, u4);
        put( dobj, u5);
        put( dobj, m1);
        assertEquals(u1.initialValue, dobj.getString(u1.tag));
        assertEquals(u2.initialValue, dobj.getString(u2.tag));
        assertEquals(u3.initialValue, dobj.getString(u3.tag));
        assertEquals(u4.initialValue, dobj.getString(u4.tag));
        assertEquals(u5.initialValue, dobj.getString(u5.tag));
        assertEquals(m1.initialValue, dobj.getString(m1.tag));

        String script = "tags_to_hash := {(0008,001a),(0008,0014),(0008,0018),(0008,1155),(0020,000E),(0020,000D) } \n" +
                "hashUIDList[tags_to_hash]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals(u1.postValue, dobj.getString(u1.tag));
        assertEquals(u2.postValue, dobj.getString(u2.tag));
        assertEquals(u3.postValue, dobj.getString(u3.tag));
        assertEquals(u4.postValue, dobj.getString(u4.tag));
        assertEquals(u5.postValue, dobj.getString(u5.tag));
        assertEquals(m1.postValue, dobj.getString(m1.tag));
    }

    public ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
