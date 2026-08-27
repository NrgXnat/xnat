package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageWriter;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Redacts a rectangle from encapsulated (compressed) pixel data.
 * <p>
 * Compressed pixels cannot be edited where they lie, so the object is decoded to uncompressed
 * frames, redacted by the same code that handles natively-encoded input, and then re-encoded. The
 * decode and encode both run through dcm4che's {@link Transcoder}, which processes a frame at a
 * time; the redaction streams. Memory stays flat, and the cost is transient scratch space for the
 * uncompressed form.
 * <p>
 * What it is re-encoded <em>as</em> depends on whether the source encoding was lossless:
 * <ul>
 *   <li><b>Lossless</b> (JPEG Lossless, JPEG-LS lossless, JPEG 2000 lossless) &mdash; back to the
 *       original transfer syntax. Re-encoding is exact, so nothing is lost and the object does not
 *       inflate. This needs an encoder for the syntax; where none is registered the object is
 *       stored uncompressed instead, with a warning. RLE Lossless is always in that position,
 *       since dcm4che ships an RLE reader but no RLE writer.</li>
 *   <li><b>Lossy</b> (JPEG Baseline, lossy JPEG 2000, &hellip;) &mdash; left uncompressed, because
 *       re-encoding would impose a second generation of loss across the whole image in order to
 *       redact one rectangle of it. The lossy compression history is recorded as required by
 *       PS3.3 C.7.6.1.1.5.</li>
 * </ul>
 * If the object cannot be decoded at all &mdash; no codec for the transfer syntax &mdash; this
 * fails. It must: returning an object whose pixels were never redacted would pass burned-in
 * identifiers through anonymization.
 */
final class EncapsulatedPixelRedactor {

    private static final Logger logger = LoggerFactory.getLogger(EncapsulatedPixelRedactor.class);

    private EncapsulatedPixelRedactor() {
    }

    /**
     * @param ds       the dataset, replaced in place with the redacted object.
     * @param dobj     the owning object, which takes ownership of the file holding the result.
     * @param rect     the requested rectangle, in image coordinates.
     * @param color    the requested fill, or null to fill with zero.
     * @param sourceTs transfer syntax of the compressed object.
     */
    static void redact(final Attributes ds, final DicomObjectI dobj, final Rectangle2D rect,
                       final Color color, final String sourceTs) throws IOException, MizerException {
        final List<File> temporary = new ArrayList<>();
        try {
            // Serialise the object as it stands. Header edits made by the script so far are in the
            // dataset, not in the file it was read from, so the source file will not do. Fragments
            // stream out of it, so this costs the compressed size.
            final File compressed = temporary(temporary);
            write(ds, compressed, sourceTs);

            final File decoded = temporary(temporary);
            transcode(compressed, decoded, UID.ExplicitVRLittleEndian, sourceTs);
            discard(compressed, temporary);

            // Re-read the decoded object: its pixel module now describes uncompressed frames, with
            // PhotometricInterpretation and PlanarConfiguration corrected for the decoded form.
            final Attributes decodedDs = read(decoded);

            final PixelGeometry geometry = PixelGeometry.of(decodedDs);
            final FrameRedactor redactor = new FrameRedactor(geometry, geometry.clip(rect),
                                                             geometry.fillSamples(color));
            final File pixels = StreamingRectanglePixelEditHandler.stageRedactedPixels(decodedDs, geometry, redactor);
            temporary.add(pixels);
            decodedDs.setValue(Tag.PixelData, VR.OW,
                               new BulkData(pixels.toURI().toString(), 0, pixels.length(), false));
            discard(decoded, temporary);

            final boolean lossy = TransferSyntaxType.isLossyCompression(sourceTs);
            if (!lossy && reencode(decodedDs, ds, dobj, sourceTs, temporary)) {
                return;
            }
            if (lossy) {
                recordLossyHistory(decodedDs, sourceTs);
            }
            decodedDs.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
            replace(ds, decodedDs);
            dobj.registerScratchFile(pixels);
            temporary.remove(pixels);
        } finally {
            for (final File file : temporary) {
                discard(file, null);
            }
        }
    }

    /**
     * Re-encodes the redacted object back to <b>targetTs</b> and adopts the result.
     *
     * @return false if there is no encoder for the transfer syntax, leaving the caller to store the
     *         object uncompressed instead. Decoding already succeeded by this point, so the pixels
     *         are redacted either way and only the container differs.
     */
    private static boolean reencode(final Attributes decodedDs, final Attributes ds, final DicomObjectI dobj,
                                    final String targetTs, final List<File> temporary) {
        if (!isWriterAvailable(targetTs)) {
            logger.warn("No image writer is available for transfer syntax {}; the redacted object will be "
                        + "stored uncompressed as Explicit VR Little Endian.", targetTs);
            return false;
        }
        try {
            final File staged = temporary(temporary);
            write(decodedDs, staged, UID.ExplicitVRLittleEndian);

            final File encoded = StreamingRectanglePixelEditHandler.createScratchFile();
            try {
                transcode(staged, encoded, targetTs, UID.ExplicitVRLittleEndian);
                discard(staged, temporary);
                // Registered only once the object holds a reference to it. Until then nothing else
                // will delete it, so any failure above has to discard it here.
                replace(ds, read(encoded));
                dobj.registerScratchFile(encoded);
            } catch (IOException | RuntimeException e) {
                discard(encoded, null);
                throw e;
            }
            return true;
        } catch (Exception e) {
            logger.warn("Unable to re-encode redacted pixel data as {}; the redacted object will be stored "
                        + "uncompressed as Explicit VR Little Endian.", targetTs, e);
            return false;
        }
    }

    /**
     * Decodes or encodes one object into another.
     *
     * @throws IOException if no codec is available, which for the decode direction means the
     *                     redaction cannot happen and the caller must not continue.
     */
    private static void transcode(final File source, final File destination, final String targetTs,
                                  final String sourceTs) throws IOException {
        try (Transcoder transcoder = new Transcoder(source)) {
            transcoder.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            transcoder.setIncludeFileMetaInformation(true);
            transcoder.setDestinationTransferSyntax(targetTs);
            transcoder.transcode((t, dataset) -> new FileOutputStream(destination));
        } catch (RuntimeException e) {
            // dcm4che reports a missing codec as an unchecked "No Reader/Writer for format" fault.
            throw new IOException("Unable to transcode pixel data from " + sourceTs + " to " + targetTs
                                  + ". A codec for the transfer syntax is required to redact pixels in a "
                                  + "compressed object.", e);
        }
    }

    private static void write(final Attributes ds, final File file, final String tsuid) throws IOException {
        try (DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(ds.createFileMetaInformation(tsuid), ds);
        }
    }

    /**
     * Reads a DICOM file keeping only the pixel data on disk, with the file meta information merged
     * in.
     * <p>
     * Restricting the descriptor to PixelData matters because these files are intermediates that get
     * deleted as soon as they are spent. Under the default descriptor every standard bulk data
     * element becomes a reference into the file &mdash; palette colour lookup tables, overlay data,
     * waveforms &mdash; and the dataset would outlive the file they point at. Those elements are
     * small, so reading them onto the heap costs nothing; the pixel data is the only value that has
     * to stay on disk.
     */
    private static Attributes read(final File file) throws IOException {
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            in.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
            final Attributes fmi     = in.readFileMetaInformation();
            final Attributes dataset = in.readDataset();
            if (fmi != null) {
                dataset.addAll(fmi);
            }
            return dataset;
        }
    }

    /** Swaps the contents of <b>target</b> for those of <b>source</b>, keeping the same instance. */
    private static void replace(final Attributes target, final Attributes source) {
        for (final int tag : target.tags()) {
            target.remove(tag);
        }
        target.addAll(source);
    }

    /**
     * Records that the pixel data has been through lossy compression, as required when a lossy
     * object is stored uncompressed.
     */
    private static void recordLossyHistory(final Attributes ds, final String sourceTs) {
        ds.setString(Tag.LossyImageCompression, VR.CS, "01");
        final String[] existing = ds.getStrings(Tag.LossyImageCompressionMethod);
        if (existing == null || existing.length == 0) {
            ds.setString(Tag.LossyImageCompressionMethod, VR.CS, lossyMethodOf(sourceTs));
        }
    }

    private static String lossyMethodOf(final String tsuid) {
        final TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        if (type == null) {
            return "ISO_10918_1";
        }
        switch (type) {
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

    private static File temporary(final List<File> temporary) throws IOException {
        final File file = StreamingRectanglePixelEditHandler.createScratchFile();
        temporary.add(file);
        return file;
    }

    /** Deletes a scratch file as soon as it is spent, so the uncompressed form is not held twice. */
    private static void discard(final File file, final List<File> temporary) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            logger.warn("Unable to delete pixel edit scratch file {}", file, e);
        }
        if (temporary != null) {
            temporary.remove(file);
        }
    }
}
