/*
 * dicomtools: org.nrg.dcm.DicomDirTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DicomDirTest extends TestFiles {
    @Before
    public void setUp() throws IOException {
        _sample = copySampleFileToTestTarget(_sampleDicomFile);

        _sampleHeaderless = File.createTempFile("sample_headerless", ".dcm");
        _sampleHeaderless.deleteOnExit();

        final Attributes dataset = DicomUtils.read(_sample);
        final Attributes fmi = dataset.createFileMetaInformation(dataset.getString(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian));
        try (final DicomOutputStream output = new DicomOutputStream(_sampleHeaderless)) {
            output.writeDataset(fmi, dataset);
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
