package org.nrg.dicom.dicomedit.pixels.impl;

import java.awt.Rectangle;
import java.util.Arrays;

/**
 * Writes a solid rectangle into one buffer of raw, uncompressed frame bytes.
 * <p>
 * Operating on stored values rather than on a decoded image is what lets a redaction run in
 * constant memory: only the frame currently being edited is resident, and for a native transfer
 * syntax no codec is involved at all.
 */
final class FrameRedactor {

    private final PixelGeometry geometry;
    private final Rectangle     rect;
    /** Fill bytes for one pixel, i.e. every sample of it, in the dataset's byte order. */
    private final byte[]        pixelFill;

    FrameRedactor(PixelGeometry geometry, Rectangle rect, int[] fillSamples) {
        this.geometry  = geometry;
        this.rect      = rect;
        this.pixelFill = new byte[geometry.samplesPerPixel * geometry.bytesPerSample];
        for (int sample = 0; sample < geometry.samplesPerPixel; sample++) {
            putSample(pixelFill, sample * geometry.bytesPerSample, fillSamples[sample]);
        }
    }

    /** True when the rectangle covers no pixels, so frames can be passed through untouched. */
    boolean isNoOp() {
        return rect.width <= 0 || rect.height <= 0;
    }

    /**
     * Fills the rectangle in one frame, in place.
     *
     * @param frame  raw bytes of a single uncompressed frame.
     * @param length number of valid bytes in <b>frame</b>.
     */
    void redact(byte[] frame, int length) {
        if (isNoOp()) {
            return;
        }
        if (geometry.planarConfiguration == 0) {
            // Colour-by-pixel: samples of a pixel are adjacent, so a run of pixels is a byte run.
            final int pixelStride = geometry.samplesPerPixel * geometry.bytesPerSample;
            final int rowStride   = geometry.columns * pixelStride;
            for (int y = rect.y; y < rect.y + rect.height; y++) {
                fillRun(frame, length, y * rowStride + rect.x * pixelStride, rect.width, pixelFill, pixelFill.length);
            }
        } else {
            // Colour-by-plane: each sample lives in its own plane, so fill one run per plane.
            final int planeStride = geometry.rows * geometry.columns * geometry.bytesPerSample;
            final int rowStride   = geometry.columns * geometry.bytesPerSample;
            for (int plane = 0; plane < geometry.samplesPerPixel; plane++) {
                final byte[] sampleFill = Arrays.copyOfRange(pixelFill,
                        plane * geometry.bytesPerSample, (plane + 1) * geometry.bytesPerSample);
                for (int y = rect.y; y < rect.y + rect.height; y++) {
                    fillRun(frame, length,
                            plane * planeStride + y * rowStride + rect.x * geometry.bytesPerSample,
                            rect.width, sampleFill, geometry.bytesPerSample);
                }
            }
        }
    }

    /** Repeats <b>unit</b> across <b>count</b> positions from <b>offset</b>, staying inside the buffer. */
    private static void fillRun(byte[] frame, int length, int offset, int count, byte[] unit, int unitLength) {
        final int end = Math.min(length, offset + count * unitLength);
        if (offset >= end) {
            return;
        }
        if (unitLength == 1) {
            Arrays.fill(frame, offset, end, unit[0]);
            return;
        }
        for (int at = offset; at < end; at += unitLength) {
            System.arraycopy(unit, 0, frame, at, Math.min(unitLength, end - at));
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
