/*
 * DicomEdit: TestAssignmentStatement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.dicomedit.TestUtils.TestSeqTag;
import org.nrg.dicom.dicomedit.TestUtils.TestTag;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.put;

/**
 * Run tests of assignment-if-exists statements.
 *
 */
public class TestAssignIfExistsStatement {

    @Test
    public void testAssignToNonExistingPublicTag() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");

        put( dobj, t1);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertFalse( dobj.contains(t2.tag));

        String script = "(0010,0010) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertFalse( dobj.contains(t2.tag));
    }

    @Test
    public void testAssignToExistingPublicTag() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName", "snafu");

        put( dobj, t1);
        put( dobj, t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));

        String script = "(0010,0010) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
    }

    @Test()
    public void testAssignFromPluralTagpath() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00080080, "Some Institution");
        TestTag t2 = new TestTag(0x00100010, "PatientName", "snafu");

        put(dobj, t1);
        put(dobj, t2);

        assertEquals(t1.initialValue, dobj.getString(t1.tag));
        assertEquals(t2.initialValue, dobj.getString(t2.tag));

        String script = "(0010,0010) ?= +/(0010,0010)";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        final AnonymizationResult result = sa.apply(dobj);

        assertTrue( result instanceof AnonymizationResultError);
        assertTrue( result.getMessages().get(0).contains( "Path matching multiple tags not allowed as term"));
    }

    @Test
    public void testAssignToExistingSequenceItem() throws MizerException {
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t1");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t1", "snafu");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, t1_0_t1);
        put( dobj, t1_1_t1);

        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));

        String script = "(0042,0010)[1]/(0010,0010) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
    }

    @Test
    public void testAssignToNonExistingSequenceItem() throws MizerException {
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t1");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t1");
        TestSeqTag t1_2_t1 = new TestSeqTag(new int[]{0x00420010,2,0x00100010}, "t1_2_t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, t1_0_t1);
        put( dobj, t1_1_t1);

        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertFalse(dobj.contains( t1_2_t1.tag));

        String script = "(0042,0010)[2]/(0010,0010) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertFalse(dobj.contains( t1_2_t1.tag));
    }

    @Test
    public void testAssignToExistingPrivateTag() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t = new TestTag( 0x00100010, "t");
        TestTag pc = new TestTag( 0x00190010, "GE creator ID");
        TestTag p1 = new TestTag( 0x00191022, "p1", "snafu");

        put( dobj, t);
        put( dobj, pc);
        put( dobj, p1);

        assertEquals( t.initialValue, dobj.getString( t.tag));
        assertEquals( pc.initialValue, dobj.getString( pc.tag));
        assertEquals( p1.initialValue, dobj.getString( p1.tag));

        String script = "(0019,{GE creator ID}22) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t.postValue, dobj.getString( t.tag));
        assertEquals( pc.postValue, dobj.getString( pc.tag));
        assertEquals( p1.postValue, dobj.getString( p1.tag));
    }

    @Test
    public void testAssignToNotExistingPrivateTag() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t = new TestTag( 0x00100010, "t");
        TestTag pc = new TestTag( 0x00190010, "GE creator ID");
        TestTag p1 = new TestTag( 0x00191022, "p1");
        TestTag p2 = new TestTag( 0x00191044, "p2");

        put( dobj, t);
        put( dobj, pc);
        put( dobj, p1);

        assertEquals( t.initialValue, dobj.getString( t.tag));
        assertEquals( pc.initialValue, dobj.getString( pc.tag));
        assertEquals( p1.initialValue, dobj.getString( p1.tag));
        assertFalse( dobj.contains( p2.tag));

        String script = "(0019,{GE creator ID}44) ?= \"snafu\"";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertEquals( t.postValue, dobj.getString( t.tag));
        assertEquals( pc.postValue, dobj.getString( pc.tag));
        assertEquals( p1.postValue, dobj.getString( p1.tag));
        assertFalse( dobj.contains( p2.tag));
    }

    /**
     * (0010,0010) LO #2 [t2] Patient’s Name
     * (0010,0020) LO #2 [t1] Patient ID
     * (0042,0010) SQ #-1 [2 items] Document Title
     * >ITEM #1:
     * >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_0_t1] Patient ID
     * >(0042,0010) SQ #-1 [1 item] Document Title
     * >>ITEM #1:
     * >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     * >>(0010,0020) LO #12 [t1_0_t1_0_t1] Patient ID
     * >ITEM #2:
     * >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_1_t1] Patient ID
     *
     *         * /(0010,0020) ?= "ANONYMISED"
     * results in
     *
     * (0010,0010) LO #2 [t2] Patient’s Name
     * (0010,0020) LO #10 [ANONYMISED] Patient ID
     * (0042,0010) SQ #-1 [2 items] Document Title
     * >ITEM #1:
     * >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     * >(0010,0020) LO #10 [ANONYMISED] Patient ID
     * >(0042,0010) SQ #-1 [1 item] Document Title
     * >>ITEM #1:
     * >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     * >>(0010,0020) LO #10 [ANONYMISED] Patient ID
     * >ITEM #2:
     * >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     * >(0010,0020) LO #10 [ANONYMISED] Patient ID
     */
    @Test()
    public void testAssignToSequenceWildcard_All() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t1 = new TestTag( 0x00100020, "t1", "ANONYMISED");
        TestTag t2 = new TestTag( 0x00100010, "t2");
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100020}, "t1_0_t1", "ANONYMISED");
        TestSeqTag t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t2");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100020}, "t1_1_t1", "ANONYMISED");
        TestSeqTag t1_1_t2 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t2");
        TestSeqTag t1_0_t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100020}, "t1_0_t1_0_t1", "ANONYMISED");
        TestSeqTag t1_0_t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100010}, "t1_0_t1_0_t2");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t1_0_t1);
        put( dobj, t1_0_t2);
        put( dobj, t1_1_t1);
        put( dobj, t1_1_t2);
        put( dobj, t1_0_t1_0_t1);
        put( dobj, t1_0_t1_0_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.initialValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.initialValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.initialValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.initialValue, dobj.getString( t1_0_t1_0_t2.tag));

        final String script = "*/(0010,0020) ?= \"ANONYMISED\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.postValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.postValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.postValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.postValue, dobj.getString( t1_0_t1_0_t2.tag));
    }

    /**
     * (0010,0010) LO #2 [t2] Patient’s Name
     * (0010,0020) LO #2 [t1] Patient ID
     * (0042,0010) SQ #-1 [2 items] Document Title
     * >ITEM #1:
     * >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_0_t1] Patient ID
     * >(0042,0010) SQ #-1 [1 item] Document Title
     * >>ITEM #1:
     * >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     * >>(0010,0020) LO #12 [t1_0_t1_0_t1] Patient ID
     * >ITEM #2:
     * >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_1_t1] Patient ID
     *
     *         +/(0010,0020) ?= "ANONYMISED"
     * results in
     *
     * (0010,0010) LO #2 [t2] Patient’s Name
     * (0010,0020) LO #10 [t1] Patient ID
     * (0042,0010) SQ #-1 [2 items] Document Title
     * >ITEM #1:
     * >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     * >(0010,0020) LO #10 [ANONYMISED] Patient ID
     * >(0042,0010) SQ #-1 [1 item] Document Title
     * >>ITEM #1:
     * >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     * >>(0010,0020) LO #10 [ANONYMISED] Patient ID
     * >ITEM #2:
     * >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     * >(0010,0020) LO #10 [ANONYMISED] Patient ID
     */
    @Test()
    public void testAssignToSequenceWildcard_OneOrMore() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t1 = new TestTag( 0x00100020, "t1");
        TestTag t2 = new TestTag( 0x00100010, "t2");
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100020}, "t1_0_t1", "ANONYMISED");
        TestSeqTag t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t2");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100020}, "t1_1_t1", "ANONYMISED");
        TestSeqTag t1_1_t2 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t2");
        TestSeqTag t1_0_t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100020}, "t1_0_t1_0_t1", "ANONYMISED");
        TestSeqTag t1_0_t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100010}, "t1_0_t1_0_t2");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t1_0_t1);
        put( dobj, t1_0_t2);
        put( dobj, t1_1_t1);
        put( dobj, t1_1_t2);
        put( dobj, t1_0_t1_0_t1);
        put( dobj, t1_0_t1_0_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.initialValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.initialValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.initialValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.initialValue, dobj.getString( t1_0_t1_0_t2.tag));

        final String script = "+/(0010,0020) ?= \"ANONYMISED\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.postValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.postValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.postValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.postValue, dobj.getString( t1_0_t1_0_t2.tag));
    }

    /**
     * (0010,0010) LO #2 [t2] Patient’s Name
     * (0010,0020) LO #2 [t1] Patient ID
     * (0042,0010) SQ #-1 [2 items] Document Title
     * >ITEM #1:
     * >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_0_t1] Patient ID
     * >(0042,0010) SQ #-1 [1 item] Document Title
     * >>ITEM #1:
     * >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     * >>(0010,0020) LO #12 [t1_0_t1_0_t1] Patient ID
     * >ITEM #2:
     * >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     * >(0010,0020) LO #8 [t1_1_t1] Patient ID
     *
     *         ./(0010,0020) ?= "ANONYMISED"
     * results in
     *
     0010,0010) LO #2 [t2] Patient’s Name
     (0010,0020) LO #2 [t1] Patient ID
     (0042,0010) SQ #-1 [2 items] Document Title
     >ITEM #1:
     >(0010,0010) LO #8 [t1_0_t2] Patient’s Name
     >(0010,0020) LO #10 [ANONYMISED] Patient ID
     >(0042,0010) SQ #-1 [1 item] Document Title
     >>ITEM #1:
     >>(0010,0010) LO #12 [t1_0_t1_0_t2] Patient’s Name
     >>(0010,0020) LO #12 [t1_0_t1_0_t1] Patient ID
     >ITEM #2:
     >(0010,0010) LO #8 [t1_1_t2] Patient’s Name
     >(0010,0020) LO #10 [ANONYMISED] Patient ID
     */
    @Test()
    public void testAssignToSequenceWildcard_One() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t1 = new TestTag( 0x00100020, "t1");
        TestTag t2 = new TestTag( 0x00100010, "t2");
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100020}, "t1_0_t1", "ANONYMISED");
        TestSeqTag t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t2");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100020}, "t1_1_t1", "ANONYMISED");
        TestSeqTag t1_1_t2 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t2");
        TestSeqTag t1_0_t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100020}, "t1_0_t1_0_t1");
        TestSeqTag t1_0_t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100010}, "t1_0_t1_0_t2");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t1_0_t1);
        put( dobj, t1_0_t2);
        put( dobj, t1_1_t1);
        put( dobj, t1_1_t2);
        put( dobj, t1_0_t1_0_t1);
        put( dobj, t1_0_t1_0_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.initialValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.initialValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.initialValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.initialValue, dobj.getString( t1_0_t1_0_t2.tag));

        final String script = "./(0010,0020) ?= \"ANONYMISED\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.postValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.postValue, dobj.getString( t1_1_t2.tag));
        assertEquals( t1_0_t1_0_t1.postValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.postValue, dobj.getString( t1_0_t1_0_t2.tag));
    }

    /**
     * assign "new value" to tag (0010,0020) within every sequence item [but only if the tag already exists in that sequence item] of the sequence assigned to (0042,0010)
     */
    @Test()
    public void testAssignToSequenceWildcard_AnyItem() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t1 = new TestTag( 0x00100020, "t1");
        TestTag t2 = new TestTag( 0x00100010, "t2");
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00100020}, "t1_0_t1", "new value");
        TestSeqTag t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t2");
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00100020}, "t1_1_t1", "new value");
        TestSeqTag t1_1_t2 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t2");
        TestSeqTag t1_2_t1 = new TestSeqTag(new int[]{0x00420010,2,0x00100020}, "t1_2_t1");
        TestSeqTag t1_2_t2 = new TestSeqTag(new int[]{0x00420010,2,0x00100010}, "t1_2_t2");
        TestSeqTag t1_0_t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100020}, "t1_0_t1_0_t1");
        TestSeqTag t1_0_t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00420010,0,0x00100010}, "t1_0_t1_0_t2");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t1_0_t1);
        put( dobj, t1_0_t2);
        put( dobj, t1_1_t1);
        put( dobj, t1_1_t2);
        // this one is missing and shouldn't be added
        // put( dobj, t1_2_t1);
        put( dobj, t1_2_t2);
        put( dobj, t1_0_t1_0_t2);
        put( dobj, t1_0_t1_0_t1);
        put( dobj, t1_0_t1_0_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.initialValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.initialValue, dobj.getString( t1_1_t2.tag));
        assertFalse( dobj.contains( t1_2_t1.tag));
        assertEquals( t1_2_t2.initialValue, dobj.getString( t1_2_t2.tag));
        assertEquals( t1_0_t1_0_t1.initialValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.initialValue, dobj.getString( t1_0_t1_0_t2.tag));

        final String script = "(0042,0010)[%]/(0010,0020) ?= \"new value\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_0_t2.postValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_1_t2.postValue, dobj.getString( t1_1_t2.tag));
        assertFalse( dobj.contains( t1_2_t1.tag));
        assertEquals( t1_2_t2.postValue, dobj.getString( t1_2_t2.tag));
        assertEquals( t1_0_t1_0_t1.postValue, dobj.getString( t1_0_t1_0_t1.tag));
        assertEquals( t1_0_t1_0_t2.postValue, dobj.getString( t1_0_t1_0_t2.tag));
    }

    /**
     * assign "new value" to all pre-existing attributes in range (0030,0100-010F)
     */
    @Test()
    public void testAssignToDigitWildcard_Any() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00300100, "t0", "new value");
        TestTag t1 = new TestTag( 0x00300101, "t1", "new value");
        TestTag t2 = new TestTag( 0x00300102, "t2", "new value");
        TestTag t3 = new TestTag( 0x00300103, "t3", "new value");
        TestTag t4 = new TestTag( 0x00300104, "t4", "new value");
        TestTag t5 = new TestTag( 0x00300105, "t5", "new value");
        TestTag t6 = new TestTag( 0x00300106, "t6", "new value");
        TestTag t7 = new TestTag( 0x00300107, "t7", "new value");
        TestTag t8 = new TestTag( 0x00300108, "t8", "new value");
        TestTag t9 = new TestTag( 0x00300109, "t9", "new value");
        TestTag ta = new TestTag( 0x0030010a, "ta", "new value");
        TestTag tb = new TestTag( 0x0030010b, "tb", "new value");
        TestTag tc = new TestTag( 0x0030010c, "tc", "new value");
        TestTag td = new TestTag( 0x0030010d, "td", "new value");
        TestTag te = new TestTag( 0x0030010e, "te", "new value");
        TestTag tf = new TestTag( 0x0030010f, "tf", "new value");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        // put( dobj, t3); not present and shouldn't be created
        // put( dobj, t4); not present and shouldn't be created
        put( dobj, t5);
        put( dobj, t6);
        // put( dobj, t7); not present and shouldn't be created
        put( dobj, t8);
        put( dobj, t9);
        put( dobj, ta);
        put( dobj, tb);
        put( dobj, tc);
        put( dobj, td);
        // put( dobj, te); not present and shouldn't be created
        put( dobj, tf);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.initialValue, dobj.getString( t5.tag));
        assertEquals( t6.initialValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.initialValue, dobj.getString( t8.tag));
        assertEquals( t9.initialValue, dobj.getString( t9.tag));
        assertEquals( ta.initialValue, dobj.getString( ta.tag));
        assertEquals( tb.initialValue, dobj.getString( tb.tag));
        assertEquals( tc.initialValue, dobj.getString( tc.tag));
        assertEquals( td.initialValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.initialValue, dobj.getString( tf.tag));

        // assign "new value" to all pre-existing attributes in range (0030,0100-010F)
        final String script = "(0030,010X) ?= \"new value\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t0.postValue, dobj.getString( t0.tag));
        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.postValue, dobj.getString( t5.tag));
        assertEquals( t6.postValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.postValue, dobj.getString( t8.tag));
        assertEquals( t9.postValue, dobj.getString( t9.tag));
        assertEquals( ta.postValue, dobj.getString( ta.tag));
        assertEquals( tb.postValue, dobj.getString( tb.tag));
        assertEquals( tc.postValue, dobj.getString( tc.tag));
        assertEquals( td.postValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.postValue, dobj.getString( tf.tag));
    }

    /**
     *  assign "new value" to all pre-existing attributes in range (0030,0100-010F) with even last digit
     */
    @Test()
    public void testAssignToDigitWildcard_Even() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00300100, "t0", "new value");
        TestTag t1 = new TestTag( 0x00300101, "t1");
        TestTag t2 = new TestTag( 0x00300102, "t2", "new value");
        TestTag t3 = new TestTag( 0x00300103, "t3");
        TestTag t4 = new TestTag( 0x00300104, "t4", "new value");
        TestTag t5 = new TestTag( 0x00300105, "t5");
        TestTag t6 = new TestTag( 0x00300106, "t6", "new value");
        TestTag t7 = new TestTag( 0x00300107, "t7");
        TestTag t8 = new TestTag( 0x00300108, "t8", "new value");
        TestTag t9 = new TestTag( 0x00300109, "t9");
        TestTag ta = new TestTag( 0x0030010a, "ta", "new value");
        TestTag tb = new TestTag( 0x0030010b, "tb");
        TestTag tc = new TestTag( 0x0030010c, "tc", "new value");
        TestTag td = new TestTag( 0x0030010d, "td");
        TestTag te = new TestTag( 0x0030010e, "te", "new value");
        TestTag tf = new TestTag( 0x0030010f, "tf");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        // put( dobj, t3); not present and shouldn't be created
        // put( dobj, t4); not present and shouldn't be created
        put( dobj, t5);
        put( dobj, t6);
        // put( dobj, t7); not present and shouldn't be created
        put( dobj, t8);
        put( dobj, t9);
        put( dobj, ta);
        put( dobj, tb);
        put( dobj, tc);
        put( dobj, td);
        // put( dobj, te); not present and shouldn't be created
        put( dobj, tf);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.initialValue, dobj.getString( t5.tag));
        assertEquals( t6.initialValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.initialValue, dobj.getString( t8.tag));
        assertEquals( t9.initialValue, dobj.getString( t9.tag));
        assertEquals( ta.initialValue, dobj.getString( ta.tag));
        assertEquals( tb.initialValue, dobj.getString( tb.tag));
        assertEquals( tc.initialValue, dobj.getString( tc.tag));
        assertEquals( td.initialValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.initialValue, dobj.getString( tf.tag));

        // assign "new value" to all pre-existing attributes in range (0030,0100-010F) with even last digit
        final String script = "(0030,010@) ?= \"new value\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t0.postValue, dobj.getString( t0.tag));
        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.postValue, dobj.getString( t5.tag));
        assertEquals( t6.postValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.postValue, dobj.getString( t8.tag));
        assertEquals( t9.postValue, dobj.getString( t9.tag));
        assertEquals( ta.postValue, dobj.getString( ta.tag));
        assertEquals( tb.postValue, dobj.getString( tb.tag));
        assertEquals( tc.postValue, dobj.getString( tc.tag));
        assertEquals( td.postValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.postValue, dobj.getString( tf.tag));
    }

    /**
     * assign "new value" to all pre-existing attributes in range (0030,0100-010F) with odd last digit
     */
    @Test()
    public void testAssignToDigitWildcard_Odd() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag t0 = new TestTag( 0x00300100, "t0");
        TestTag t1 = new TestTag( 0x00300101, "t1", "new value");
        TestTag t2 = new TestTag( 0x00300102, "t2");
        TestTag t3 = new TestTag( 0x00300103, "t3", "new value");
        TestTag t4 = new TestTag( 0x00300104, "t4");
        TestTag t5 = new TestTag( 0x00300105, "t5", "new value");
        TestTag t6 = new TestTag( 0x00300106, "t6");
        TestTag t7 = new TestTag( 0x00300107, "t7", "new value");
        TestTag t8 = new TestTag( 0x00300108, "t8");
        TestTag t9 = new TestTag( 0x00300109, "t9", "new value");
        TestTag ta = new TestTag( 0x0030010a, "ta");
        TestTag tb = new TestTag( 0x0030010b, "tb", "new value");
        TestTag tc = new TestTag( 0x0030010c, "tc");
        TestTag td = new TestTag( 0x0030010d, "td", "new value");
        TestTag te = new TestTag( 0x0030010e, "te");
        TestTag tf = new TestTag( 0x0030010f, "tf", "new value");

        put( dobj, t0);
        put( dobj, t1);
        put( dobj, t2);
        // put( dobj, t3); not present and shouldn't be created
        // put( dobj, t4); not present and shouldn't be created
        put( dobj, t5);
        put( dobj, t6);
        // put( dobj, t7); not present and shouldn't be created
        put( dobj, t8);
        put( dobj, t9);
        put( dobj, ta);
        put( dobj, tb);
        put( dobj, tc);
        put( dobj, td);
        // put( dobj, te); not present and shouldn't be created
        put( dobj, tf);

        assertEquals( t0.initialValue, dobj.getString( t0.tag));
        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.initialValue, dobj.getString( t5.tag));
        assertEquals( t6.initialValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.initialValue, dobj.getString( t8.tag));
        assertEquals( t9.initialValue, dobj.getString( t9.tag));
        assertEquals( ta.initialValue, dobj.getString( ta.tag));
        assertEquals( tb.initialValue, dobj.getString( tb.tag));
        assertEquals( tc.initialValue, dobj.getString( tc.tag));
        assertEquals( td.initialValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.initialValue, dobj.getString( tf.tag));

        // assign "new value" to all pre-existing attributes in range (0030,0100-010F) with odd last digit
        final String script = "(0030,010#) ?= \"new value\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t0.postValue, dobj.getString( t0.tag));
        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t2.postValue, dobj.getString( t2.tag));
        assertFalse( dobj.contains( t3.tag));
        assertFalse( dobj.contains( t4.tag));
        assertEquals( t5.postValue, dobj.getString( t5.tag));
        assertEquals( t6.postValue, dobj.getString( t6.tag));
        assertFalse( dobj.contains( t7.tag));
        assertEquals( t8.postValue, dobj.getString( t8.tag));
        assertEquals( t9.postValue, dobj.getString( t9.tag));
        assertEquals( ta.postValue, dobj.getString( ta.tag));
        assertEquals( tb.postValue, dobj.getString( tb.tag));
        assertEquals( tc.postValue, dobj.getString( tc.tag));
        assertEquals( td.postValue, dobj.getString( td.tag));
        assertFalse( dobj.contains( te.tag));
        assertEquals( tf.postValue, dobj.getString( tf.tag));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
