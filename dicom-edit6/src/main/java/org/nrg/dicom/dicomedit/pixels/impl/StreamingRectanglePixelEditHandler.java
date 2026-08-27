package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link org.nrg.dicom.dicomedit.pixels.PixelEditHandler} that redacts a solid rectangle without
 * ever holding more than one frame in memory.
 * <p>
 * The edited pixels are written to a scratch file and the dataset's PixelData value is replaced by
 * a {@link BulkData} reference to it, so the object stays streamable all the way to
 * {@link DicomObjectI#write}. The scratch file is registered with the object and removed by
 * {@link DicomObjectI#releaseScratchFiles()} once it has been written out.
 * <p>
 * Two properties differ from the pixelmed-based handler this replaces, both deliberate:
 * <ul>
 *   <li>The transfer syntax is preserved, rather than every object being rewritten as Explicit VR
 *       Little Endian. Decompressing a lossless-compressed object inflates it several-fold for no
 *       gain in fidelity, and undoes any pre-compression applied on import.</li>
 *   <li>The fill value the script asks for is honoured. The previous handler always filled with
 *       zero, silently ignoring {@code v=}/{@code r=,g=,b=}.</li>
 * </ul>
 * Lossy-compressed input is the exception to the first point: re-encoding it would impose a second
 * generation of loss on the whole image, including everything outside the redacted rectangle, so it
 * is written out uncompressed with the lossy compression history recorded.
 */
public class StreamingRectanglePixelEditHandler extends SimpleRectanglePixelEditHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreamingRectanglePixelEditHandler.class);

    /** Where edited pixel data is staged. Sized for the pixel data, so not always suited to /tmp. */
    static final String SCRATCH_DIR_PROPERTY = "dicom.pixeledit.scratch.dir";

    @Override
    public void process(final Rectangle2D rect, final Color color, final DicomObjectI dobj) throws MizerException {
        final Attributes ds = dobj.getAttributes();
        if (!ds.contains(Tag.PixelData)) {
            logger.warn("Skipping DICOM object with no pixel data, SOP Instance UID {}",
                        ds.getString(Tag.SOPInstanceUID));
            return;
        }

        final PixelGeometry geometry = PixelGeometry.of(ds);
        final Rectangle     clipped  = geometry.clip(rect);
        if (clipped.isEmpty()) {
            logger.warn("Redacted region {} does not intersect the {}x{} image with SOP Instance UID {}",
                        rect, geometry.columns, geometry.rows, ds.getString(Tag.SOPInstanceUID));
            return;
        }

        final String             sourceTs = sourceTransferSyntax(ds);
        final TransferSyntaxType tsType   = TransferSyntaxType.forUID(sourceTs);

        try {
            if (tsType == null || !tsType.isPixeldataEncapsulated()) {
                redactNative(ds, dobj, geometry, new FrameRedactor(geometry, clipped, geometry.fillSamples(color)));
            } else {
                // Decoding can change the pixel layout, so the encapsulated path rebuilds the
                // geometry for itself rather than reusing what was read off the compressed object.
                EncapsulatedPixelRedactor.redact(ds, dobj, rect, color, sourceTs);
            }
        } catch (IOException e) {
            throw new MizerException("Error editing rectangular pixel region.", e);
        }
    }

    /**
     * Streams every frame through the redactor and repoints PixelData at the result.
     * <p>
     * The transfer syntax is untouched: the bytes written are in the same encoding as the bytes
     * read, so an Implicit VR Little Endian object stays implicit.
     */
    private void redactNative(final Attributes ds, final DicomObjectI dobj,
                              final PixelGeometry geometry, final FrameRedactor redactor) throws IOException {
        final File scratch = stageRedactedPixels(ds, geometry, redactor);
        repointPixelData(ds, dobj, scratch, scratch.length(), ds.getVR(Tag.PixelData), geometry.bigEndian);
    }

    /**
     * Writes every frame of uncompressed pixel data through the redactor into a new scratch file and
     * returns it, leaving <b>ds</b> alone.
     * <p>
     * This is the single implementation of the edit itself. The encapsulated path decodes to
     * uncompressed frames and then comes through here too, so a rectangle lands in the same place
     * whatever the object arrived as.
     */
    static File stageRedactedPixels(final Attributes ds, final PixelGeometry geometry,
                                    final FrameRedactor redactor) throws IOException {
        final Object value       = ds.getValue(Tag.PixelData);
        final long   valueLength = nativeValueLength(value);
        final File   scratch     = createScratchFile();

        try {
            long copied = 0;
            try (InputStream in = openNative(value);
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(scratch), 1 << 16)) {
                final byte[] frame = new byte[(int) geometry.frameLength];
                // Until the value is consumed, rather than until NumberOfFrames is reached: pixel
                // data longer than the frame count accounts for still has to be redacted, or those
                // extra frames pass through carrying whatever was burned into them. A trailing pad
                // byte is shorter than the rectangle, so the redactor leaves it alone.
                while (copied < valueLength) {
                    final int read = readFrame(in, frame, (int) Math.min(frame.length, valueLength - copied));
                    if (read <= 0) {
                        break;
                    }
                    redactor.redact(frame, read);
                    out.write(frame, 0, read);
                    copied += read;
                }
            }

            if (copied != valueLength) {
                throw new IOException("Redacted " + copied + " of " + valueLength + " pixel data bytes");
            }
            return scratch;
        } catch (IOException | RuntimeException e) {
            // Running out of space is the likely failure here, since a pixel edit needs the
            // object's size again in scratch, and leaving a partial file behind compounds it.
            Files.deleteIfExists(scratch.toPath());
            throw e;
        }
    }

    /** Replaces PixelData with a reference to <b>scratch</b> and ties that file to the object. */
    static void repointPixelData(final Attributes ds, final DicomObjectI dobj, final File scratch,
                                 final long length, final VR vr, final boolean bigEndian) {
        ds.setValue(Tag.PixelData, vr == null ? VR.OW : vr,
                    new BulkData(scratch.toURI().toString(), 0, length, bigEndian));
        dobj.registerScratchFile(scratch);
    }

    static File createScratchFile() throws IOException {
        final String configured = System.getProperty(SCRATCH_DIR_PROPERTY);
        if (configured == null) {
            return Files.createTempFile("pixeledit", ".pixels").toFile();
        }
        final Path directory = Paths.get(configured);
        try {
            // createDirectories rather than mkdirs: it succeeds when the directory is already there,
            // so two imports creating it at the same moment cannot race one of them into a failure.
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IOException("Pixel edit scratch directory " + directory + ", from "
                                  + SCRATCH_DIR_PROPERTY + ", does not exist and cannot be created", e);
        }
        return Files.createTempFile(directory, "pixeledit", ".pixels").toFile();
    }

    /**
     * The transfer syntax of the object being edited. Mizer merges the file meta information into
     * the dataset, so it is readable from there.
     */
    private static String sourceTransferSyntax(final Attributes ds) {
        return ds.getString(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
    }

    private static long nativeValueLength(final Object value) throws IOException {
        if (value instanceof BulkData) {
            return ((BulkData) value).longLength();
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        throw new IOException("Unexpected native pixel data value " + describe(value));
    }

    private static InputStream openNative(final Object value) throws IOException {
        if (value instanceof BulkData) {
            return ((BulkData) value).openStream();
        }
        if (value instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) value);
        }
        throw new IOException("Unexpected native pixel data value " + describe(value));
    }

    /** Reads up to <b>wanted</b> bytes, tolerating a short final frame rather than throwing. */
    private static int readFrame(final InputStream in, final byte[] frame, final int wanted) throws IOException {
        int filled = 0;
        while (filled < wanted) {
            final int read = in.read(frame, filled, wanted - filled);
            if (read < 0) {
                break;
            }
            filled += read;
        }
        return filled;
    }

    private static String describe(final Object value) {
        if (value instanceof Fragments) {
            return "encapsulated in " + ((Fragments) value).size() + " fragments";
        }
        return value == null || value == Value.NULL ? "of null" : "of type " + value.getClass().getName();
    }

}
