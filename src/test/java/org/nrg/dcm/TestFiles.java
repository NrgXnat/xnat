/*
 * DicomUtils: org.nrg.dcm.TestFiles
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class TestFiles {
    protected final File _sampleDicomFile;
    protected final File _sampleDicomFileGzip;

    protected TestFiles() {
        final URL dicomUrl = TestFiles.class.getClassLoader().getResource(DICOM_RESOURCE);
        assertNotNull(dicomUrl);

        final URL dicomGzUrl = TestFiles.class.getClassLoader().getResource(DICOM_RESOURCE + ".gz");
        assertNotNull(dicomGzUrl);

        try {
            final File dicomFile = new File(dicomUrl.toURI());
            _sampleDicomFile = dicomFile.isFile() ? dicomFile : null;
            final File dicomGzFile = new File(dicomGzUrl.toURI());
            _sampleDicomFileGzip = dicomGzFile.isFile() ? dicomGzFile : null;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to initialize sample data", e);
        }
        if (null == _sampleDicomFile || null == _sampleDicomFileGzip) {
            // TODO: retrieve sample data
            throw new RuntimeException("Unable to find sample data");
        }
    }

    protected File copySampleFileToTestTarget(final File source) throws IOException {
        final File target = File.createTempFile("sample", source.getName().endsWith(".gz") ? ".dcm.gz" : ".dcm");
        target.deleteOnExit();

        Files.copy(source, target);

        return target;
    }

    private final static String DICOM_RESOURCE = "dicom/1.MR.head_DHead.4.23.20061214.091206.156000.8215318065.dcm";
}
