package org.nrg.xnat.snapshot.generator.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Ordering the objects of a scan whose catalog does not record instance numbers.
 * <p>
 * {@code CatalogBuilder} skips both instance number and frame count in the branch that builds the
 * secondary catalog -- "they don't really apply to secondary files" -- so a scan rendered from one has
 * neither. The frame count was dealt with in #44; this is the order, which decided which slice of the
 * montage each object became.
 */
public class SnapshotInstanceOrderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /** The instance number rides along in the read that counts frames, so it costs no extra I/O. */
    @Test
    public void readsTheInstanceNumberFromTheSameReadAsTheFrameCount() throws Exception {
        final File object = writeObject("ordered.dcm", 7, 3);

        final FrameCounter.ObjectHeader header = FrameCounter.headerOf(object);

        assertEquals("frames", 3, header.frames());
        assertEquals("instance number", Integer.valueOf(7), header.instanceNumber());
    }

    /** An object that does not say gets null rather than a fabricated zero, so it can sort last. */
    @Test
    public void reportsNoInstanceNumberWhenTheObjectDoesNotCarryOne() throws Exception {
        final File object = writeObject("unordered.dcm", null, null);

        final FrameCounter.ObjectHeader header = FrameCounter.headerOf(object);

        assertEquals("frames", 1, header.frames());
        assertNull("instance number", header.instanceNumber());
    }

    /** Instance Number sits below Columns, so widening the read was never necessary. */
    @Test
    public void instanceNumberIsInsideTheWindowThatReadsTheDimensions() {
        assertEquals("Instance Number should sort before Columns, which bounds the read",
                     -1, Integer.compare(Tag.InstanceNumber, Tag.Columns + 1));
    }

    /** An object carrying no image still reports its place in the series, so it does not disorder the rest. */
    @Test
    public void reportsTheInstanceNumberEvenWhenTheObjectHasNoImage() throws Exception {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.ComprehensiveSRStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.1");
        dataset.setInt(Tag.InstanceNumber, VR.IS, 4);
        final File file = temporaryFolder.newFile("report.dcm");
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }

        final FrameCounter.ObjectHeader header = FrameCounter.headerOf(file);

        assertEquals("contributes no slices", 0, header.frames());
        assertEquals("but still knows where it belongs", Integer.valueOf(4), header.instanceNumber());
    }

    /** A minimal object, optionally declaring Instance Number and Number of Frames. */
    private File writeObject(final String name, final Integer instanceNumber, final Integer frames) throws Exception {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498." + name.hashCode());
        if (instanceNumber != null) {
            dataset.setInt(Tag.InstanceNumber, VR.IS, instanceNumber);
        }
        dataset.setInt(Tag.Rows, VR.US, 4);
        dataset.setInt(Tag.Columns, VR.US, 4);
        dataset.setInt(Tag.BitsAllocated, VR.US, 8);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        if (frames != null) {
            dataset.setInt(Tag.NumberOfFrames, VR.IS, frames);
        }
        final File file = temporaryFolder.newFile(name);
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }
        return file;
    }
}
