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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Run tests of if-elseif-else statements.
 *
 * The syntax of if-elseif-else statements mirror Java's except 'elseif' is one word and block braces are required.
 *
 */
public class TestConditional {

    @Test
    public void testMatchesIf() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "if ((0010,0010) ~ \".*Curly.*\" ) { (0010,0010) := \"if\"}\n" +
                "elseif ((0010,0010) ~ \".*Larry.*\" ) { (0010,0010) := \"elseif\"}\n" +
                "else { (0010,0010) := \"else\" }";
        String name = "Howard^Curly";
        int tag = 0x00100010;
        src_dobj.putString( tag, name);

        assertTrue(src_dobj.contains(tag));
        assertEquals( src_dobj.getString(tag), name);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "if", result_dobj.getString(tag));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
