/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.nrg.dicom.dicomedit.TestUtils.*;

/**
 * Run tests of logic functions, functions in conditionals.
 *
 */
public class TestLogicFunctions {
    @Test
    public void testTrue() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "if( true[]) { (0010,0010) := \"true\"}";
        TestTag t1 = new TestTag(0x00100010, "false", "true");
        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
    }

    @Test
    public void testTrue2() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "true[]? (0010,0010) := \"true\": (0010,0010) := \"false\"";
        TestTag t1 = new TestTag(0x00100010, "false", "true");
        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
    }

    @Test
    public void testTrueIgnoresArguments() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "true[\"fubar\"]? (0010,0010) := \"true\": (0010,0010) := \"false\"";
        TestTag t1 = new TestTag(0x00100010, "false", "true");
        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
    }

    @Test
    public void testFalse() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "if( false[]) { (0010,0010) := \"true\"}";
        TestTag t1 = new TestTag(0x00100010, "false", "false");
        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
    }

    @Test
    public void testIsPresent() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00100010, "false", "true");
        TestTag t2 = new TestTag(0x00100020, "false", "true");
        put( dobj, t1);

        String script = "if( isPresent[(0010,0010)]) { (0010,0010) := \"true\"}";
        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();
        assertEquals( t1.postValue, dobj.getString(t1.tag));

        put( dobj, t1);
        script = "if( isPresent[ (0010,0020)]) { (0010,0010) := \"true\"}";
        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();
        assertEquals( "false", dobj.getString(t1.tag));

        put( dobj, t1);
        put( dobj, t2);
        script = "if( isPresent[ (0010,0020), (0010,0010)]) { (0010,0010) := \"true\"}";
        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();
        assertEquals( "true", dobj.getString(t1.tag));
    }

    @Test
    public void testIsPresentPrivate() throws MizerException {
        TestTag t1 = new TestTag(0x0044000b, "20100101");
        TestTag pc = new TestTag(0x20010010, "Philips Imaging DD 001");
        TestTag p1 = new TestTag(0x2001100c, "N");
        TestTag p2 = new TestTag(0x2001100e, "N");
        TestTag p3 = new TestTag(0x2001100f, "0");
        TestTag p4 = new TestTag(0x20011010, "N)");
        TestTag p5 = new TestTag(0x20011011, "0");
        TestTag p6 = new TestTag(0x20011012, "N");
        TestTag p7 = new TestTag(0x20011013, "1");

        TestTag t2 = new TestTag(0x00120010, "CORRECT");
        TestTag t3 = new TestTag(0x00120020, "CORRECT");
        TestTag t4 = new TestTag(0x00120020, "CORRECT");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, t1);
        put( dobj, pc);
        put( dobj, p1);
        put( dobj, p2);
        put( dobj, p3);
        put( dobj, p4);
        put( dobj, p5);
        put( dobj, p6);
        put( dobj, p7);

        String script = "isPresent[(2001,{Philips Imaging DD 001}0c)] ? (0012,0010) := \"CORRECT\" : (0012,0010) := \"INCORRECT\"\n" +
                "isPresent[(2001,{Philips Imaging DD 001}0d)] ? (0012,0020) := \"INCORRECT\" : (0012,0020) := \"CORRECT\"\n" +
                "if (isPresent[(2001,{NOPEEEEEEEE}0c)]) {\n" +
                "    (0012,0021) := \"INCORRECT\"\n" +
                "} else {\n" +
                "    (0012,0021) := \"CORRECT\"\n" +
                "}";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t3.postValue, dobj.getString( t3.tag));
        assertEquals( t4.postValue, dobj.getString( t4.tag));
    }

    @Test
    public void testIsPresentSequenceItem() throws MizerException {
        TestSeqTag s1_0 = new TestSeqTag(new int[] {0x00189239,0,0x00100010}, "foo");
        TestTag t2 = new TestTag(0x00120020, "CORRECT");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, s1_0);

        String script = "isPresent[(0018,9239)[0]] ? (0012,0020) := \"CORRECT\" : (0012,0020) := \"INCORRECT\"";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( s1_0.postValue, dobj.getString( s1_0.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
    }

    @Test
    public void testIsPresentMissingSeqItem() throws MizerException {
        TestTag t2 = new TestTag(0x00120020, "CORRECT");
        // Add one item
        TestSeqTag s1_0 = new TestSeqTag(new int[] {0x00189239,0,0x00100010}, "foo");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, s1_0);

        // Test presence of a second item is false.
        String script = "isPresent[(0018,9239)[1]] ? (0012,0020) := \"INCORRECT\" : (0012,0020) := \"CORRECT\"";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t2.postValue, dobj.getString( t2.tag));
    }

}
