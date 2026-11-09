package org.nrg.xnat.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compressed tar uploads read at the same time must not corrupt each other. commons-compress 1.26.0 (COMPRESS-666)
 * skips the unread part of an entry through commons-io's scratch buffer, which every thread shares, so a skip's
 * inflater output can be overwritten while the inflater still needs it. Some imports then failed with "Corrupted TAR
 * archive"; others archived files with wrong header values, with no error.
 *
 * <p>This reads archives through the same streams as {@link DicomZipImporter}'s importTgzFile. Text sidecars are read
 * only as far as a DICOM parser gets before it gives up, so {@code getNextTarEntry()} has to skip the rest; every other
 * entry is read to its end, and its digest must match a read of the same archive on one thread.</p>
 */
public class CompressedTarConcurrencyTest {
    private static final int THREADS  = 4;
    private static final int ROUNDS   = 6;
    private static final int ARCHIVES = 6;
    private static final int PAIRS    = 30;

    private static final String[] WORDS = {"Station Name:", "Protocol:", "Sequence:", "Coil:", "alpha", "beta", "gamma", "T1"};

    @Test
    public void concurrentReadsOfCompressedTarsAreIntact() throws Exception {
        final List<byte[]>       archives = new ArrayList<>();
        final List<List<String>> expected = new ArrayList<>();
        for (int archive = 0; archive < ARCHIVES; archive++) {
            archives.add(archive(archive));
            expected.add(read(archives.get(archive)));
        }

        final AtomicInteger   intact     = new AtomicInteger();
        final AtomicInteger   unreadable = new AtomicInteger();
        final AtomicInteger   altered    = new AtomicInteger();
        final ExecutorService pool       = Executors.newFixedThreadPool(THREADS);
        try {
            final List<Future<?>> workers = new ArrayList<>();
            for (int thread = 0; thread < THREADS; thread++) {
                final int offset = thread;
                workers.add(pool.submit(() -> {
                    for (int round = 0; round < ROUNDS; round++) {
                        for (int archive = 0; archive < ARCHIVES; archive++) {
                            final int index = (archive + offset) % ARCHIVES;
                            try {
                                (read(archives.get(index)).equals(expected.get(index)) ? intact : altered).incrementAndGet();
                            } catch (IOException | IllegalArgumentException e) {
                                unreadable.incrementAndGet();
                            }
                        }
                    }
                    return null;
                }));
            }
            for (final Future<?> worker : workers) {
                worker.get();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(unreadable).as("reads that failed").hasValue(0);
        assertThat(altered).as("reads with different bytes and no error").hasValue(0);
        assertThat(intact).hasValue(THREADS * ROUNDS * ARCHIVES);
    }

    /** The digests of an archive's non-sidecar entries, read as DicomZipImporter.importTgzFile reads them. */
    private static List<String> read(final byte[] archive) throws IOException, NoSuchAlgorithmException {
        final List<String>  digests = new ArrayList<>();
        final MessageDigest digest  = MessageDigest.getInstance("MD5");
        final byte[]        buffer  = new byte[8192];
        final InputStream   is      = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(archive)));
        try (final TarArchiveInputStream tar = new TarArchiveInputStream(is)) {
            TarArchiveEntry entry;
            while (null != (entry = tar.getNextTarEntry())) {
                if (entry.getName().endsWith(".txt")) {
                    tar.read(buffer, 0, 132);
                    continue;
                }
                digest.reset();
                int read;
                while ((read = tar.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
                digests.add(HexFormat.of().formatHex(digest.digest()));
            }
        }
        return digests;
    }

    /** A tgz of pairs of a text sidecar and smooth, image-like 16-bit data, which both compress well. */
    private static byte[] archive(final int seed) throws IOException {
        final Random                random = new Random(seed);
        final ByteArrayOutputStream bytes  = new ByteArrayOutputStream();
        try (final TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(bytes))) {
            for (int pair = 0; pair < PAIRS; pair++) {
                add(tar, "README-" + pair + ".txt", sidecar(random));
                add(tar, "IMG-" + pair + ".dcm", image(random, pair));
            }
        }
        return bytes.toByteArray();
    }

    private static void add(final TarArchiveOutputStream tar, final String name, final byte[] content) throws IOException {
        final TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(content.length);
        tar.putArchiveEntry(entry);
        tar.write(content);
        tar.closeArchiveEntry();
    }

    private static byte[] sidecar(final Random random) {
        final int           size = 4000 + random.nextInt(12000);
        final StringBuilder text = new StringBuilder();
        while (text.length() < size) {
            text.append(WORDS[random.nextInt(WORDS.length)]).append(random.nextInt(8) == 0 ? '\n' : ' ');
        }
        return text.toString().getBytes(UTF_8);
    }

    private static byte[] image(final Random random, final int phase) {
        final int    side   = 64 + random.nextInt(64);
        final byte[] pixels = new byte[side * side * 2];
        for (int y = 0, at = 0; y < side; y++) {
            for (int x = 0; x < side; x++, at += 2) {
                final int value = (int) (2000 + 1500 * Math.sin(x / 17.0 + phase) * Math.cos(y / 23.0)) + random.nextInt(8);
                pixels[at]     = (byte) value;
                pixels[at + 1] = (byte) (value >> 8);
            }
        }
        return pixels;
    }
}
