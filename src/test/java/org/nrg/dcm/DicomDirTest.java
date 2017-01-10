/*
 * dicomtools: org.nrg.dcm.DicomDirTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import com.google.common.io.Files;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomDirTest extends TestFiles {
    @Before
    public void setUp() throws IOException {
        _sample = copySampleFileToTestTarget(_sampleDicomFile);

        _sampleHeaderless = File.createTempFile("sample_headerless", ".dcm");
        _sampleHeaderless.deleteOnExit();

        final DicomObject dicomObject = DicomUtils.read(_sample);
        try (final DicomOutputStream output = new DicomOutputStream(_sampleHeaderless)) {
            output.writeDataset(dicomObject, dicomObject.getString(Tag.TransferSyntaxUID));
        }
    }

    @After
    public void tearDown() {
        _sample.delete();
        _sampleHeaderless.delete();
    }

    @Test
    public void testAddFile() throws IOException {
        addFile(_sample);
    }

    @Test
    public void testAddHeaderlessFile() throws IOException {
        addFile(_sampleHeaderless);
    }

    private void addFile(final File file) throws IOException {
        final File ddf = File.createTempFile("DICOM", "DIR");
        ddf.deleteOnExit();

        final DicomDir dd = new DicomDir(ddf);
        dd.create();
        dd.addFile(file);
        dd.close();
        ddf.delete();
    }

    private File _sample;
    private File _sampleHeaderless;
}
