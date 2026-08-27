package org.nrg.dicom.dicomedit.pixels.impl;

import java.awt.Rectangle;
import java.util.Arrays;

/**
 * Writes a solid rectangle into raw, uncompressed pixel data, one line at a time.
 * <p>
 * A line -- a row of pixels, or with colour-by-plane a row of one plane -- is the unit the fill is
 * addressed in. The caller streams a whole number of lines through a fixed-size buffer, so nothing
 * here depends on how large a frame is. Operating on stored values rather than on a decoded image is
 * what lets it run without a codec at all for a native transfer syntax.
 * <p>
 * Within a line the fill is written in groups of {@link PixelGeometry#pixelsPerGroup} pixels, which
 * is one pixel in every layout that stores whole pixels and two in the 422 layouts, where a chroma
 * pair is shared. A rectangle whose edge falls inside a shared group is rounded outwards: half a
 * pair cannot be given a new colour on its own, and over-redacting a column is the safe direction to
 * err in.
 */
final class LineRedactor {

    private final PixelGeometry geometry;
    private final Rectangle     rect;
    /**
     * Fill bytes for one group -- a whole pixel, one sample of it, or a pixel pair sharing chroma --
     * indexed by plane, with a single entry for every layout but colour-by-plane.
     */
    private final byte[][]      lineFill;
    /** Byte offset the rectangle starts at within a line, rounded down to a whole group. */
    private final int           runOffset;
    /** Groups the rectangle covers, rounded out at both edges. */
    private final int           runGroups;

    LineRedactor(PixelGeometry geometry, Rectangle rect, int[] fillSamples) {
        this.geometry = geometry;
        this.rect     = rect;

        if (geometry.pixelsPerGroup > 1) {
            // 422: two luma samples share one chroma pair, stored Y Y Cb Cr. Eight bits per sample
            // by definition, so byte order does not come into it.
            lineFill = new byte[][]{{(byte) fillSamples[0], (byte) fillSamples[0],
                                     (byte) fillSamples[1], (byte) fillSamples[2]}};
        } else {
            final byte[] pixelFill = new byte[geometry.samplesPerPixel * geometry.bytesPerSample];
            for (int sample = 0; sample < geometry.samplesPerPixel; sample++) {
                putSample(pixelFill, sample * geometry.bytesPerSample, fillSamples[sample]);
            }
            if (geometry.planarConfiguration == 0) {
                // Colour-by-pixel: samples of a pixel are adjacent, so a run of pixels is a byte run
                // of whole pixels.
                lineFill = new byte[][]{pixelFill};
            } else {
                // Colour-by-plane: a line belongs to one plane, so it takes that plane's sample alone.
                lineFill = new byte[geometry.samplesPerPixel][];
                for (int plane = 0; plane < geometry.samplesPerPixel; plane++) {
                    lineFill[plane] = Arrays.copyOfRange(pixelFill, plane * geometry.bytesPerSample,
                                                         (plane + 1) * geometry.bytesPerSample);
                }
            }
        }
        final int firstGroup = rect.x / geometry.pixelsPerGroup;
        runOffset = firstGroup * geometry.groupLength;
        runGroups = PixelGeometry.ceilDiv(rect.x + rect.width, geometry.pixelsPerGroup) - firstGroup;
    }

    /** True when the rectangle covers no pixels, so lines can be passed through untouched. */
    boolean isNoOp() {
        return rect.width <= 0 || rect.height <= 0;
    }

    /**
     * Fills whatever part of the rectangle falls in one line, in place.
     *
     * @param data      a buffer holding one or more whole lines.
     * @param offset    where the line starts in <b>data</b>.
     * @param length    number of valid bytes of the line, which is short only for a truncated value.
     * @param lineIndex which line of the frame this is, counted from zero.
     */
    void redact(byte[] data, int offset, int length, long lineIndex) {
        if (isNoOp()) {
            return;
        }
        // Interleaved, every line is a row; by plane, the rows of each plane in turn.
        final int y = (int) (geometry.planarConfiguration == 0 ? lineIndex : lineIndex % geometry.rows);
        if (y < rect.y || y >= rect.y + rect.height) {
            return;
        }
        final int plane = geometry.planarConfiguration == 0 ? 0 : (int) (lineIndex / geometry.rows);
        fillRun(data, offset + length, offset + runOffset, runGroups, lineFill[plane]);
    }

    /** Repeats <b>unit</b> across <b>count</b> positions from <b>offset</b>, stopping at <b>limit</b>. */
    private static void fillRun(byte[] data, int limit, int offset, int count, byte[] unit) {
        final int end = Math.min(limit, offset + count * unit.length);
        if (offset >= end) {
            return;
        }
        if (unit.length == 1) {
            Arrays.fill(data, offset, end, unit[0]);
            return;
        }
        for (int at = offset; at < end; at += unit.length) {
            System.arraycopy(unit, 0, data, at, Math.min(unit.length, end - at));
        }
    }

    private void putSample(byte[] buffer, int offset, int value) {
        for (int byteIndex = 0; byteIndex < geometry.bytesPerSample; byteIndex++) {
            // Little endian puts the least significant byte first; big endian, last.
            final int shift = 8 * (geometry.bigEndian ? geometry.bytesPerSample - 1 - byteIndex : byteIndex);
            buffer[offset + byteIndex] = (byte) (value >>> shift);
        }
    }
}
