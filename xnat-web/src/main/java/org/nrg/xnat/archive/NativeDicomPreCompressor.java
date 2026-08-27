/*
 * web: org.nrg.xnat.archive.NativeDicomPreCompressor
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.dicom.dicomedit.pixels.ImageWriters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Pre-compresses Native (uncompressed) DICOM files with large PixelData to encapsulated
 * (JPEG 2000 Lossless) format before anonymization.
 * <p>
 * dcm4che's {@code readValue()} loads entire Value into a Java {@code byte[]}, which has a ~2GB limit.
 * Encapsulated (fragmented) pixel data is read fragment-by-fragment, avoiding this limit.
 * The dcm4che {@link Transcoder} compresses frame-by-frame via {@code readFrame()}/{@code compressFrame()},
 * never triggering {@code readValue()} on the bulk pixel data.
 */
@Slf4j
public final class NativeDicomPreCompressor {

    // File-size upper bound: if the whole file is under this limit, PixelData cannot
    // hit the ~2 GB byte[] ceiling in dcm4che readValue().  2 000 000 000 leaves ~140 MB
    // of headroom below Integer.MAX_VALUE for JVM array-header overhead.
    private static final long FILE_SIZE_THRESHOLD = 2_000_000_000L;

    private static volatile String resolvedDestinationTsuid;

    private NativeDicomPreCompressor() {}

    /**
     * Pre-compresses a Native DICOM file when the Transfer Syntax is not yet known.
     * Equivalent to {@code preCompressIfNeeded(outputFile, null)}.
     *
     * @param outputFile the DICOM file on disk (replaced in-place if compressed)
     * @return true if the file was compressed, false otherwise
     */
    public static boolean preCompressIfNeeded(final File outputFile) {
        return preCompressIfNeeded(outputFile, null);
    }

    /**
     * Pre-compresses a Native DICOM file when the Transfer Syntax is already known.
     * <p>
     * Uses {@link File#length()} as a cheap upper bound on PixelData size.
     * If {@code transferSyntax} is {@code null}, reads only the File Meta Information
     * (a few hundred bytes) to obtain it — but only after the file-length check passes.
     *
     * @param outputFile     the DICOM file on disk (replaced in-place if compressed)
     * @param transferSyntax the Transfer Syntax UID of the file, or {@code null} to read from file
     * @return true if the file was compressed, false otherwise
     */
    public static boolean preCompressIfNeeded(final File outputFile, final String transferSyntax) {
        if (!isPreCompressionNeeded(outputFile, transferSyntax)) {
            return false;
        }

        log.info("Native DICOM file {} is {} bytes (>= {} byte threshold), will pre-compress",
                outputFile.getName(), outputFile.length(), FILE_SIZE_THRESHOLD);

        try {
            compressFile(outputFile);
            log.info("Successfully pre-compressed {} to encapsulated format", outputFile.getName());
            return true;
        } catch (Exception | LinkageError e) {
            // LinkageError too: the JPEG 2000 writer is backed by a native library, and where that
            // is not installed it fails as an Error. Without this the Error passes straight through
            // and fails the import, rather than skipping pre-compression as the message promises.
            log.warn("Pre-compression failed for {}, anonymization will proceed with original file",
                    outputFile.getName(), e);
            return false;
        }
    }

    /**
     * Returns {@code true} if the file is a large Native DICOM that should be
     * pre-compressed to avoid the ~2 GB {@code byte[]} limit in dcm4che.
     * <p>
     * Checks file length first (near-zero cost). Only when the file exceeds the
     * threshold and {@code transferSyntax} is {@code null} does it read the File
     * Meta Information to determine the Transfer Syntax.
     */
    static boolean isPreCompressionNeeded(final File file, final String transferSyntax) {
        if (file.length() < FILE_SIZE_THRESHOLD) {
            return false;
        }

        String ts = transferSyntax;
        if (ts == null) {
            try (final DicomInputStream dis = new DicomInputStream(file)) {
                final Attributes fmi = dis.readFileMetaInformation();
                ts = fmi != null ? fmi.getString(Tag.TransferSyntaxUID) : null;
            } catch (IOException e) {
                log.warn("Failed to read DICOM file meta information: {}", file.getName(), e);
                return false;
            }
        }

        return isNativeTransferSyntax(ts);
    }

    static boolean isNativeTransferSyntax(final String tsuid) {
        return UID.ImplicitVRLittleEndian.equals(tsuid)
                || UID.ExplicitVRLittleEndian.equals(tsuid)
                || UID.ExplicitVRBigEndian.equals(tsuid);
    }

    private static void compressFile(final File dicomFile) throws IOException {
        final String destTsuid = resolveDestinationTransferSyntax();
        final File tempFile = new File(dicomFile.getParentFile(), dicomFile.getName() + ".j2k.tmp");
        try {
            try (final Transcoder transcoder = new Transcoder(dicomFile)) {
                transcoder.setIncludeFileMetaInformation(true);
                transcoder.setDestinationTransferSyntax(destTsuid);
                transcoder.transcode((t, dataset) -> new FileOutputStream(tempFile));
            }
            Files.move(tempFile.toPath(), dicomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    private static String resolveDestinationTransferSyntax() {
        String tsuid = resolvedDestinationTsuid;
        if (tsuid != null) {
            return tsuid;
        }
        synchronized (NativeDicomPreCompressor.class) {
            tsuid = resolvedDestinationTsuid;
            if (tsuid != null) {
                return tsuid;
            }
            tsuid = findAvailableCompressedSyntax();
            resolvedDestinationTsuid = tsuid;
            return tsuid;
        }
    }

    private static String findAvailableCompressedSyntax() {
        // Try JPEG 2000 Lossless with default (OpenCV) writer
        if (ImageWriters.isAvailable(UID.JPEG2000Lossless)) {
            log.info("DICOM pre-compression: using JPEG 2000 Lossless (default writer)");
            return UID.JPEG2000Lossless;
        }

        // Fallback: JPEG Lossless SV1 (unlikely without OpenCV, but try)
        if (ImageWriters.isAvailable(UID.JPEGLosslessSV1)) {
            log.info("DICOM pre-compression: using JPEG Lossless SV1 as fallback");
            return UID.JPEGLosslessSV1;
        }

        throw new IllegalStateException(
                "No image writer available for DICOM pre-compression (tried JPEG 2000 Lossless and JPEG Lossless SV1)");
    }

}
