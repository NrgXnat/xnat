package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.image.YBR;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * The pixel module attributes needed to locate a rectangle inside raw, uncompressed frame bytes.
 *
 * @see StreamingRectanglePixelEditHandler
 */
final class PixelGeometry {

    /**
     * The longest pixel data value a DICOM object can express: an element carries its length in
     * four bytes, and the all-ones value is reserved for an undefined length.
     */
    private static final long MAX_PIXEL_DATA_LENGTH = 0xFFFFFFFEL;

    /**
     * The element the pixels are in: PixelData, or one of the floating point elements a parametric
     * map carries them in instead. An object has exactly one of the three.
     */
    final int     pixelDataTag;
    /** Whether a sample is an IEEE float rather than an integer, which is what the tag decides. */
    final boolean floatingPoint;

    final int     rows;
    final int     columns;
    final int     samplesPerPixel;
    final int     bitsAllocated;
    final int     planarConfiguration;
    final int     frames;
    final boolean bigEndian;
    final String  photometricInterpretation;

    /** Bytes per sample. */
    final int bytesPerSample;

    /**
     * Pixels that share one set of chroma samples, so the smallest run a fill can be written in.
     * <p>
     * One for every layout that stores whole pixels. Two for YBR_FULL_422 and YBR_PARTIAL_422, which
     * carry one chroma pair for every two luma samples, stored Y Y Cb Cr; a rectangle in those has
     * to be rounded out to whole pairs, since half a pair cannot be recoloured on its own.
     */
    final int pixelsPerGroup;
    /** Bytes in one such group. */
    final int groupLength;
    /**
     * Bytes in one line: a row of pixels, or with colour-by-plane a row of one plane.
     * <p>
     * A line is the unit a redaction addresses, and the buffer it streams through holds a whole
     * number of them, so this and not the frame is what has to fit in memory. Rows and Columns are
     * US, so a line of a legal object is at most a few hundred kilobytes however large the image.
     */
    final long lineLength;
    /** Lines in one frame: its rows, or with colour-by-plane its rows once per plane. */
    final long linesPerFrame;
    /** Bytes in one uncompressed frame. */
    final long frameLength;

    /**
     * Whether chroma samples are shared between lines, so no line can be redacted on its own.
     * <p>
     * True only for YBR_PARTIAL_420, which sub-samples vertically as well as horizontally. The
     * standard permits it only with the MPEG transfer syntaxes, so it arrives here either
     * encapsulated -- in which case it is decoded first -- or malformed.
     */
    final boolean chromaSharedBetweenLines;

    /** dcm4che's conversion into this layout's colour space, or null when the fill needs none. */
    private final YBR ybr;

    private PixelGeometry(Attributes ds) {
        pixelDataTag  = pixelDataTag(ds);
        floatingPoint = pixelDataTag == Tag.FloatPixelData || pixelDataTag == Tag.DoubleFloatPixelData;

        rows                = ds.getInt(Tag.Rows, 0);
        columns             = ds.getInt(Tag.Columns, 0);
        samplesPerPixel     = ds.getInt(Tag.SamplesPerPixel, 1);
        bitsAllocated       = ds.getInt(Tag.BitsAllocated, 0);
        planarConfiguration = ds.getInt(Tag.PlanarConfiguration, 0);
        frames              = Math.max(1, ds.getInt(Tag.NumberOfFrames, 1));
        bigEndian           = ds.bigEndian();
        // A floating point element states its own sample width, and BitsAllocated is not always
        // there to be read: OF is four bytes, OD is eight.
        bytesPerSample      = pixelDataTag == Tag.FloatPixelData ? 4
                              : pixelDataTag == Tag.DoubleFloatPixelData ? 8
                              : bitsAllocated / 8;

        photometricInterpretation = ds.getString(Tag.PhotometricInterpretation);
        final PhotometricInterpretation pmi = standard(photometricInterpretation);
        chromaSharedBetweenLines = pmi == PhotometricInterpretation.YBR_PARTIAL_420;
        ybr                      = colourSpace(pmi);

        // A frame is a whole number of lines, and a line a whole number of groups, whichever layout
        // this is: colour-by-pixel puts the samples of a pixel side by side, colour-by-plane gives
        // each sample its own plane of rows, and the 422 layouts pair pixels up around their chroma.
        if (pmi == PhotometricInterpretation.YBR_FULL_422
            || pmi == PhotometricInterpretation.YBR_PARTIAL_422) {
            pixelsPerGroup = 2;
            groupLength    = 4;
            linesPerFrame  = rows;
        } else if (planarConfiguration == 0) {
            pixelsPerGroup = 1;
            groupLength    = samplesPerPixel * bytesPerSample;
            linesPerFrame  = rows;
        } else {
            pixelsPerGroup = 1;
            groupLength    = bytesPerSample;
            linesPerFrame  = (long) rows * samplesPerPixel;
        }
        lineLength  = (long) ceilDiv(columns, pixelsPerGroup) * groupLength;
        frameLength = lineLength * linesPerFrame;
    }

    /**
     * Reads the geometry, rejecting anything this handler cannot address byte-wise.
     *
     * @throws MizerException if the object is not an image, or its pixels are not byte-aligned.
     */
    static PixelGeometry of(Attributes ds) throws MizerException {
        final PixelGeometry geometry = new PixelGeometry(ds);
        if (geometry.rows <= 0 || geometry.columns <= 0) {
            throw new MizerException("DICOM object has no image dimensions (Rows/Columns), cannot alter pixels.");
        }
        if (geometry.samplesPerPixel <= 0) {
            // A line has to be at least one byte long for the redaction to make progress.
            throw new MizerException("DICOM object declares " + geometry.samplesPerPixel
                                     + " samples per pixel, cannot alter pixels.");
        }
        if (!geometry.floatingPoint && (geometry.bitsAllocated <= 0 || geometry.bitsAllocated % 8 != 0)) {
            // BitsAllocated of 1 packs 8 pixels per byte, so a rectangle is not a byte range. Only
            // asked of integer samples: a floating point element is byte-aligned by its VR, and
            // BitsAllocated is not required alongside it.
            throw new MizerException("BitsAllocated of " + geometry.bitsAllocated
                                     + " is not byte-aligned, cannot alter pixels.");
        }
        if (geometry.pixelsPerGroup > 1 && geometry.bytesPerSample != 1) {
            // The 422 layouts are defined for 8-bit samples, which is what a group of four bytes
            // assumes; dcm4che's own ColorSubsampling ignores BitsAllocated for the same reason.
            throw new MizerException("PhotometricInterpretation of " + geometry.photometricInterpretation
                                     + " with BitsAllocated of " + geometry.bitsAllocated
                                     + " is not a layout this understands, cannot alter pixels.");
        }
        if (geometry.lineLength > Integer.MAX_VALUE) {
            // Redaction buffers whole lines, and a byte[] cannot hold this one. No legal object gets
            // near this, so what it catches is a nonsensical SamplesPerPixel rather than a large
            // image.
            throw new MizerException("A single image line of " + geometry.lineLength
                                     + " bytes is larger than can be buffered, cannot alter pixels.");
        }
        return geometry;
    }

    /**
     * The element <b>ds</b> carries its pixels in, or zero if it carries none.
     * <p>
     * Parametric maps and some enhanced objects store pixels as floating point values in
     * {@code (7FE0,0008)} or {@code (7FE0,0009)} rather than in {@code (7FE0,0010)}, and an object
     * has exactly one of the three. Asking only about PixelData would pass those through with their
     * burned-in regions intact, and dcm4che treats all three as bulk data, so they cost the same to
     * redact.
     */
    static int pixelDataTag(final Attributes ds) {
        if (ds.contains(Tag.PixelData)) {
            return Tag.PixelData;
        }
        if (ds.contains(Tag.FloatPixelData)) {
            return Tag.FloatPixelData;
        }
        if (ds.contains(Tag.DoubleFloatPixelData)) {
            return Tag.DoubleFloatPixelData;
        }
        return 0;
    }

    /**
     * Rejects pixel data whose chroma is shared between lines, which redacting a line at a time
     * cannot address.
     *
     * @throws MizerException if a line cannot be redacted on its own.
     * @see #chromaSharedBetweenLines
     */
    void requireChromaWithinLines() throws MizerException {
        if (chromaSharedBetweenLines) {
            throw new MizerException("PhotometricInterpretation of " + photometricInterpretation
                                     + " shares chroma samples between rows, so a rectangle is not a "
                                     + "range of bytes, cannot alter pixels.");
        }
    }

    /**
     * Whether decoding this object's pixel data would produce a value too long for a pixel data
     * element to express -- which is what redacting an encapsulated object has to do.
     * <p>
     * dcm4che's transcoder computes the decoded length in int arithmetic
     * ({@code ImageDescriptor.getLength()} is {@code getFrameLength() * frames}, returning an int)
     * and writes whatever that produces, so past this the length field wraps to a small positive
     * number and the decoded object is written with most of its frames missing.
     */
    boolean decodesTooLongToExpress() {
        // Divided rather than multiplied, so the comparison cannot overflow the way the length it
        // is guarding against does.
        return frames > MAX_PIXEL_DATA_LENGTH / frameLength;
    }

    /** The requested rectangle reduced to the pixels that actually exist in the image. */
    Rectangle clip(Rectangle2D requested) {
        final int x0 = (int) Math.max(0, Math.floor(Math.min(requested.getMinX(), requested.getMaxX())));
        final int x1 = (int) Math.min(columns, Math.ceil(Math.max(requested.getMinX(), requested.getMaxX())));
        final int y0 = (int) Math.max(0, Math.floor(Math.min(requested.getMinY(), requested.getMaxY())));
        final int y1 = (int) Math.min(rows, Math.ceil(Math.max(requested.getMinY(), requested.getMaxY())));
        return new Rectangle(x0, y0, Math.max(0, x1 - x0), Math.max(0, y1 - y0));
    }

    /**
     * The stored value to write into each sample of a redacted pixel.
     * <p>
     * The script supplies either {@code v=<gray>} or {@code r=,g=,b=}, which
     * {@code SimpleRectanglePixelEditHandler} turns into a {@link Color}; {@code v=N} arrives as
     * {@code Color(N,N,N)}, so the red component carries the grey value either way. Values are
     * written as stored values, not rescaled to the bit depth: a script asking for 100 gets 100.
     * <p>
     * That value is capped at 255, because the fill arrives as a {@link Color}. A 16-bit image can
     * therefore only be filled from the bottom of its range, and {@code v=1000} fails in
     * {@code SimpleRectanglePixelEditHandler} before reaching here. On PALETTE COLOR the value is a
     * palette index rather than a shade, so what it looks like depends on the lookup table.
     * <p>
     * On a YBR layout the colour is converted rather than written as it stands: Y, Cb and Cr are not
     * R, G and B, so writing a black fill unconverted stores Y=0 Cb=0 Cr=0, which renders as a
     * medium green. The conversion is dcm4che's own, so the fill lands as the colour that was asked
     * for. YBR_ICT and YBR_RCT are left alone: they exist inside the JPEG 2000 codec, and an object
     * that has been decoded to be redacted comes back as RGB.
     * <p>
     * A null colour means the script's fill specification was unparseable; that fills with black,
     * which is zero in every layout that needs no conversion.
     */
    int[] fillSamples(Color color) {
        final int[] samples = new int[samplesPerPixel];
        final Color fill    = color == null ? Color.BLACK : color;
        if (samplesPerPixel != 3) {
            java.util.Arrays.fill(samples, fill.getRed());
            return samples;
        }
        if (ybr == null) {
            samples[0] = fill.getRed();
            samples[1] = fill.getGreen();
            samples[2] = fill.getBlue();
            return samples;
        }
        final float[] converted = ybr.fromRGB(new float[]{fill.getRed() / 255f,
                                                          fill.getGreen() / 255f,
                                                          fill.getBlue() / 255f});
        for (int sample = 0; sample < samples.length; sample++) {
            samples[sample] = Math.min(255, Math.max(0, Math.round(converted[sample] * 255)));
        }
        return samples;
    }

    /**
     * <b>pmi</b> as dcm4che models it, or null when it is absent or not one of the standard values.
     * <p>
     * {@code fromString} throws on anything non-standard, and the attribute is type 1 but still
     * arrives absent or misspelled. An unrecognised value is treated as storing whole pixels in no
     * particular colour space, which is what every layout except the YBR ones does.
     */
    private static PhotometricInterpretation standard(final String pmi) {
        if (pmi == null) {
            return null;
        }
        try {
            return PhotometricInterpretation.fromString(pmi);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static YBR colourSpace(final PhotometricInterpretation pmi) {
        if (pmi == PhotometricInterpretation.YBR_FULL || pmi == PhotometricInterpretation.YBR_FULL_422) {
            return YBR.FULL;
        }
        if (pmi == PhotometricInterpretation.YBR_PARTIAL_422
            || pmi == PhotometricInterpretation.YBR_PARTIAL_420) {
            return YBR.PARTIAL;
        }
        return null;
    }

    static int ceilDiv(final int value, final int divisor) {
        return (value + divisor - 1) / divisor;
    }
}
