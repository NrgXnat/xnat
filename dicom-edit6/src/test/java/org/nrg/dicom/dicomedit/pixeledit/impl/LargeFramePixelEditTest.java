package org.nrg.dicom.dicomedit.pixeledit.impl;

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
import org.nrg.dicom.dicomedit.pixels.impl.StreamingRectanglePixelEditHandler;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * A single frame larger than a {@code byte[]} can hold must be redacted in the right bytes.
 * <p>
 * Redaction buffers one line of the image, so the size of a frame is no longer a limit and there is
 * nothing left to refuse below the 4 GB ceiling a pixel data element itself imposes. That claim is
 * cheap to make and only this proves it: the object here declares a single 65535x33000 frame, so
 * {@code rows * columns} is past {@link Integer#MAX_VALUE} and buffering a frame could not have
 * represented it at all, let alone held it.
 * <p>
 * Every byte of the output is checked, not a sample of them: the failure this guards against is a
 * rectangle landing at the wrong offset, which shows up as bytes that should have been left alone
 * and were not, somewhere no spot check would look.
 * <p>
 * Opt in, since the object has to be built to be tested:
 * <pre>
 *   ./gradlew :dicom-edit6:test -PlargeObjectTests
 * </pre>
 */
public class LargeFramePixelEditTest {

    /** 65535 rows of 33000 bytes: 2_162_655_000, just past the 2^31 - 1 a byte[] index stops at. */
    private static final int  ROWS = 65535, COLUMNS = 33000;
    private static final long FRAME_LENGTH = (long) ROWS * COLUMNS;
    /** Source, scratch and output, with room to spare. */
    private static final long REQUIRED_FREE_BYTES = 8L * 1024 * 1024 * 1024;

    /** Redacted region, chosen to span several lines so the line stepping has to be right. */
    private static final int   RECT_X = 10, RECT_Y = 5, RECT_WIDTH = 100, RECT_HEIGHT = 3;
    private static final int   FILL   = 7;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final StreamingRectanglePixelEditHandler handler = new StreamingRectanglePixelEditHandler();

    @Test
    public void redactsAFrameLargerThanAByteArray() throws Exception {
        assumeTrue("set -PlargeObjectTests to run", Boolean.getBoolean("test.largeObjects"));

        final File source = temporaryFolder.newFile("large-frame-source.dcm");
        assumeTrue("needs " + REQUIRED_FREE_BYTES / (1024 * 1024) + " MB free",
                   source.getParentFile().getUsableSpace() > REQUIRED_FREE_BYTES);
        writeSingleOversizedFrame(source);

        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        handler.process(new Rectangle2D.Float(RECT_X, RECT_Y, RECT_WIDTH, RECT_HEIGHT),
                        new Color(FILL, FILL, FILL), dobj);

        final File output = temporaryFolder.newFile("large-frame-output.dcm");
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();

        verifyEveryByte(output);
    }

    /**
     * Writes a DICOM with one frame whose length exceeds what an int can hold.
     * <p>
     * Hand-encoded for the same reason {@code LargeDicomObjectTest} does it: building the value
     * through {@link Attributes} would mean holding it in a {@code byte[]}.
     */
    private void writeSingleOversizedFrame(final File file) throws IOException {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.201");
        dataset.setString(Tag.StudyInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.202");
        dataset.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.203");
        dataset.setString(Tag.Modality, VR.CS, "OT");
        dataset.setInt(Tag.Rows, VR.US, ROWS);
        dataset.setInt(Tag.Columns, VR.US, COLUMNS);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        dataset.setInt(Tag.BitsAllocated, VR.US, 8);
        dataset.setInt(Tag.BitsStored, VR.US, 8);
        dataset.setInt(Tag.HighBit, VR.US, 7);
        dataset.setInt(Tag.PixelRepresentation, VR.US, 0);

        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos, 1 << 20)) {
            try (DicomOutputStream dos = new DicomOutputStream(bos, UID.ExplicitVRLittleEndian)) {
                dos.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
                dos.flush();

                // Explicit VR OB header: tag, VR, two reserved bytes, then a 32-bit length that does
                // not fit in a signed int.
                final byte[] header = new byte[12];
                ByteUtils.tagToBytes(Tag.PixelData, header, 0, false);
                header[4] = 'O';
                header[5] = 'B';
                ByteUtils.intToBytes((int) FRAME_LENGTH, header, 8, false);
                bos.write(header);

                final byte[] buffer = new byte[1 << 20];
                for (long written = 0; written < FRAME_LENGTH; ) {
                    final int count = (int) Math.min(buffer.length, FRAME_LENGTH - written);
                    for (int index = 0; index < count; index++) {
                        buffer[index] = (byte) sourceByte(written + index);
                    }
                    bos.write(buffer, 0, count);
                    written += count;
                }
            }
        }
    }

    /**
     * Streams the redacted pixel data and compares every byte with what it should be.
     * <p>
     * Counted rather than asserted per byte: two billion assertions cost more than the redaction
     * being verified. The first mismatch is reported, which is the one worth seeing.
     */
    private static void verifyEveryByte(final File file) throws Exception {
        final Attributes dataset;
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.readFileMetaInformation();
            dataset = in.readDataset();
        }
        final BulkData pixels = (BulkData) dataset.getValue(Tag.PixelData);
        assertEquals("the pixel data should be exactly as long as it was", FRAME_LENGTH, pixels.longLength());

        long   mismatches = 0, firstMismatch = -1, redactedFromSomethingElse = 0;
        int    wanted = 0, found = 0;
        final byte[] buffer = new byte[1 << 16];
        try (InputStream in = pixels.openStream()) {
            for (long at = 0; at < FRAME_LENGTH; ) {
                final int read = in.read(buffer, 0, (int) Math.min(buffer.length, FRAME_LENGTH - at));
                if (read < 0) {
                    throw new EOFException("pixel data ended " + (FRAME_LENGTH - at) + " bytes early");
                }
                for (int index = 0; index < read; index++) {
                    final long offset   = at + index;
                    final int  source   = sourceByte(offset);
                    final int  expected = isRedacted(offset) ? FILL : source;
                    final int  actual   = buffer[index] & 0xFF;
                    if (actual != expected) {
                        if (firstMismatch < 0) {
                            firstMismatch = offset;
                            wanted        = expected;
                            found         = actual;
                        }
                        mismatches++;
                    }
                    if (isRedacted(offset) && source != FILL) {
                        redactedFromSomethingElse++;
                    }
                }
                at += read;
            }
        }

        assertEquals("byte " + firstMismatch + " (row " + (firstMismatch < 0 ? -1 : firstMismatch / COLUMNS)
                     + ", column " + (firstMismatch < 0 ? -1 : firstMismatch % COLUMNS) + ") was " + found
                     + ", should be " + wanted + "; " + mismatches + " bytes wrong in all",
                     0, mismatches);
        // Without this, a region that already held the fill value would satisfy the comparison above
        // whether or not any redaction took place.
        assertTrue("the region should not already have held the fill value throughout",
                   redactedFromSomethingElse > 0);
    }

    private static boolean isRedacted(final long offset) {
        final long row    = offset / COLUMNS;
        final long column = offset % COLUMNS;
        return row >= RECT_Y && row < RECT_Y + RECT_HEIGHT
               && column >= RECT_X && column < RECT_X + RECT_WIDTH;
    }

    /** The source pattern, a pure function of position, so verifying needs no second copy of it. */
    private static int sourceByte(final long offset) {
        // Prime, so the pattern does not line up with the row length and a rectangle written one row
        // out lands on bytes that differ.
        return (int) (offset % 251);
    }
}
