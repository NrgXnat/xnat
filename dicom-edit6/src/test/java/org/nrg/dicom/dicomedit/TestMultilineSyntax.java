package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * Multi-line statements are handled by preprocessing the script and stripping backslash-newline.
 * These tests just insure the scripts compile and don't care if they actually do anything.
 */
public class TestMultilineSyntax {
    private static final Logger logger = LoggerFactory.getLogger((TestListSyntax.class));

    @Test
    public void test1() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "(0010,0010) := \"fubar\"\n"
                + "(0010,0020) := \\\n \"snafu\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

    @Test
    public void test2() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        String script = "(0010,00\\\n10) := \"fubar\"\n"
                + "(0010,0020) := \"sna\\\nfu\"\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

    @Test
    public void testDE_135() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String script = "tagsToRemove := { \n" +
                "        (0010,0010),\n" +
                "        (0010,21B0),\n" +
                "        (0038,0010),\n" +
                "        (0008,1080)\n" +
                "    }\n\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

    @Test
    public void testDE_138() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String script = "(0008,1030) := \"THIS IS \\\n" +
                "A STRING\" \n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

    @Test
    public void testComment() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String script = "// one-line comment\n"
                + "// multi-line \\\n comment.\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

    @Test
    public void testEmptyList() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        String script = "foo := {}\n"
                + "bar := { }\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);
        assertNotNull( dobj);
    }

}
