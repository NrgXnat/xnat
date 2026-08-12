package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Redacts a rectangle from encapsulated (compressed) pixel data, one frame at a time.
 * <p>
 * Compressed pixels cannot be edited where they lie, so each frame is decoded, redacted, and staged
 * as raw bytes. What happens next depends on whether the source encoding is lossless:
 * <ul>
 *   <li><b>Lossless</b> (JPEG Lossless, JPEG-LS lossless, JPEG 2000 lossless, RLE) &mdash; the
 *       staged pixels are re-encoded back to the original transfer syntax. Re-encoding is exact, so
 *       nothing is lost and the object does not inflate.</li>
 *   <li><b>Lossy</b> (JPEG Baseline, lossy JPEG 2000, &hellip;) &mdash; the object is written out
 *       uncompressed, because re-encoding would impose a second generation of loss across the whole
 *       image to redact one rectangle of it. The lossy compression history is recorded as required
 *       by PS3.3 C.7.6.1.1.5.</li>
 * </ul>
 * Only one frame is resident at a time in either case.
 */
final class EncapsulatedPixelRedactor {

    private static final Logger logger = LoggerFactory.getLogger(EncapsulatedPixelRedactor.class);

    private EncapsulatedPixelRedactor() {
    }

    /**
     * @param ds       the dataset, mutated in place.
     * @param dobj     the owning object, which takes ownership of the staged pixel file.
     * @param rect     the requested rectangle, in image coordinates.
     * @param color    the requested fill, or null to fill with zero.
     * @param sourceTs transfer syntax of the compressed object.
     */
    static void redact(final Attributes ds, final DicomObjectI dobj, final Rectangle2D rect,
                       final Color color, final String sourceTs)
            throws IOException, MizerException {

        final Decompressor decompressor = new Decompressor(ds, sourceTs);
        try {
            // Lazy: installs a streaming PixelData value and corrects PhotometricInterpretation and
            // PlanarConfiguration for the decoded form. No pixels are read yet.
            decompressor.decompress();

            // Built from the *decoded* geometry, since decompress() may have just changed
            // PlanarConfiguration -- a colour frame can be planar on the wire and interleaved once
            // decoded, and the two put a rectangle in different byte ranges.
            final PixelGeometry decoded  = PixelGeometry.of(ds);
            final FrameRedactor redactor = new FrameRedactor(decoded, decoded.clip(rect),
                                                            decoded.fillSamples(color));

            final File scratch = StreamingRectanglePixelEditHandler.createScratchFile();
            final long staged  = stageRedactedFrames(decompressor, decoded, redactor, scratch);

            final boolean lossy = TransferSyntaxType.isLossyCompression(sourceTs);
            if (!lossy && recompress(ds, dobj, decoded, scratch, staged, sourceTs)) {
                return;
            }
            if (lossy) {
                recordLossyHistory(ds, sourceTs);
            }
            ds.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
            StreamingRectanglePixelEditHandler.repointPixelData(ds, dobj, scratch, staged,
                                                                VR.OW, false);
        } finally {
            decompressor.dispose();
        }
    }

    /** Decodes, redacts and writes every frame, returning the total number of bytes staged. */
    private static long stageRedactedFrames(final Decompressor decompressor, final PixelGeometry geometry,
                                            final FrameRedactor redactor, final File scratch) throws IOException {
        long staged = 0;
        try (ImageInputStream source = decompressor.createImageInputStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(scratch), 1 << 16)) {
            final ByteArrayOutputStream frame = new ByteArrayOutputStream((int) geometry.frameLength);
            for (int index = 0; index < geometry.frames; index++) {
                frame.reset();
                decompressor.writeFrameTo(source, index, frame);
                final byte[] bytes = frame.toByteArray();
                redactor.redact(bytes, bytes.length);
                out.write(bytes);
                staged += bytes.length;
            }
            if ((staged & 1) != 0) {
                // DICOM values are even-length; pad as dcm4che would.
                out.write(0);
                staged++;
            }
        }
        return staged;
    }

    /**
     * Re-encodes the staged pixels back to <b>targetTs</b>, replacing the dataset's pixel data on
     * success.
     *
     * @return false if no encoder is available, leaving the dataset untouched for the caller to
     *         write out uncompressed.
     */
    private static boolean recompress(final Attributes ds, final DicomObjectI dobj, final PixelGeometry geometry,
                                      final File scratch, final long staged, final String targetTs) {
        if (!isWriterAvailable(targetTs)) {
            logger.warn("No image writer is available for transfer syntax {}; the redacted object will be "
                        + "written uncompressed as Explicit VR Little Endian.", targetTs);
            return false;
        }

        File nativeFile = null;
        File encoded    = null;
        try {
            // Transcoder works from a file, so stage the redacted object natively first.
            nativeFile = StreamingRectanglePixelEditHandler.createScratchFile();
            ds.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
            ds.setValue(Tag.PixelData, VR.OW,
                        new org.dcm4che3.data.BulkData(scratch.toURI().toString(), 0, staged, false));
            try (DicomOutputStream out = new DicomOutputStream(nativeFile)) {
                out.writeDataset(ds.createFileMetaInformation(UID.ExplicitVRLittleEndian), ds);
            }

            encoded = StreamingRectanglePixelEditHandler.createScratchFile();
            final File destination = encoded;
            try (Transcoder transcoder = new Transcoder(nativeFile)) {
                transcoder.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                transcoder.setIncludeFileMetaInformation(true);
                transcoder.setDestinationTransferSyntax(targetTs);
                transcoder.transcode((t, dataset) -> new FileOutputStream(destination));
            }

            adoptEncoded(ds, dobj, encoded);
            encoded = null;   // now owned by dobj
            return true;
        } catch (Exception e) {
            logger.warn("Unable to re-encode redacted pixel data as {}; the redacted object will be written "
                        + "uncompressed as Explicit VR Little Endian.", targetTs, e);
            ds.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
            return false;
        } finally {
            deleteQuietly(nativeFile);
            deleteQuietly(encoded);
        }
    }

    /** Replaces the dataset's contents with the re-encoded object, keeping its pixels on disk. */
    private static void adoptEncoded(final Attributes ds, final DicomObjectI dobj, final File encoded)
            throws IOException {
        final Attributes reloaded;
        final Attributes fmi;
        try (DicomInputStream in = new DicomInputStream(encoded)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            fmi      = in.readFileMetaInformation();
            reloaded = in.readDataset();
        }
        for (final int tag : ds.tags()) {
            ds.remove(tag);
        }
        ds.addAll(reloaded);
        if (fmi != null) {
            ds.addAll(fmi);
        }
        // The dataset's bulk data now lives in the re-encoded file, so its lifetime is the object's.
        dobj.registerScratchFile(encoded);
    }

    /**
     * Records that the pixel data has been through lossy compression, as required when a lossy
     * object is decompressed and stored uncompressed.
     */
    private static void recordLossyHistory(final Attributes ds, final String sourceTs) {
        ds.setString(Tag.LossyImageCompression, VR.CS, "01");
        final String[] existing = ds.getStrings(Tag.LossyImageCompressionMethod);
        if (existing == null || existing.length == 0) {
            ds.setString(Tag.LossyImageCompressionMethod, VR.CS, lossyMethodOf(sourceTs));
        }
    }

    private static String lossyMethodOf(final String tsuid) {
        switch (TransferSyntaxType.forUID(tsuid)) {
            case JPEG_2000:
                return "ISO_15444_1";
            case JPEG_LS:
                return "ISO_14495_1";
            case MPEG:
                return "ISO_13818_2";
            default:
                return "ISO_10918_1";
        }
    }

    private static boolean isWriterAvailable(final String tsuid) {
        try {
            final ImageWriterFactory.ImageWriterParam param = ImageWriterFactory.getImageWriterParam(tsuid);
            if (param == null) {
                return false;
            }
            final ImageWriter writer = ImageWriterFactory.getImageWriter(param);
            writer.dispose();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void deleteQuietly(final File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                logger.warn("Unable to delete pixel edit scratch file {}", file, e);
            }
        }
    }
}
