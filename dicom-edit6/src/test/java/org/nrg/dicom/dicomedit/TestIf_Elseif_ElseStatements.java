/*
 * DicomEdit: TestConditionalStatements
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

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Run tests of if-elseif-else statements.
 *
 * The syntax of if-elseif-else statements mirror Java's except 'elseif' is one word and block braces are required.
 *
 */
public class TestIf_Elseif_ElseStatements {

    @Test
    public void testMatchesIf() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) = \"Fine^Larry\" ) { (0010,0010) := \"elseif\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Howard^Curly";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(tag), "if");
    }

    @Test
    public void testMatchesElseif() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) = \"Fine^Larry\" ) { (0010,0010) := \"elseif\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Fine^Larry";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(tag), "elseif");
    }

    @Test
    public void testMatchesElse() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) = \"Fine^Larry\" ) { (0010,0010) := \"elseif\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Howard^Moe";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(tag), "else");
    }

    @Test
    public void testMatchesElseWithMultipleElseif() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) = \"Fine^Larry\" ) { (0010,0010) := \"elseif1\"}\n" +
                "elseif ((0010,0010) = \"Howard^Shemp\" ) { (0010,0010) := \"elseif2\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Howard^Moe";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(tag), "else");
    }

    @Test
    public void testMatchesElseifWithMultipleElseif() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) = \"Fine^Larry\" ) { (0010,0010) := \"elseif1\"}\n" +
                "elseif ((0010,0010) = \"Howard^Shemp\" ) { (0010,0010) := \"elseif2\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Howard^Shemp";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(tag), "elseif2");
    }

    @Test
    public void testIfWithMultipleStatementBlock() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) = \"Howard^Curly\" ) { \n" +
                " (0010,0010) := \"1\"\n" +
                " (0010,0020) := \"2\"}\n";
        String name = "Howard^Curly";
        src_dobj.putString( 0x00100010, name);
        src_dobj.putString( 0x00100020, name);

        assertEquals( src_dobj.getString(0x00100010), name);
        assertEquals( src_dobj.getString(0x00100020), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00100010), "1");
        assertEquals( result_dobj.getString(0x00100020), "2");
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
