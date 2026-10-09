package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * A DICOM object whose pixel data exceeds 2 GB must survive being loaded, edited and written back.
 * <p>
 * {@code DicomInputStream.readValue()} reads a value into a {@code byte[]}, so past
 * {@link Integer#MAX_VALUE} it cannot represent one at all and throws "tag value too large" however
 * much heap is available. That is a hard ceiling rather than memory pressure, and reading bulk data
 * as {@link BulkData} references is what gets past it: dcm4che carries their offsets and lengths as
 * longs specifically to cover the 2-4 GB range, and the value is streamed from the source file
 * straight to the output on write.
 * <p>
 * Opt in, since the object has to be built to be tested and there is no way to build a 2 GB object
 * cheaply:
 * <pre>
 *   ./gradlew :mizer:test -PlargeObjectTests
 * </pre>
 */
public class LargeDicomObjectTest {

    /** Just past the 2^31 - 1 ceiling: enough to prove the point, no larger than it has to be. */
    private static final long PIXEL_DATA_LENGTH = 2_200_000_000L;
    private static final int  ROWS = 1024, COLUMNS = 1024;
    /** Source plus output, with room to spare. */
    private static final long REQUIRED_FREE_BYTES = 6L * 1024 * 1024 * 1024;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void anonymizesAnObjectLargerThanTwoGigabytes() throws Exception {
        assumeTrue("set -PlargeObjectTests to run", Boolean.getBoolean("test.largeObjects"));

        final File source = temporaryFolder.newFile("large-source.dcm");
        assumeTrue("needs " + REQUIRED_FREE_BYTES / (1024 * 1024) + " MB free",
                   source.getParentFile().getUsableSpace() > REQUIRED_FREE_BYTES);
        writeOversizedObject(source);
        final String pixelsBefore = digestPixelData(source);

        // The ceiling this exists to get past. Reading bulk data onto the heap cannot represent a
        // value this large, so it fails outright rather than merely needing a bigger heap.
        try {
            DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.YES);
            fail("expected reading bulk data onto the heap to fail past 2 GB");
        } catch (MizerException expected) {
            assertTrue("expected a 'value too large' failure, got: " + expected.getCause(),
                       String.valueOf(expected.getCause()).contains("too large"));
        }

        // What mizer does now: bulk data stays in the source file and is streamed through on write.
        final DicomObjectI dicomObject =
                DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        assertTrue("pixel data should be a reference, not a value on the heap",
                   dicomObject.getAttributes().getValue(Tag.PixelData) instanceof BulkData);

        dicomObject.getAttributes().setString(Tag.PatientName, VR.PN, "ANON^SUBJECT");
        dicomObject.getAttributes().remove(Tag.PatientBirthDate);

        final File edited = temporaryFolder.newFile("large-edited.dcm");
        try (OutputStream out = new FileOutputStream(edited)) {
            dicomObject.write(out);
        }
        dicomObject.releaseScratchFiles();

        final Attributes written = readHeader(edited);
        assertEquals("the header edit should have been applied", "ANON^SUBJECT",
                     written.getString(Tag.PatientName));
        assertTrue("the removed element should be gone", !written.contains(Tag.PatientBirthDate));
        assertEquals("pixel data must survive the round trip untouched",
                     pixelsBefore, digestPixelData(edited));
    }

    /**
     * Writes a DICOM whose PixelData value length exceeds what an int can hold.
     * <p>
     * Hand-encoded, because building it through {@link Attributes} would mean holding the value in a
     * {@code byte[]}, which is the very thing that cannot be done at this size.
     */
    private void writeOversizedObject(final File file) throws IOException {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.101");
        dataset.setString(Tag.StudyInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.102");
        dataset.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.103");
        dataset.setString(Tag.PatientName, VR.PN, "REAL^PATIENT^NAME");
        dataset.setString(Tag.PatientID, VR.LO, "MRN-0001234");
        dataset.setString(Tag.PatientBirthDate, VR.DA, "19700101");
        dataset.setString(Tag.Modality, VR.CS, "OT");
        dataset.setInt(Tag.Rows, VR.US, ROWS);
        dataset.setInt(Tag.Columns, VR.US, COLUMNS);
        dataset.setInt(Tag.BitsAllocated, VR.US, 16);
        dataset.setInt(Tag.BitsStored, VR.US, 16);
        dataset.setInt(Tag.HighBit, VR.US, 15);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setInt(Tag.PixelRepresentation, VR.US, 0);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        dataset.setInt(Tag.NumberOfFrames, VR.IS, (int) (PIXEL_DATA_LENGTH / ((long) ROWS * COLUMNS * 2)));

        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos, 1 << 20)) {
            try (DicomOutputStream dos = new DicomOutputStream(bos, UID.ExplicitVRLittleEndian)) {
                dos.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
                dos.flush();

                // Explicit VR OW header: tag, VR, two reserved bytes, then a 32-bit length. The
                // length here does not fit in a signed int, which is the whole point.
                final byte[] header = new byte[12];
                ByteUtils.tagToBytes(Tag.PixelData, header, 0, false);
                header[4] = 'O';
                header[5] = 'W';
                ByteUtils.intToBytes((int) PIXEL_DATA_LENGTH, header, 8, false);
                bos.write(header);

                final byte[] buffer = new byte[1 << 20];
                for (int index = 0; index < buffer.length; index++) {
                    buffer[index] = (byte) (index * 31 + 7);
                }
                for (long written = 0; written < PIXEL_DATA_LENGTH; ) {
                    final int count = (int) Math.min(buffer.length, PIXEL_DATA_LENGTH - written);
                    bos.write(buffer, 0, count);
                    written += count;
                }
            }
        }
    }

    private static Attributes readHeader(final File file) throws IOException {
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            final Attributes fmi     = in.readFileMetaInformation();
            final Attributes dataset = in.readDataset();
            if (fmi != null) {
                dataset.addAll(fmi);
            }
            return dataset;
        }
    }

    /** Digests the pixel data without ever holding it, which is the only way at this size. */
    private static String digestPixelData(final File file) throws Exception {
        final Attributes dataset;
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.readFileMetaInformation();
            dataset = in.readDataset();
        }
        final BulkData      pixels = (BulkData) dataset.getValue(Tag.PixelData);
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[]        buffer = new byte[1 << 16];
        try (InputStream in = pixels.openStream()) {
            for (long remaining = pixels.longLength(); remaining > 0; ) {
                final int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) {
                    throw new EOFException("pixel data ended " + remaining + " bytes early");
                }
                digest.update(buffer, 0, read);
                remaining -= read;
            }
        }
        final StringBuilder hex = new StringBuilder();
        for (final byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
