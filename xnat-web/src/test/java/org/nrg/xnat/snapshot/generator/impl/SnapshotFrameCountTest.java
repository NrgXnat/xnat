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
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Counting frames off the DICOM objects, for scans whose catalog does not record it.
 * <p>
 * Only the primary catalog carries dimensions_z: {@code CatalogBuilder} sets it inside the branch
 * that builds the primary catalog, so a scan whose objects landed in the secondary catalog gets one
 * without it and used to be refused with "This is above my pay grade". The count has to match what
 * the builder would have recorded -- one frame per object unless the object says otherwise.
 */
public class SnapshotFrameCountTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void countsOneFramePerSingleFrameObject() throws Exception {
        final File first  = writeObject("single-a.dcm", null);
        final File second = writeObject("single-b.dcm", null);

        assertEquals(Integer.valueOf(2),
                     SnapshotResourceGeneratorImpl.countFrames(
                             Arrays.asList(first.getAbsolutePath(), second.getAbsolutePath())));
    }

    @Test
    public void countsDeclaredFramesForMultiFrameObjects() throws Exception {
        final File multi = writeObject("multi.dcm", 5);

        assertEquals("a five frame object counts as five, not as one file",
                     Integer.valueOf(5),
                     SnapshotResourceGeneratorImpl.countFrames(
                             Collections.singletonList(multi.getAbsolutePath())));
    }

    /** A series is not guaranteed to be uniform, so the count is a sum rather than a multiplication. */
    @Test
    public void sumsAcrossAMixtureOfObjects() throws Exception {
        final File single = writeObject("mixed-single.dcm", null);
        final File three  = writeObject("mixed-three.dcm", 3);
        final File four   = writeObject("mixed-four.dcm", 4);

        assertEquals(Integer.valueOf(8),
                     SnapshotResourceGeneratorImpl.countFrames(
                             Arrays.asList(single.getAbsolutePath(), three.getAbsolutePath(), four.getAbsolutePath())));
    }

    /**
     * Nothing trustworthy to render from, so the caller has to hear about it rather than be handed a
     * count that silently omits whatever could not be read.
     */
    @Test
    public void returnsNullWhenAnObjectCannotBeRead() throws Exception {
        final File readable = writeObject("readable.dcm", null);
        final File missing  = new File(temporaryFolder.getRoot(), "not-here.dcm");

        assertNull(SnapshotResourceGeneratorImpl.countFrames(
                Arrays.asList(readable.getAbsolutePath(), missing.getAbsolutePath())));
    }

    /** A minimal object, optionally declaring Number of Frames. Pixel data is beside the point here. */
    private File writeObject(final String name, final Integer frames) throws Exception {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498." + name.hashCode());
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
