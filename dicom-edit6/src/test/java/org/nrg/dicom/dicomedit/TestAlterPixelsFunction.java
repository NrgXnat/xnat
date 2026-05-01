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
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Run tests of alterPixels function.
 *
 */
public class TestAlterPixelsFunction {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    /**
     * Need to manually view compare the input and output images to see the pixel edits but this at least shows the alterpixels statement
     * parses and runs.
     */
    @Test
    public void testSupportedEditor() {
        try {
            String script =
                    "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"] \n";

            final DicomObjectI src_dobj = DicomObjectFactory.newInstance(FILE4);

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            Path f = Files.createTempFile(Paths.get("/tmp"),"de6", ".dcm");
            try (FileOutputStream os = new FileOutputStream(f.toFile())) {
                result_dobj.write(os);
            }
        }
        catch( IOException | MizerException e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * Test script with un-supported pixel editor.
     */
    @Test
    public void testUnSupportedEditor() throws MizerException {
        String script =
                "alterPixels[\"foo\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(FILE4);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final AnonymizationResult result = sa.apply(src_dobj);

        assertTrue( result instanceof AnonymizationResultError);
    }

    /**
     * Test script with bad parameter syntax.
     */
    @Test
    public void testTooFewParams() throws MizerException {
        String script =
                "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\" ]\n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(FILE4);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final AnonymizationResult result = sa.apply(src_dobj);

        assertTrue( result instanceof AnonymizationResultError);
    }

    public Throwable getRootCause(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
