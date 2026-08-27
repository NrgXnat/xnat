package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
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

    final int     rows;
    final int     columns;
    final int     samplesPerPixel;
    final int     bitsAllocated;
    final int     planarConfiguration;
    final int     frames;
    final boolean bigEndian;

    /** Bytes per sample. */
    final int bytesPerSample;
    /** Bytes in one uncompressed frame. */
    final long frameLength;

    private PixelGeometry(Attributes ds) {
        rows                = ds.getInt(Tag.Rows, 0);
        columns             = ds.getInt(Tag.Columns, 0);
        samplesPerPixel     = ds.getInt(Tag.SamplesPerPixel, 1);
        bitsAllocated       = ds.getInt(Tag.BitsAllocated, 0);
        planarConfiguration = ds.getInt(Tag.PlanarConfiguration, 0);
        frames              = Math.max(1, ds.getInt(Tag.NumberOfFrames, 1));
        bigEndian           = ds.bigEndian();
        bytesPerSample      = bitsAllocated / 8;
        frameLength         = (long) rows * columns * samplesPerPixel * bytesPerSample;
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
        if (geometry.bitsAllocated <= 0 || geometry.bitsAllocated % 8 != 0) {
            // BitsAllocated of 1 packs 8 pixels per byte, so a rectangle is not a byte range.
            throw new MizerException("BitsAllocated of " + geometry.bitsAllocated
                                     + " is not byte-aligned, cannot alter pixels.");
        }
        if (geometry.frameLength > Integer.MAX_VALUE) {
            // Redaction buffers one frame at a time, and a byte[] cannot hold this one. Refusing
            // beats the alternatives: 2-4 GiB throws out of the array allocation, and beyond that
            // the length narrows to a small positive number and every rectangle lands in the wrong
            // place while the output still comes out the right size.
            throw new MizerException("A single frame of " + geometry.frameLength
                                     + " bytes is larger than can be buffered, cannot alter pixels.");
        }
        return geometry;
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
     * A null colour means the script's fill specification was unparseable; that fills with zero,
     * which is what this operation has always done.
     */
    int[] fillSamples(Color color) {
        final int[] samples = new int[samplesPerPixel];
        if (color == null) {
            return samples;
        }
        if (samplesPerPixel == 3) {
            samples[0] = color.getRed();
            samples[1] = color.getGreen();
            samples[2] = color.getBlue();
        } else {
            java.util.Arrays.fill(samples, color.getRed());
        }
        return samples;
    }
}
