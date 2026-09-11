/*
 * core: org.nrg.xft.utils.zip.ZipUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.zip;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.fileExtraction.Format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.*;


/**
 * @author timo
 */
@SuppressWarnings("unused")
@Slf4j
public class ZipUtils implements ZipI {

    public static boolean isCompressedFile(final String filename, final String... extras) {
        return Format.getFormat(filename) != Format.UNKNOWN || StringUtils.endsWithAny(filename.toLowerCase(), extras);
    }

    public static String getCompression(final String filename, final String... extras) {
        if (!isCompressedFile(filename, extras)) {
            return "";
        }
        final Format format = Format.getFormat(filename);
        if (format != Format.UNKNOWN) {
            return format.toString().toLowerCase();
        }
        final Matcher matcher = getExtensionPattern(extras).matcher(filename);
        if (matcher.matches()) {
            return matcher.group("extension");
        }
        throw new RuntimeException("File " + filename + " doesn't match any of the specified extensions for compression, even though isCompressedFile() said it did.");
    }

    public static void extractFile(final File file, final Path destination) throws IOException {
        extractFile(file, destination, getCompression(file.getName()));
    }

    public static void extractFile(final File file, final Path destination, final String compression) throws IOException {
        final ZipI zipper = createZipper(compression);
        if (zipper instanceof TarUtils tarUtils) {
            // The archive is already a materialized, seekable file -- extract straight from it instead of routing
            // through the InputStream overload below, which would make a wholly redundant buffered copy.
            tarUtils.extractFromFile(file, destination.toString(), true, null, null);
        } else {
            ((ZipUtils) zipper).extractFromFile(file, destination.toString(), true, null, null);
        }
    }

    public static void extractFile(final InputStream input, final Path destination, final String compression) throws IOException {
        // Entry-name validation lives inside ZipUtils/TarUtils' own extract(InputStream, ...) implementations (see
        // ZipUtils#extractMap and TarUtils#extract), so every caller of the ZipI interface gets it the same way,
        // not just this convenience wrapper.
        createZipper(compression).extract(input, destination.toString());
    }

    private static ZipI createZipper(final String compression) {
        switch (compression) {
            case "tar":
                return new TarUtils();
            case "tgz":
                final TarUtils tarUtils = new TarUtils();
                tarUtils.setCompressionMethod(ZipOutputStream.DEFLATED);
                return tarUtils;
            default:
                return new ZipUtils();
        }
    }

    /**
     * Rejects an archive upload (zip, tar, or tgz) that was found to contain one or more entries whose relative path
     * does not resolve within the intended destination directory. The rejected archive is preserved in the XNAT
     * cache folder along with an {@code upload_report.txt} file describing why the upload was rejected, then an
     * {@link UnsafeArchiveException} is thrown so the caller knows the upload did not succeed -- and so callers that
     * only mean to catch a genuine extraction I/O failure (as opposed to a deliberate rejection) can tell the two
     * apart. Package-visible so both {@link ZipUtils} and {@link TarUtils} can share it.
     *
     * <p>The report is written and the admin notified <em>before</em> the (possibly large) archive is copied into
     * the cache: that copy is best-effort and its failure (e.g. the copy itself runs out of disk space) must never
     * cost the report or the notification, which are what actually let an administrator review the rejection. The
     * copy is also capped at {@link #MAX_RETAINED_ARCHIVE_BYTES} and old rejections are purged on every call, so a
     * user cannot fill the cache volume by looping uploads that are each rejected.</p>
     *
     * @param archiveFile     The archive file that was rejected.
     * @param archiveFileName The file name to give the archive when it's copied into the cache folder, e.g.
     *                        {@code upload.zip} or {@code upload.tar}.
     * @param destination     The destination the archive was going to be extracted into.
     * @param unsafeEntries   The offending entry names found in the archive.
     *
     * @throws UnsafeArchiveException Always, to indicate the upload was rejected. Every bookkeeping step above the
     *                                 throw (creating the report folder, writing the report, the admin email, the
     *                                 retained archive copy) is best-effort: none of it may turn a rejection into a
     *                                 plain {@link IOException} that a caller could mistake for an ordinary
     *                                 extraction failure and fall back around -- see {@link UnsafeArchiveException}.
     */
    static void rejectArchiveUpload(final File archiveFile, final String archiveFileName, final Path destination, final List<String> unsafeEntries) throws UnsafeArchiveException {
        final boolean       singular = unsafeEntries.size() == 1;
        Path cacheFolder = null;
        try {
            // Use createTempDirectory (atomic, collision-proof) rather than a plain timestamp-named folder: two
            // uploads rejected in the same millisecond would otherwise resolve to the same folder and silently
            // overwrite each other's archive and report. The timestamp is kept as a human-readable prefix only,
            // not for uniqueness.
            final Path rejectedUploadsRoot = getXnatCachePath().resolve("RejectedUploads");
            Files.createDirectories(rejectedUploadsRoot);
            purgeOldRejectedUploads(rejectedUploadsRoot);
            cacheFolder = Files.createTempDirectory(rejectedUploadsRoot, FileUtils.getMsTimestamp() + "-");

            final boolean retainArchive   = archiveFile.length() <= MAX_RETAINED_ARCHIVE_BYTES;
            final File    rejectedArchive = cacheFolder.resolve(archiveFileName).toFile();

            final StringBuilder report = new StringBuilder();
            report.append("XNAT Archive Upload Rejected").append(System.lineSeparator());
            report.append("Timestamp: ").append(new Date()).append(System.lineSeparator());
            report.append("Intended destination: ").append(destination).append(System.lineSeparator());
            if (retainArchive) {
                report.append("Rejected archive: ").append(rejectedArchive.getAbsolutePath()).append(System.lineSeparator());
            } else {
                report.append("Rejected archive: not retained (").append(archiveFile.length())
                      .append(" bytes exceeds the ").append(MAX_RETAINED_ARCHIVE_BYTES)
                      .append("-byte retention cap); only this report and the offending entry names were kept.")
                      .append(System.lineSeparator());
            }
            report.append(System.lineSeparator());
            report.append("Reason: The uploaded archive was rejected because it contains ").append(unsafeEntries.size())
                  .append(singular ? " entry" : " entries")
                  .append(" whose relative path resolves outside of the intended destination directory ")
                  .append("(a path traversal / \"zip-slip\" attempt). To protect the file system, the entire upload was ")
                  .append("rejected rather than extracting only the safe entries.").append(System.lineSeparator());
            report.append(System.lineSeparator());
            report.append("Offending ").append(singular ? "entry" : "entries").append(" (").append(unsafeEntries.size()).append("):").append(System.lineSeparator());
            for (final String entry : unsafeEntries) {
                report.append("  - ").append(entry).append(System.lineSeparator());
            }

            final File reportFile = cacheFolder.resolve("upload_report.txt").toFile();
            org.apache.commons.io.FileUtils.writeStringToFile(reportFile, report.toString(), StandardCharsets.UTF_8);

            // Notify the site administrator, with the report content as the message body. This is best-effort:
            // wrapped in its own try/catch because AdminUtils.sendAdminEmail can itself throw (e.g. it calls
            // XDAT.getNotificationsPreferences() without a null-check of its own, which throws an uncaught NPE if
            // Spring isn't fully up) -- a notification failure must never replace, or prevent, the rejection below.
            // The report is HTML-escaped before being embedded, since it includes the archive's own entry names
            // verbatim.
            try {
                AdminUtils.sendAdminEmail("XNAT Archive Upload Rejected",
                                           "<pre>" + StringEscapeUtils.escapeHtml4(report.toString()) + "</pre>");
            } catch (final Exception e) {
                log.warn("Unable to send the archive-upload-rejected notification email to the site administrator", e);
            }

            // The archive copy happens last, and its failure is non-fatal: the report and email above have already
            // captured everything an administrator needs, and the upload must still be rejected either way.
            if (retainArchive) {
                try {
                    org.apache.commons.io.FileUtils.copyFile(archiveFile, rejectedArchive);
                } catch (final Exception e) {
                    log.warn("Unable to retain a copy of the rejected archive in {} (the rejection itself is unaffected)", cacheFolder, e);
                }
            } else {
                log.warn("Rejected archive targeting {} is {} bytes, over the {}-byte retention cap; only the report and offending entry names were kept.",
                          destination, archiveFile.length(), MAX_RETAINED_ARCHIVE_BYTES);
            }
        } catch (final Exception e) {
            // None of the bookkeeping above (folder/report creation included) may prevent, or change the type of,
            // the rejection below: a caller catching IOException broadly (e.g. to fall back to copying the archive
            // in as-is when extraction merely fails) must never be able to mistake "the RejectedUploads folder
            // happened to be unwritable" for "this archive turned out to be safe after all".
            log.error("Unable to fully record the rejected-archive report for the upload targeting {} (rejecting it regardless)", destination, e);
        }

        // The absolute path goes to the server log and the report file only -- both are server-local surfaces an
        // administrator already has file system access to. The thrown message is what propagates back to callers
        // (some of which forward exception messages to external clients, e.g. as an HTTP error body), so it must
        // never include a local path.
        log.warn("Rejected archive upload targeting {}: {} unsafe {} detected.{}",
                 destination, unsafeEntries.size(), singular ? "entry" : "entries",
                 cacheFolder == null ? "" : " Report written to " + cacheFolder);

        throw new UnsafeArchiveException("Archive upload rejected: archive contains " + unsafeEntries.size() +
                               (singular ? " entry" : " entries") +
                               " with a relative path that resolves outside of the destination directory. " +
                               "This upload has been logged for review by a site administrator.");
    }

    /** Maximum size, in bytes, of a rejected archive that {@link #rejectArchiveUpload} will retain a copy of. */
    private static final long MAX_RETAINED_ARCHIVE_BYTES = 500L * 1024 * 1024;

    /** How long a rejected upload's folder (report, entry names, and -- if retained -- the archive) is kept. */
    private static final Duration REJECTED_UPLOAD_RETENTION = Duration.ofDays(30);

    /**
     * Minimum gap between purges: rejections can arrive back-to-back (e.g. a user probing the upload endpoint), and
     * every one of them runs on the thread handling that request/import -- listing and stat-ing the whole
     * {@code RejectedUploads} folder on every single rejection would make each one pay for that walk before the
     * caller ever sees the rejection.
     */
    private static final Duration MIN_PURGE_INTERVAL = Duration.ofHours(1);

    /** Epoch millis of the last purge attempt, process-wide; volatile since rejections can race across threads. */
    private static volatile long lastPurgeAttemptMillis = 0;

    /**
     * Best-effort, age-based purge of {@code <cache>/RejectedUploads}, attempted on a rejection (at most once per
     * {@link #MIN_PURGE_INTERVAL}, so a burst of rejections doesn't each pay for walking the folder) so nothing has
     * to schedule a separate cleanup job. A user with upload rights could otherwise fill the cache volume by
     * looping uploads that are each rejected, since (unlike a successful upload) nothing else ever revisits this
     * folder. Any failure here is logged and swallowed -- this is housekeeping, not part of the rejection itself.
     */
    private static void purgeOldRejectedUploads(final Path rejectedUploadsRoot) {
        final long now = System.currentTimeMillis();
        if (now - lastPurgeAttemptMillis < MIN_PURGE_INTERVAL.toMillis()) {
            return;
        }
        lastPurgeAttemptMillis = now;

        final long cutoffMillis = now - REJECTED_UPLOAD_RETENTION.toMillis();
        try (final Stream<Path> children = Files.list(rejectedUploadsRoot)) {
            children.filter(Files::isDirectory).forEach(child -> {
                try {
                    if (org.apache.commons.io.FileUtils.isFileOlder(child.toFile(), cutoffMillis)) {
                        org.apache.commons.io.FileUtils.deleteDirectory(child.toFile());
                    }
                } catch (final IOException e) {
                    log.debug("Unable to check or purge the old rejected upload {}", child, e);
                }
            });
        } catch (final IOException e) {
            log.debug("Unable to purge old entries from {}", rejectedUploadsRoot, e);
        }
    }

    /**
     * Buffers the given input stream to a temporary file under the XNAT cache path -- never {@code java.io.tmpdir},
     * which may be tmpfs (RAM-backed) in containerized deployments, so buffering a large archive there could
     * otherwise exhaust memory rather than disk. The stream is closed once fully read. Package-visible so both
     * {@link ZipUtils} and {@link TarUtils} can share it.
     *
     * @param is     The input stream to buffer.
     * @param prefix The prefix to use for the temporary file's name.
     *
     * @return The temporary file containing the stream's contents.
     *
     * @throws IOException When an error occurs reading or writing the data.
     */
    static File bufferToCache(final InputStream is, final String prefix) throws IOException {
        final Path bufferFolder = getXnatCachePath().resolve("UploadBuffer");
        Files.createDirectories(bufferFolder);
        final File temp = Files.createTempFile(bufferFolder, prefix, ".tmp").toFile();
        try {
            try (final FileOutputStream fos = new FileOutputStream(temp)) {
                IOUtils.copy(is, fos);
            }
        } catch (final IOException e) {
            // A partial write (e.g. disk full partway through a large upload) would otherwise leave an orphaned,
            // potentially large temp file behind -- the caller never gets a File back to clean up itself.
            FileUtils.DeleteFile(temp);
            throw e;
        } finally {
            is.close();
        }
        return temp;
    }

    /**
     * Resolves the XNAT cache path to buffer/store archives under. Falls back to the JVM's default temp directory
     * if the site configuration isn't available -- e.g. the Spring context isn't up yet, or this is running outside
     * the web application entirely (unit tests) -- so extraction doesn't hard-fail just because the preferred,
     * disk-backed location can't be determined.
     */
    private static Path getXnatCachePath() {
        try {
            final String cachePath = XDAT.getSiteConfigPreferences().getCachePath();
            if (StringUtils.isNotBlank(cachePath)) {
                return Path.of(cachePath);
            }
        } catch (final Exception e) {
            log.debug("Unable to determine the XNAT cache path (the site configuration may not be available yet); falling back to the default temp directory.", e);
        }
        // Surfaced at WARN, not DEBUG: buffering/extracting into java.io.tmpdir is exactly the tmpfs (RAM-backed)
        // risk the Javadoc above calls out, and an operator should be able to notice it's happening from the log
        // without having to raise the log level first.
        log.warn("Falling back to the default temp directory ({}) for archive buffering/extraction; the XNAT site configuration's cache path is not available.", System.getProperty("java.io.tmpdir", "/tmp"));
        return Path.of(System.getProperty("java.io.tmpdir", "/tmp"));
    }

    byte[]          buf         = new byte[FileUtils.LARGE_DOWNLOAD];
    ZipOutputStream out         = null;
    int             compression = ZipOutputStream.DEFLATED;
    boolean         decompress  = false;
    private final List<String> _duplicates = new ArrayList<>();

    @Override
    public void setOutputStream(OutputStream outStream) {
        out = new ZipOutputStream(outStream);
        out.setMethod(ZipOutputStream.DEFLATED);
    }

    @Override
    public void setOutputStream(OutputStream outStream, int compressionMethod) {
        out = new ZipOutputStream(outStream);
        out.setMethod(compressionMethod);
        compression = compressionMethod;
    }

    @Override
    public void setCompressionMethod(int compressionMethod) {
        out.setMethod(compressionMethod);
        compression = compressionMethod;
    }

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     *
     * @throws FileNotFoundException When the requested file or folder can't be located.
     * @throws IOException           When an error occurs with reading or writing data.
     */
    @Override
    public void write(String relativePath, String absolutePath) throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        File f = new File(absolutePath);
        write(relativePath, f);
    }

    /**
     * @param relativePath path name for zip file
     * @param f            The file to write out.
     *
     * @throws FileNotFoundException When the requested file or folder can't be located.
     * @throws IOException           When an error occurs with reading or writing data.
     */
    @Override
    public void write(final String relativePath, final File f) throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        final boolean isGzip = decompress && f.getName().toLowerCase().endsWith(".gz");
        final String relative = isGzip && StringUtils.endsWith(relativePath, ".gz") ? StringUtils.removeEnd(relativePath, ".gz") : relativePath;
        try (final InputStream in = isGzip ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f)) {
            if (isGzip && compression == ZipOutputStream.STORED) {
                final File temp = writeIncoming(in);
                addEntry(relativePath, temp, f.lastModified());

                // Transfer bytes from the file to the ZIP file
                try (final InputStream input = new FileInputStream(temp)) {
                    IOUtils.copy(input, out);
                }

                // Complete the entry
                out.closeEntry();
                FileUtils.DeleteFile(temp);
            } else {
                addEntry(relativePath, f, f.lastModified());

                // Transfer bytes from the file to the ZIP file
                IOUtils.copy(in, out);

                // Complete the entry
                out.closeEntry();
            }
        }
    }

    protected void addEntry(final String relativePath, final File f, final long l) throws IOException {
        ZipEntry entry = new ZipEntry(relativePath);

        if (compression == ZipOutputStream.STORED) {
            CRC32           crc32           = new CRC32();
            int             n;
            FileInputStream fileinputstream = new FileInputStream(f);
            byte[]          rgb             = new byte[1000];
            while ((n = fileinputstream.read(rgb)) > -1) {
                crc32.update(rgb, 0, n);
            }

            fileinputstream.close();

            entry.setSize(f.length());
            entry.setCrc(crc32.getValue());
        }
        entry.setTime(l);

        out.putNextEntry(entry);
    }

    @Override
    public void write(String relativePath, InputStream is) throws IOException {
        if (compression == ZipOutputStream.STORED) {
            final File temp = writeIncoming(is);
            write(relativePath, temp);
            FileUtils.DeleteFile(temp);
        } else {
            ZipEntry entry = new ZipEntry(relativePath);
            out.putNextEntry(entry);

            // Transfer bytes from the file to the ZIP file
            IOUtils.copy(is, out);

            // Complete the entry
            out.closeEntry();
        }
    }


    /**
     * @throws IOException When an error occurs with reading or writing data.
     */
    @Override
    public void close() throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        out.close();
    }

    @Override
    public void extract(File archive, String destination, boolean deleteOnExtract) throws IOException {
        // Route through the same hardened, per-entry-validated extraction used by every other entry point in this
        // class, rather than Ant's Expand task: Ant doesn't validate entry names against the destination directory
        // at the version this project resolves, and offers no per-entry hook to clear the executable bit as each
        // file is written (the previous approach had to sweep the whole destination tree afterward instead, which
        // followed symlinks it shouldn't have). moveExistingToHistory=false preserves Expand's original
        // plain-overwrite behaviour: it never moved a conflicting file into archive history.
        extractMapFromFile(archive, destination, true, null, null, false);

        if (deleteOnExtract) {
            archive.deleteOnExit();
        }
    }

    @Override
    public List<File> extract(InputStream is, String dir) throws IOException {
        return new ArrayList<>(extractMap(is, dir, true, null).values());
    }

    public Map<String, File> extractMap(final InputStream is, final String dir) throws IOException {
        return extractMap(is, dir, true, null);
    }

    @Override
    public List<File> extract(InputStream is, String destination, boolean overwrite, EventMetaI ci) throws IOException {
        return extract(is, destination, overwrite, ci, null);
    }

    @Override
    public List<File> extract(InputStream is, String destination, boolean overwrite, EventMetaI ci, IOFileFilter filter) throws IOException {
        return new ArrayList<>(extractMap(is, destination, overwrite, ci, filter).values());
    }

    public Map<String, File> extractMap(final InputStream is, final String destination, final boolean overwrite, final EventMetaI ci) throws IOException {
        return extractMap(is, destination, overwrite, ci, null);
    }

    public Map<String, File> extractMap(final InputStream is, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        // A plain InputStream isn't seekable, so it has to be buffered before it can be scanned and extracted.
        // Buffer it under the XNAT cache path rather than the default java.io.tmpdir, which may be tmpfs
        // (RAM-backed) in containerized deployments -- buffering a large upload there could otherwise exhaust memory.
        // This protects every caller of extract(InputStream, ...) uniformly, not just ZipUtils.extractFile.
        final File buffered = bufferToCache(is, "zip-upload-");
        try {
            return extractMapFromFile(buffered, destination, overwrite, ci, filter);
        } finally {
            FileUtils.DeleteFile(buffered);
        }
    }

    /**
     * Extracts directly from an already-materialized archive file, avoiding any redundant buffering copy.
     *
     * @see #extractMapFromFile(File, String, boolean, EventMetaI, IOFileFilter)
     */
    List<File> extractFromFile(final File zipFile, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        return new ArrayList<>(extractMapFromFile(zipFile, destination, overwrite, ci, filter).values());
    }

    Map<String, File> extractMapFromFile(final File zipFile, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        return extractMapFromFile(zipFile, destination, overwrite, ci, filter, true);
    }

    /**
     * Scans the given zip file to confirm every entry resolves within the destination directory and, if so,
     * extracts it directly -- avoiding any redundant buffering copy when the caller already has a materialized,
     * seekable file. If any entry does not, the entire upload is rejected via
     * {@link #rejectArchiveUpload(File, String, Path, List)} and nothing is extracted.
     *
     * <p>The scan and the extraction both read entries from the <em>same</em> {@link ZipFile} instance -- reading
     * the entry names once (from the central directory) to decide whether the upload is safe, then extracting via
     * {@link ZipFile#getInputStream(ZipEntry)} against those same {@link ZipEntry} objects. A zip file technically
     * carries two, independent copies of each entry's metadata: the central directory (at the end of the file) and
     * a local file header (immediately before that entry's compressed data). A tool that scans one and extracts via
     * the other -- e.g. scanning with {@link ZipFile}, which reads the central directory, but extracting with
     * {@link ZipInputStream}, which reads local headers as it goes -- can be fed a zip whose local header name
     * differs from its central-directory name (or that has no central-directory entry at all): the scan sees the
     * safe name, the extraction writes to the unsafe one. Reading both from the same {@link ZipFile} instance closes
     * that gap by construction, rather than relying on the two parsers agreeing.</p>
     *
     * <p>{@link ZipFile} is stricter than the streaming, local-header-based extraction this project used before
     * path-traversal protection was added: it refuses some inputs (a truncated archive, a raw gzip stream misnamed
     * {@code .zip}, certain legacy CP437-encoded entry names) that the old approach tolerated. Rather than hard-
     * failing those uploads, a {@link ZipException} opening the archive as a {@link ZipFile} falls back to
     * {@link #extractUsingZipInputStream}, which reproduces the old, tolerant behaviour -- scanning and extracting
     * via two passes of {@link ZipInputStream} instead, so it's still immune to the local/central-directory mismatch
     * above (both passes read the same local-header view).</p>
     *
     * @param zipFile     The zip file to scan and extract.
     * @param destination The destination folder to extract into.
     * @param overwrite   Whether existing files at the destination should be overwritten.
     * @param ci          Event metadata used when moving overwritten files to history.
     * @param filter      An optional filter restricting which entries are extracted.
     * @param moveExistingToHistory Whether an existing file at the destination should be moved into archive history
     *                              before being overwritten, or simply overwritten in place.
     *
     * @return The extracted files, keyed by their entry name.
     *
     * @throws IOException When an error occurs reading the archive, or when the upload is rejected.
     */
    private Map<String, File> extractMapFromFile(final File zipFile, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter, final boolean moveExistingToHistory) throws IOException {
        final File destinationFolder = new File(destination);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        if (zipFile.length() == 0) {
            // A genuinely empty file has no entries to scan or extract, and opening it as a ZipFile would throw
            // "zip file is empty" here, before extraction even gets a chance to run.
            return new HashMap<>();
        }

        // Only a failure to open/parse the archive's central directory at all -- e.g. a truncated archive, a raw
        // gzip stream misnamed .zip, or certain legacy CP437-encoded entry names -- falls back to the tolerant,
        // ZipInputStream-based extraction below. That's deliberately scoped to just this open call: a ZipException
        // once extraction is under way (e.g. one corrupt entry's compressed data) must NOT trigger the same
        // fallback, since some entries may already have been written to destination by then, and the fallback
        // starts extracting the whole archive again from scratch -- it has no idea those entries already exist,
        // and would re-run MoveToHistory (or misreport them as pre-existing duplicates) for entries this pass
        // already handled.
        final ZipFile zip;
        try {
            zip = new ZipFile(zipFile);
        } catch (final ZipException e) {
            log.debug("{} could not be opened as a well-formed zip archive ({}); falling back to a tolerant, streaming extraction.", zipFile, e.getMessage());
            return extractUsingZipInputStream(zipFile, destinationFolder, destination, overwrite, ci, filter, moveExistingToHistory);
        }

        final List<String>      unsafeEntries;
        final Map<String, File> extractedFiles = new HashMap<>();
        try (zip) {
            final List<? extends ZipEntry> entries = Collections.list(zip.entries());

            unsafeEntries = new ArrayList<>();
            for (final ZipEntry entry : entries) {
                if (!FileUtils.isCanonicalPath(destinationFolder, entry.getName())) {
                    unsafeEntries.add(entry.getName());
                }
            }

            if (unsafeEntries.isEmpty()) {
                for (final ZipEntry entry : entries) {
                    extractZipEntry(zip, entry, destination, overwrite, ci, filter, moveExistingToHistory, extractedFiles);
                }
            }
        }

        if (!unsafeEntries.isEmpty()) {
            // The ZipFile handle above is already closed at this point -- rejectArchiveUpload copies the underlying
            // file, which should never be attempted while this process still holds it open.
            rejectArchiveUpload(zipFile, "upload.zip", Path.of(destination), unsafeEntries);
            return new HashMap<>();
        }
        return extractedFiles;
    }

    private void extractZipEntry(final ZipFile zip, final ZipEntry entry, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter, final boolean moveExistingToHistory, final Map<String, File> extractedFiles) throws IOException {
        final String name = entry.getName();
        if (entry.isDirectory()) {
            final File subfolder = new File(destination, name);
            if (!subfolder.exists()) {
                subfolder.mkdirs();
            }
            extractedFiles.put(name, subfolder);
            return;
        }

        try (final InputStream entryStream = zip.getInputStream(entry)) {
            writeExtractedEntry(entryStream, destination, name, overwrite, ci, filter, moveExistingToHistory, extractedFiles);
        }
    }

    /**
     * Writes one non-directory entry's content to disk, applying the overwrite/filter/history/executable-bit policy
     * every zip extraction path in this class shares -- {@link #extractZipEntry} (the common, {@link ZipFile}-based
     * path) and {@link #extractUsingZipInputStream} (the tolerant fallback) both call this rather than each keeping
     * their own copy of that policy, so a future change to it can't be made in one and missed in the other.
     *
     * @param entryContent The entry's content, not yet consumed. This method reads it fully but does not close it --
     *                      the caller owns its lifecycle: a {@link ZipFile#getInputStream(ZipEntry)} result needs to
     *                      be closed per entry, while a {@link ZipInputStream} is shared across every entry in the
     *                      archive and must never be closed until all of them are done.
     */
    private void writeExtractedEntry(final InputStream entryContent, final String destination, final String name, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter, final boolean moveExistingToHistory, final Map<String, File> extractedFiles) throws IOException {
        final File f = new File(destination, name);
        if (filter == null || filter.accept(f)) {
            if (f.exists() && !overwrite) {
                _duplicates.add(name);
            } else {
                if (f.exists() && moveExistingToHistory) {
                    FileUtils.MoveToHistory(f, EventUtils.getTimestamp(ci));
                }
                f.getParentFile().mkdirs();
                final File absolute = f.getAbsoluteFile();
                Files.copy(entryContent, absolute.toPath(), StandardCopyOption.REPLACE_EXISTING);
                FileUtils.clearExecutable(absolute);
                extractedFiles.put(name, absolute);
            }
        } else {
            log.warn("File {} was rejected by the provided filter and will not be extracted.", name);
        }
    }

    /**
     * Tolerant fallback used when {@link ZipFile} refuses to open the archive at all (see the {@link ZipException}
     * handling in {@link #extractMapFromFile}). Scans and extracts using two separate passes of
     * {@link ZipInputStream} over the same file -- unlike mixing a {@link ZipFile} scan with a
     * {@link ZipInputStream} extraction, both passes here read entry names from the same place (the local file
     * headers, in stream order), so there's no way for the name that was validated to differ from the name that
     * gets written.
     */
    private Map<String, File> extractUsingZipInputStream(final File zipFile, final File destinationFolder, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter, final boolean moveExistingToHistory) throws IOException {
        final List<String> unsafeEntries = new ArrayList<>();
        try (final InputStream fis = new FileInputStream(zipFile);
             final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!FileUtils.isCanonicalPath(destinationFolder, entry.getName())) {
                    unsafeEntries.add(entry.getName());
                }
            }
        }
        if (!unsafeEntries.isEmpty()) {
            rejectArchiveUpload(zipFile, "upload.zip", Path.of(destination), unsafeEntries);
            return new HashMap<>();
        }

        final Map<String, File> extractedFiles = new HashMap<>();
        try (final InputStream fis = new FileInputStream(zipFile);
             final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String name = entry.getName();
                if (!entry.isDirectory()) {
                    // Defense in depth: this exact name was already validated during the scan pass above (and both
                    // passes read the same local-header stream, so it can't have changed) -- but an extraction loop
                    // should never trust an entry name it hasn't checked itself either.
                    if (!FileUtils.isCanonicalPath(destinationFolder, name)) {
                        throw new IOException("Archive entry \"" + name + "\" resolves outside of the destination directory.");
                    }
                    writeExtractedEntry(zis, destination, name, overwrite, ci, filter, moveExistingToHistory, extractedFiles);
                } else {
                    final File subfolder = new File(destination, name);
                    if (!subfolder.exists()) {
                        subfolder.mkdirs();
                    }
                    extractedFiles.put(name, subfolder);
                }
            }
        }
        return extractedFiles;
    }

    public static void Unzip(File f) throws IOException {
        String s     = f.getAbsolutePath();
        int    index = s.lastIndexOf('/');
        if (index == -1) {
            index = s.lastIndexOf('\\');
        }
        if (index == -1) {
            throw new IOException("Unknown Zip File.");
        }

        final String dir = s.substring(0, index);

        // Same rationale, and the same moveExistingToHistory=false plain-overwrite semantics, as extract(File,
        // String, boolean) above: this used to go through Ant's Expand task too.
        new ZipUtils().extractMapFromFile(f, dir, true, null, null, false);
    }

    @Override
    public void writeDirectory(File dir) throws IOException {
        writeDirectory("", dir);
    }

    private void writeDirectory(String parentPath, File dir) throws IOException {
        String       dirName = dir.getName() + "/";
        final File[] files   = dir.listFiles();
        if (files != null) {
            for (final File child : files) {
                if (child.isDirectory()) {
                    writeDirectory(parentPath + dirName, child);
                } else {
                    write(parentPath + dirName + child.getName(), child);
                }
            }
        }
    }

    /**
     * @return Returns the _compressionMethod.
     */
    @Override
    public int getCompressionMethod() {
        return this.compression;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#getDecompressFilesBeforeZipping()
     */
    @Override
    public boolean getDecompressFilesBeforeZipping() {
        return decompress;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#setDecompressFilesBeforeZipping(boolean)
     */
    @Override
    public void setDecompressFilesBeforeZipping(boolean method) {
        decompress = method;
    }

    @Override
    public List<String> getDuplicates() {
        return _duplicates;
    }

    private static File writeIncoming(final InputStream is) throws IOException {
        final File temp = File.createTempFile("temp", "");
        try (final FileOutputStream fos = new FileOutputStream(temp)) {
            IOUtils.copy(is, fos);
        }
        is.close();
        return temp;
    }
    private static Pattern getExtensionPattern(final String... extras) {
        return Pattern.compile(EXTENSION_PATTERN.formatted(Arrays.stream(extras).map(extra -> RegExUtils.removePattern(extra, "^\\.")).map(extra -> RegExUtils.replaceAll(extra, "\\.", "\\\\.")).collect(Collectors.joining("|"))));
    }

    private static final String EXTENSION_PATTERN = "^(?<filename>.*)\\.(?<extension>%s)$";
}
