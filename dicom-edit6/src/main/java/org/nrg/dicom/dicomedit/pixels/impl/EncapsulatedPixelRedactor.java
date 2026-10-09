package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.nrg.dicom.dicomedit.pixels.ImageWriters;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
     * @param ds             the dataset, replaced in place with the redacted object.
     * @param dobj           the owning object, which takes ownership of the file holding the result.
     * @param sourceGeometry the pixel module read off the compressed object, whose dimensions
     *                       already describe the decoded image; only the layout changes on decoding.
     * @param rect           the requested rectangle, in image coordinates.
     * @param color          the requested fill, or null to fill with zero.
     * @param sourceTs       transfer syntax of the compressed object.
     */
    static void redact(final Attributes ds, final DicomObjectI dobj, final PixelGeometry sourceGeometry,
                       final Rectangle2D rect, final Color color, final String sourceTs)
            throws IOException, MizerException {
        if (sourceGeometry.pixelDataTag != Tag.PixelData) {
            // Float Pixel Data and Double Float Pixel Data are not permitted with an encapsulated
            // transfer syntax, so this is a malformed object rather than one to guess at. Left
            // explicit because everything below reads (7FE0,0010) and would otherwise find nothing.
            throw new MizerException("Floating point pixel data cannot be encapsulated, so this "
                                     + "object's transfer syntax and pixel data element disagree, "
                                     + "cannot alter pixels.");
        }
        if (sourceGeometry.decodesTooLongToExpress()) {
            // Checked before decoding rather than after: the transcoder would write a wrapped
            // length and drop most of the frames without failing, and the redaction would report
            // success on an object that had quietly lost its pixel data.
            throw new MizerException("Redacting this object means decoding " + sourceGeometry.frames
                                     + " frames of " + sourceGeometry.frameLength + " bytes, which is longer "
                                     + "than a pixel data element can express, cannot alter pixels.");
        }

        final List<File> temporary = new ArrayList<>();
        try {
            // Serialise the object as it stands. Header edits made by the script so far are in the
            // dataset, not in the file it was read from, so the source file will not do. Fragments
            // stream out of it, so this costs the compressed size.
            final long compressedLength = compressedLength(ds);
            final File compressed = temporary(temporary);
            write(ds, compressed, sourceTs);

            final File decoded = temporary(temporary);
            transcode(compressed, decoded, UID.ExplicitVRLittleEndian, sourceTs);
            discard(compressed, temporary);

            // Re-read the decoded object: its pixel module now describes uncompressed frames, with
            // PhotometricInterpretation and PlanarConfiguration corrected for the decoded form.
            final Attributes decodedDs = read(decoded);

            final PixelGeometry geometry = PixelGeometry.of(decodedDs);
            // Decoding normally leaves RGB behind, but the codecs that do not touch
            // PhotometricInterpretation -- RLE among them -- can leave a sub-sampled layout here.
            geometry.requireChromaWithinLines();
            final LineRedactor redactor = new LineRedactor(geometry, geometry.clip(rect),
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
                recordLossyHistory(decodedDs, sourceTs, pixels.length(), compressedLength);
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
        if (!ImageWriters.isAvailable(targetTs)) {
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
        } catch (RuntimeException | LinkageError e) {
            // dcm4che reports a missing codec as an unchecked "No Reader/Writer for format" fault.
            // A codec whose native library is absent reports it as an Error rather than an Exception
            // -- OpenCV throws UnsatisfiedLinkError from inside the decode -- and an Error passes
            // through every catch between here and the importer, so the object fails the archive
            // with a stack trace about org.opencv.core.Mat instead of the reason above.
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
     * <p>
     * Lossy Image Compression Method and Ratio pair up in order, one value per compression step, and
     * values already present are never changed. This step's method is appended unless the last one
     * recorded is already this step's, as when the encoder wrote the method itself. A ratio is
     * appended only when every earlier step has one, so it cannot end up beside the wrong method.
     *
     * @param decodedLength    bytes of decoded pixel data.
     * @param compressedLength bytes of compressed pixel data, or a negative number if unknown, in
     *                         which case no ratio is written.
     */
    private static void recordLossyHistory(final Attributes ds, final String sourceTs,
                                           final long decodedLength, final long compressedLength) {
        ds.setString(Tag.LossyImageCompression, VR.CS, "01");
        final String       method  = lossyMethodOf(sourceTs);
        final List<String> methods = valuesOf(ds, Tag.LossyImageCompressionMethod);
        final List<String> ratios  = valuesOf(ds, Tag.LossyImageCompressionRatio);
        if (methods.isEmpty() || !method.equals(methods.get(methods.size() - 1))) {
            methods.add(method);
            ds.setString(Tag.LossyImageCompressionMethod, VR.CS, methods.toArray(new String[0]));
        }
        if (ratios.size() == methods.size() - 1 && compressedLength > 0) {
            ratios.add(String.format(Locale.ROOT, "%.3f", (double) decodedLength / compressedLength));
            ds.setString(Tag.LossyImageCompressionRatio, VR.DS, ratios.toArray(new String[0]));
        }
    }

    /**
     * Bytes of compressed pixel data: every fragment after the Basic Offset Table. Negative if the
     * value is not encapsulated fragments or a fragment's length cannot be read.
     */
    private static long compressedLength(final Attributes ds) {
        final Object value = ds.getValue(Tag.PixelData);
        if (!(value instanceof Fragments)) {
            return -1;
        }
        final Fragments fragments = (Fragments) value;
        long total = 0;
        for (int i = 1; i < fragments.size(); i++) {
            final Object fragment = fragments.get(i);
            if (fragment instanceof byte[]) {
                total += ((byte[]) fragment).length;
            } else if (fragment instanceof BulkData) {
                total += ((BulkData) fragment).longLength();
            } else if (fragment != Value.NULL) {
                return -1;
            }
        }
        return total;
    }

    private static List<String> valuesOf(final Attributes ds, final int tag) {
        final String[] values = ds.getStrings(tag);
        return values == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(values));
    }

    /**
     * The Lossy Image Compression Method defined term (PS3.3 C.7.6.1.1.5.1) for a lossy transfer
     * syntax. Keyed by UID rather than {@link TransferSyntaxType}, which files HTJ2K under JPEG 2000
     * and every video syntax under MPEG.
     */
    static String lossyMethodOf(final String tsuid) {
        switch (tsuid) {
            case UID.JPEGLSNearLossless:
                return "ISO_14495_1";
            case UID.JPEG2000:
            case UID.JPEG2000MC:
                return "ISO_15444_1";
            case UID.HTJ2K:
                return "ISO_15444_15";
            case UID.JPEGXL:
                return "ISO_18181_1";
            case UID.MPEG2MPML:
            case UID.MPEG2MPMLF:
            case UID.MPEG2MPHL:
            case UID.MPEG2MPHLF:
                return "ISO_13818_2";
            case UID.MPEG4HP41:
            case UID.MPEG4HP41F:
            case UID.MPEG4HP41BD:
            case UID.MPEG4HP41BDF:
            case UID.MPEG4HP422D:
            case UID.MPEG4HP422DF:
            case UID.MPEG4HP423D:
            case UID.MPEG4HP423DF:
            case UID.MPEG4HP42STEREO:
            case UID.MPEG4HP42STEREOF:
                return "ISO_14496_10";
            case UID.HEVCMP51:
            case UID.HEVCM10P51:
                return "ISO_23008_2";
            default:
                // Lossy JPEG, and JPEG XL recompression of a JPEG, whose loss is the JPEG's.
                return "ISO_10918_1";
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
