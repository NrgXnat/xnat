package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The stored pixel values of a DICOM file, decompressed if need be, for comparing what a redaction
 * did against what it should have done.
 * <p>
 * Deliberately reads stored values rather than a rendered image: a test that goes through an image
 * decoder cannot tell "the handler wrote the wrong value" from "the decoder rendered it
 * differently".
 */
final class RawPixels {

    final Attributes dataset;
    final String     transferSyntax;
    final int        rows;
    final int        columns;
    final int        samplesPerPixel;
    final int        bytesPerSample;
    final int        planarConfiguration;
    final int        frames;
    final boolean    bigEndian;
    private final byte[] pixels;

    private RawPixels(Attributes dataset, String transferSyntax, byte[] pixels) {
        this.dataset             = dataset;
        this.transferSyntax      = transferSyntax;
        this.pixels              = pixels;
        this.rows                = dataset.getInt(Tag.Rows, 0);
        this.columns             = dataset.getInt(Tag.Columns, 0);
        this.samplesPerPixel     = dataset.getInt(Tag.SamplesPerPixel, 1);
        this.bytesPerSample      = dataset.getInt(Tag.BitsAllocated, 8) / 8;
        this.planarConfiguration = dataset.getInt(Tag.PlanarConfiguration, 0);
        this.frames              = Math.max(1, dataset.getInt(Tag.NumberOfFrames, 1));
        this.bigEndian           = UID.ExplicitVRBigEndian.equals(transferSyntax);
    }

    static RawPixels of(File file) throws IOException {
        final Attributes dataset;
        final Attributes fmi;
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            fmi     = in.readFileMetaInformation();
            dataset = in.readDataset();
        }
        final String transferSyntax = fmi != null
                ? fmi.getString(Tag.TransferSyntaxUID)
                : dataset.getString(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);

        final Object value = dataset.getValue(Tag.PixelData);
        final byte[] pixels;
        if (value instanceof BulkData) {
            pixels = readFully(((BulkData) value).openStream(), (int) ((BulkData) value).longLength());
        } else if (value instanceof byte[]) {
            pixels = (byte[]) value;
        } else {
            pixels = decompress(dataset, transferSyntax);
        }
        return new RawPixels(dataset, transferSyntax, pixels);
    }

    private static byte[] decompress(Attributes dataset, String transferSyntax) throws IOException {
        final Decompressor decompressor = new Decompressor(dataset, transferSyntax);
        try {
            decompressor.decompress();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ImageInputStream source = decompressor.createImageInputStream()) {
                final int frames = Math.max(1, dataset.getInt(Tag.NumberOfFrames, 1));
                for (int index = 0; index < frames; index++) {
                    decompressor.writeFrameTo(source, index, out);
                }
            }
            return out.toByteArray();
        } finally {
            decompressor.dispose();
        }
    }

    private static byte[] readFully(InputStream in, int length) throws IOException {
        try (InputStream source = in) {
            final byte[] buffer = new byte[length];
            int          filled = 0;
            while (filled < buffer.length) {
                final int read = source.read(buffer, filled, buffer.length - filled);
                if (read < 0) {
                    throw new IOException("pixel data ended early at " + filled + " of " + length);
                }
                filled += read;
            }
            return buffer;
        }
    }

    /** The stored value of one sample, as an unsigned int. */
    int sample(int frame, int x, int y, int sampleIndex) {
        final long frameLength = (long) rows * columns * samplesPerPixel * bytesPerSample;
        final long offset;
        if (planarConfiguration == 0) {
            offset = frame * frameLength
                     + ((long) y * columns + x) * samplesPerPixel * bytesPerSample
                     + (long) sampleIndex * bytesPerSample;
        } else {
            offset = frame * frameLength
                     + (long) sampleIndex * rows * columns * bytesPerSample
                     + ((long) y * columns + x) * bytesPerSample;
        }
        int value = 0;
        for (int index = 0; index < bytesPerSample; index++) {
            final int shift = 8 * (bigEndian ? bytesPerSample - 1 - index : index);
            value |= (pixels[(int) offset + index] & 0xff) << shift;
        }
        return value;
    }

    /** ByteArrayInputStream over the raw values, for whole-buffer comparisons. */
    ByteArrayInputStream stream() {
        return new ByteArrayInputStream(pixels);
    }

    int length() {
        return pixels.length;
    }
}
