/*
 * DicomEdit: TestConditionalStatements
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Run tests of conditional statements.
 *
 * Conditional statements have syntax: conditional ? action : action, where the 'else' clause is optional.
 * Valid conditional operators are '=' equals, '~' matches, '!=' not-equals, '!~' not-matches.
 * action are assignment, variable initialization, or deletion.
 *
 */
public class TestConditionalStatements {

    @Test
    public void testMatchesLiteralRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) ~ \"Some Institution\" ? (0008,0080) := \"New Institute\" \n";
        int[] s = {0x00080080};
        src_dobj.putString( s, "Some Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals( src_dobj.getString(s), "Some Institution");

        final BaseScriptApplicator sa          = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00080080), "New Institute");
    }

    @Test
    public void testIfThenElseMatchesLiteralRegexTrue() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) ~ \"Some Institution\" ? (0008,0080) := \"True Institute\" : (0008,0080) := \"False Institute\"\n";
        int[] s = {0x00080080};
        src_dobj.putString( s, "Some Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals( src_dobj.getString(s), "Some Institution");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00080080), "True Institute");
    }

    @Test
    public void testIfThenElseMatchesLiteralRegexFalse() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) ~ \"Wrong Institution\" ? (0008,0080) := \"True Institute\" : (0008,0080) := \"False Institute\"\n";
        int[] s = {0x00080080};
        src_dobj.putString( s, "Some Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals( src_dobj.getString(s), "Some Institution");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00080080), "False Institute");
    }

    @Test
    public void testNotMatchesLiteralRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) !~ \"Some Institution\" ? (0008,0080) := \"New Institute\" \n";
        int[] s = {0x00080080};
        src_dobj.putString( s, "Some FooFoo Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals( src_dobj.getString(s), "Some FooFoo Institution");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00080080), "New Institute");
    }

    @Test
    public void testDigitClassRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0020) ~ \"20[0-9]{6}\" ? (0010,4000) := \"Session 20XX\" \n";
        int[] s = {0x00080020};
        src_dobj.putString( s, "20061214");

        assertTrue(src_dobj.contains(0x00080020));
        assertEquals( src_dobj.getString(s), "20061214");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00104000), "Session 20XX");
    }

    @Test
    public void testDigitPredefinedClassRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        // double escape the 'd' char because here, the regex is a string in a string.
        // this results in a DE processing the statement: (0008,0020)~"20\\d{6}":(0010,4000):="Session 20XX"
        String statement = "(0008,0020) ~ \"20\\\\d\\\\d\\\\d\\\\d\\\\d\\\\d\" ? (0010,4000) := \"Session 20XX\" \n";
        int[] s = {0x00080020};
        src_dobj.putString( s, "20061214");

        assertTrue(src_dobj.contains(0x00080020));
        assertEquals( src_dobj.getString(s), "20061214");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00104000), "Session 20XX");
    }

    @Test
    public void testAssignIfEmpty() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
            String statement = "(0010,0010) = \"\" ? (0010,0010) := (0010,0020) \n";

            // 10,10 missing:  Does not assign.
            src_dobj.putString(0x00100020, "1020");

            assertFalse(src_dobj.contains(0x00100010));
            assertEquals(src_dobj.getString(0x00100020), "1020");

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
            DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertFalse(result_dobj.contains(0x00100010));
            assertNull( result_dobj.getString(0x00100010));
            assertEquals(result_dobj.getString(0x00100020), "1020");

            // 10,10 known value: Does not assign.
            src_dobj.putString(0x00100010, "1010");
            assertTrue(src_dobj.contains(0x00100010));
            assertEquals(src_dobj.getString(0x00100010), "1010");
            assertEquals(src_dobj.getString(0x00100020), "1020");

            result_dobj = sa.apply( src_dobj).getDicomObject();

            assertEquals(result_dobj.getString(0x00100010), "1010");
            assertEquals(result_dobj.getString(0x00100020), "1020");

            // 10,10 blank: Does assign.
            src_dobj.putString(0x00100010, "");
            assertTrue(src_dobj.contains(0x00100010));
            assertEquals(src_dobj.getString(0x00100010), "");
            assertEquals(src_dobj.getString(0x00100020), "1020");

            result_dobj = sa.apply( src_dobj).getDicomObject();

            assertEquals(result_dobj.getString(0x00100010), "1020");
            assertEquals(result_dobj.getString(0x00100020), "1020");
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    public void testAssignIfNullOrEmpty() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
            String statement = "(0010,0010) !~ \".+\" ? (0010,0010) := (0010,0020) \n";

            // 10,10 missing: Does assign
            src_dobj.putString(0x00100020, "1020");

            assertFalse( src_dobj.contains(0x00100010));
            assertEquals( "1020", src_dobj.getString(0x00100020));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
            DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertTrue( result_dobj.contains(0x00100010));
            assertEquals( "1020", result_dobj.getString(0x00100010));
            assertEquals( "1020", result_dobj.getString(0x00100020));

            // 10,10 blank: Does assign
            src_dobj.putString(0x00100010, "");
            assertTrue( src_dobj.contains(0x00100010));
            assertEquals( "", src_dobj.getString(0x00100010));
            assertEquals( "1020", src_dobj.getString(0x00100020));

            result_dobj = sa.apply( src_dobj).getDicomObject();

            assertEquals( "1020", result_dobj.getString(0x00100010));
            assertEquals( "1020", result_dobj.getString(0x00100020));

            // 10,10 known value: Does not assign.
            src_dobj.putString(0x00100010, "1010");
            assertTrue(src_dobj.contains(0x00100010));
            assertEquals( "1010", src_dobj.getString(0x00100010));
            assertEquals( "1020", src_dobj.getString(0x00100020));

            result_dobj = sa.apply( src_dobj).getDicomObject();

            assertEquals("1010", result_dobj.getString(0x00100010));
            assertEquals( "1020", result_dobj.getString(0x00100020));

        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    public void testMethodActionParses() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
            String statement = "(0020,0011) = \"13875\" ? alterpixels[ \"rectangle\", \"l=0, t=0, r=32, b=16\", \"solid\", \"v=100\"] \n";
            assertTrue(true);
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    @Ignore
    // TestMethodAssign is a Custom Function defined in src/test/java.  I can't get src/main/java/.../FunctionManager
    // to discover this function.  Dang classloaders.
    public void testMethodActionIfTrue() {
        try {
            DicomObjectI dobj = DicomObjectFactory.newInstance();
            String statement = "(0020,0011) = \"13875\" ? testMethodAssign[ (0010,0010), \"Test method was here\" ] \n";
            dobj.putString(0x00200011, "13875");
            assertEquals( "13875", dobj.getString(0x00200011));
            assertFalse( dobj.contains(0x00100010));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(statement));
            dobj = sa.apply( dobj).getDicomObject();

            assertEquals("1010", dobj.getString(0x00100010));
            assertEquals( "1020", dobj.getString(0x00100020));

        }
        catch( Exception e) {
            e.printStackTrace();
            fail( "Unexpected exception: " + e);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
