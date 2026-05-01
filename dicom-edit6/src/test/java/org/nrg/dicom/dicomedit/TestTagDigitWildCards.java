/*
 * DicomEdit: TestDeleteStatement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test assign/delete to tags with wildcards.
 */
public class TestTagDigitWildCards {

    private static final Logger logger = LoggerFactory.getLogger(TestTagDigitWildCards.class);

    private static final String S_ASSIGN_TO_WILDCARD = "version \"6.1\"\n(0018,901X) := \"NO\"\n";
    private static final String S_ASSIGN_TO_ODD_WILDCARD = "version \"6.1\"\n(0018,901#) := \"NO\"\n";
    private static final String S_ASSIGN_TO_EVEN_WILDCARD = "version \"6.1\"\n(0018,901@) := \"NO\"\n";
    private static final String S_DELETE_EVEN_WILDCARD = "version \"6.1\"\n-(0018,900@)\n";
    private static final String S_DELETE_ODD_WILDCARD = "version \"6.1\"\n-(0018,900#)\n";
    private static final String S_DELETE_WILDCARD = "version \"6.1\"\n-(0018,900X)\n";


    @Test
    public void testAssignToWildCard() throws MizerException {
        final DicomObjectI dobj = createTestObject();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_TO_WILDCARD));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Assigning to a tagpath that maps to multiple attributes in not supported. TagPath: 0018901X"));
    }

    @Test
    public void testAssignOddDigitWildCard() throws MizerException {
        final DicomObjectI dobj = createTestObject();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_TO_ODD_WILDCARD));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Assigning to a tagpath that maps to multiple attributes in not supported. TagPath: 0018901#"));
    }

    @Test
    public void testAssignEvenDigitWildCard() throws MizerException {
        final DicomObjectI dobj = createTestObject();

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_ASSIGN_TO_EVEN_WILDCARD));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Assigning to a tagpath that maps to multiple attributes in not supported. TagPath: 0018901@"));
    }

    @Test
    public void testDeleteEvenDigit() throws Exception {
        final DicomObjectI src_dobj = createTestObject();

        assertTrue( src_dobj.contains(t9000));
        assertTrue( src_dobj.contains(t9001));
        assertTrue( src_dobj.contains(t9002));
        assertTrue( src_dobj.contains(t9003));
        assertTrue( src_dobj.contains(t9004));
        assertTrue( src_dobj.contains(t9005));
        assertTrue( src_dobj.contains(t9007));
        assertTrue( src_dobj.contains(t9008));
        assertTrue( src_dobj.contains(t9009));
        assertTrue( src_dobj.contains(t900a));
        assertTrue( src_dobj.contains(t900b));
        assertTrue( src_dobj.contains(t900c));
        assertTrue( src_dobj.contains(t900d));
        assertTrue( src_dobj.contains(t900e));
        assertTrue( src_dobj.contains(t900f));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_EVEN_WILDCARD));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(t9000));
        assertTrue( result_dobj.contains(t9001));
        assertFalse( result_dobj.contains(t9002));
        assertTrue( result_dobj.contains(t9003));
        assertFalse( result_dobj.contains(t9004));
        assertTrue( result_dobj.contains(t9005));
        assertTrue( result_dobj.contains(t9007));
        assertFalse( result_dobj.contains(t9008));
        assertTrue( result_dobj.contains(t9009));
        assertFalse( result_dobj.contains(t900a));
        assertTrue( result_dobj.contains(t900b));
        assertFalse( result_dobj.contains(t900c));
        assertTrue( result_dobj.contains(t900d));
        assertFalse( result_dobj.contains(t900e));
        assertTrue( result_dobj.contains(t900f));
    }

    @Test
    public void testDeleteOddDigit() throws Exception {
        final DicomObjectI src_dobj = createTestObject();

        assertTrue( src_dobj.contains(t9000));
        assertTrue( src_dobj.contains(t9001));
        assertTrue( src_dobj.contains(t9002));
        assertTrue( src_dobj.contains(t9003));
        assertTrue( src_dobj.contains(t9004));
        assertTrue( src_dobj.contains(t9005));
        assertTrue( src_dobj.contains(t9007));
        assertTrue( src_dobj.contains(t9008));
        assertTrue( src_dobj.contains(t9009));
        assertTrue( src_dobj.contains(t900a));
        assertTrue( src_dobj.contains(t900b));
        assertTrue( src_dobj.contains(t900c));
        assertTrue( src_dobj.contains(t900d));
        assertTrue( src_dobj.contains(t900e));
        assertTrue( src_dobj.contains(t900f));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ODD_WILDCARD));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(t9000));
        assertFalse( result_dobj.contains(t9001));
        assertTrue( result_dobj.contains(t9002));
        assertFalse( result_dobj.contains(t9003));
        assertTrue( result_dobj.contains(t9004));
        assertFalse( result_dobj.contains(t9005));
        assertFalse( result_dobj.contains(t9007));
        assertTrue( result_dobj.contains(t9008));
        assertFalse( result_dobj.contains(t9009));
        assertTrue( result_dobj.contains(t900a));
        assertFalse( result_dobj.contains(t900b));
        assertTrue( result_dobj.contains(t900c));
        assertFalse( result_dobj.contains(t900d));
        assertTrue( result_dobj.contains(t900e));
        assertFalse( result_dobj.contains(t900f));
    }


    @Test
    public void testDeleteAnyDigit() throws Exception {
        final DicomObjectI src_dobj = createTestObject();

        assertTrue( src_dobj.contains(t9000));
        assertTrue( src_dobj.contains(t9001));
        assertTrue( src_dobj.contains(t9002));
        assertTrue( src_dobj.contains(t9003));
        assertTrue( src_dobj.contains(t9004));
        assertTrue( src_dobj.contains(t9005));
        assertTrue( src_dobj.contains(t9007));
        assertTrue( src_dobj.contains(t9008));
        assertTrue( src_dobj.contains(t9009));
        assertTrue( src_dobj.contains(t900a));
        assertTrue( src_dobj.contains(t900b));
        assertTrue( src_dobj.contains(t900c));
        assertTrue( src_dobj.contains(t900d));
        assertTrue( src_dobj.contains(t900e));
        assertTrue( src_dobj.contains(t900f));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_WILDCARD));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(t9000));
        assertFalse( result_dobj.contains(t9001));
        assertFalse( result_dobj.contains(t9002));
        assertFalse( result_dobj.contains(t9003));
        assertFalse( result_dobj.contains(t9004));
        assertFalse( result_dobj.contains(t9005));
        assertFalse( result_dobj.contains(t9007));
        assertFalse( result_dobj.contains(t9008));
        assertFalse( result_dobj.contains(t9009));
        assertFalse( result_dobj.contains(t900a));
        assertFalse( result_dobj.contains(t900b));
        assertFalse( result_dobj.contains(t900c));
        assertFalse( result_dobj.contains(t900d));
        assertFalse( result_dobj.contains(t900e));
        assertFalse( result_dobj.contains(t900f));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString( t9000, "t9000");
        src_dobj.putString( t9001, "t9001");
        src_dobj.putString( t9002, "t9002");
        src_dobj.putString( t9003, "t9003");
        src_dobj.putString( t9004, "t9004");
        src_dobj.putString( t9005, "t9005");
        src_dobj.putString( t9007, "t9007");
        src_dobj.putString( t9008, "t9008");
        src_dobj.putString( t9009, "t9009");
        src_dobj.putString( t900a, "t900a");
        src_dobj.putString( t900b, "t900b");
        src_dobj.putString( t900c, "t900c");
        src_dobj.putString( t900d, "t900d");
        src_dobj.putString( t900e, "t900e");
        src_dobj.putString( t900f, "t900f");

        return src_dobj;
    }

    private static int t9000 = 0x00189000;
    private static int t9001 = 0x00189001;
    private static int t9002 = 0x00189002;
    private static int t9003 = 0x00189003;
    private static int t9004 = 0x00189004;
    private static int t9005 = 0x00189005;
    private static int t9006 = 0x00189006;
    private static int t9007 = 0x00189007;
    private static int t9008 = 0x00189008;
    private static int t9009 = 0x00189009;
    private static int t900a = 0x0018900a;
    private static int t900b = 0x0018900b;
    private static int t900c = 0x0018900c;
    private static int t900d = 0x0018900d;
    private static int t900e = 0x0018900e;
    private static int t900f = 0x0018900f;

}
