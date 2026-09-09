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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Expand;
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
        // Path-traversal protection lives inside ZipUtils/TarUtils' own extract(InputStream, ...) implementations
        // (see ZipUtils#extractMap and TarUtils#extract), so every caller of the ZipI interface is protected the
        // same way, not just this convenience wrapper.
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
     * Scans every entry of the specified zip file and returns the names of any entries whose relative path, once
     * resolved against <b>destinationDir</b>, escapes that directory (a path traversal / "zip-slip" attempt). Reads
     * the archive's central directory (via {@link ZipFile}) rather than streaming and discarding every entry's
     * compressed content, so scanning cost is proportional to the entry count, not the archive's total size.
     *
     * @param zipFile        The zip file to scan.
     * @param destinationDir The directory the archive is intended to be extracted into.
     *
     * @return The (possibly empty) list of unsafe entry names found in the archive.
     *
     * @throws IOException When an error occurs reading the archive.
     */
    private static List<String> findPathTraversalEntries(final File zipFile, final File destinationDir) throws IOException {
        if (zipFile.length() == 0) {
            // A genuinely empty file has no entries to scan, and zero entries is trivially safe from path traversal.
            // Opening it as a ZipFile would throw "zip file is empty" here -- before extraction even gets a chance
            // to run -- even though the actual extraction step (ZipInputStream, or Ant's Expand for the File-based
            // extract) is either fine with an empty archive or fails with its own, more appropriate error. Let
            // extraction handle it the way it always has instead of failing early in the scan.
            return Collections.emptyList();
        }
        final List<String> unsafeEntries = new ArrayList<>();
        try (final ZipFile zip = new ZipFile(zipFile)) {
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (!FileUtils.isCanonicalPath(destinationDir, entry.getName())) {
                    unsafeEntries.add(entry.getName());
                }
            }
        }
        return unsafeEntries;
    }

    /**
     * Rejects an archive upload (zip, tar, or tgz) that was found to contain one or more path-traversal entries. The
     * rejected archive is preserved in the XNAT cache folder along with an {@code upload_report.txt} file describing
     * why the upload was rejected, then an {@link IOException} is thrown so the caller knows the upload did not
     * succeed. Package-visible so both {@link ZipUtils} and {@link TarUtils} can share it.
     *
     * @param archiveFile     The (buffered) archive file that was rejected.
     * @param archiveFileName The file name to give the archive when it's copied into the cache folder, e.g.
     *                        {@code upload.zip} or {@code upload.tar}.
     * @param destination     The destination the archive was going to be extracted into.
     * @param unsafeEntries   The unsafe entry names found in the archive.
     *
     * @throws IOException Always thrown to indicate the upload was rejected.
     */
    static void rejectArchiveUpload(final File archiveFile, final String archiveFileName, final Path destination, final List<String> unsafeEntries) throws IOException {
        // Use createTempDirectory (atomic, collision-proof) rather than a plain timestamp-named folder: two uploads
        // rejected in the same millisecond would otherwise resolve to the same folder and silently overwrite each
        // other's archive and report. The timestamp is kept as a human-readable prefix only, not for uniqueness.
        final Path rejectedUploadsRoot = getXnatCachePath().resolve("RejectedUploads");
        Files.createDirectories(rejectedUploadsRoot);
        final Path cacheFolder = Files.createTempDirectory(rejectedUploadsRoot, FileUtils.getMsTimestamp() + "-");

        final File rejectedArchive = cacheFolder.resolve(archiveFileName).toFile();
        org.apache.commons.io.FileUtils.copyFile(archiveFile, rejectedArchive);

        final boolean       singular = unsafeEntries.size() == 1;
        final StringBuilder report   = new StringBuilder();
        report.append("XNAT Archive Upload Rejected").append(System.lineSeparator());
        report.append("Timestamp: ").append(new Date()).append(System.lineSeparator());
        report.append("Intended destination: ").append(destination).append(System.lineSeparator());
        report.append("Rejected archive: ").append(rejectedArchive.getAbsolutePath()).append(System.lineSeparator());
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

        // Notify the site administrator, with the report content as the message body. This is best-effort: wrapped
        // in its own try/catch because AdminUtils.sendAdminEmail can itself throw (e.g. it calls
        // XDAT.getNotificationsPreferences() without a null-check of its own, which throws an uncaught NPE if
        // Spring isn't fully up) -- a notification failure must never replace, or prevent, the rejection below. The
        // report is HTML-escaped before being embedded, since it includes the archive's own entry names verbatim,
        // and those come from the untrusted upload itself.
        try {
            AdminUtils.sendAdminEmail("XNAT Archive Upload Rejected",
                                       "<pre>" + StringEscapeUtils.escapeHtml4(report.toString()) + "</pre>");
        } catch (final Exception e) {
            log.warn("Unable to send the archive-upload-rejected notification email to the site administrator", e);
        }

        // The absolute path goes to the server log and the report file only -- both are server-local surfaces an
        // administrator already has file system access to. The thrown message is what propagates back to callers
        // (some of which forward exception messages to external clients, e.g. as an HTTP error body), so it must
        // never include a local path.
        log.warn("Rejected archive upload targeting {}: {} unsafe {} detected. Archive and report written to {}",
                 destination, unsafeEntries.size(), singular ? "entry" : "entries", cacheFolder);

        throw new IOException("Archive upload rejected: archive contains " + unsafeEntries.size() +
                               (singular ? " entry" : " entries") +
                               " with a relative path that resolves outside of the destination directory. " +
                               "This upload has been logged for review by a site administrator.");
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
        // Ant's Expand task has no built-in path-traversal protection at the version this project resolves, so scan
        // for unsafe entries ourselves before handing the archive to it. If any entry is unsafe, the whole upload is
        // rejected and Expand never runs.
        final List<String> unsafeEntries = findPathTraversalEntries(archive, new File(destination));
        if (!unsafeEntries.isEmpty()) {
            rejectArchiveUpload(archive, "upload.zip", Path.of(destination), unsafeEntries);
            return;
        }

        final class Expander extends Expand {
            public Expander() {
                setProject(new Project());
                getProject().init();
                setTaskType("unzip");
                setTaskName("unzip");
                setOwningTarget(new Target());
            }
        }
        final File destinationFolder = new File(destination);
        Expander expander = new Expander();
        expander.setSrc(archive);
        expander.setDest(destinationFolder);
        expander.execute();

        // Ant's Expand task doesn't give us a per-entry hook, and doesn't apply the archive's own permission bits
        // itself, but the underlying file system's default file-creation mode could still leave a freshly extracted
        // file executable (e.g. a permissive umask) -- so sweep the destination afterward.
        clearExecutableRecursively(destinationFolder);

        if (deleteOnExtract) {
            archive.deleteOnExit();
        }
    }

    /**
     * Recursively clears the executable permission bit on every regular file under <b>dir</b> (directories are left
     * alone, since clearing a directory's execute bit would make it untraversable). Used after an extraction method
     * that doesn't offer a per-entry hook to do this as each file is written.
     *
     * @param dir The directory to sweep.
     */
    private static void clearExecutableRecursively(final File dir) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }
        try (final Stream<Path> paths = Files.walk(dir.toPath())) {
            paths.filter(Files::isRegularFile).forEach(path -> FileUtils.clearExecutable(path.toFile()));
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
        // A plain InputStream isn't seekable, so it has to be buffered before it can be scanned and extracted in two
        // passes. Buffer it under the XNAT cache path rather than the default java.io.tmpdir, which may be tmpfs
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

    /**
     * Scans the given zip file for path-traversal entries and, if it's clean, extracts it directly -- avoiding any
     * redundant buffering copy when the caller already has a materialized, seekable file. If any entry is unsafe,
     * the entire upload is rejected via {@link #rejectArchiveUpload(File, String, Path, List)} and nothing is
     * extracted.
     *
     * @param zipFile     The zip file to scan and extract.
     * @param destination The destination folder to extract into.
     * @param overwrite   Whether existing files at the destination should be overwritten.
     * @param ci          Event metadata used when moving overwritten files to history.
     * @param filter      An optional filter restricting which entries are extracted.
     *
     * @return The extracted files, keyed by their entry name.
     *
     * @throws IOException When an error occurs reading the archive, or when the upload is rejected.
     */
    Map<String, File> extractMapFromFile(final File zipFile, final String destination, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        final File destinationFolder = new File(destination);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        final List<String> unsafeEntries = findPathTraversalEntries(zipFile, destinationFolder);
        if (!unsafeEntries.isEmpty()) {
            rejectArchiveUpload(zipFile, "upload.zip", Path.of(destination), unsafeEntries);
            return new HashMap<>();
        }

        final Map<String, File> extractedFiles = new HashMap<>();

        // Loop over all of the entries in the zip file
        //  Create a ZipInputStream to read the zip file
        try (final InputStream fis = new FileInputStream(zipFile);
             final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String name = entry.getName();
                if (!entry.isDirectory()) {
                    final File f = new File(destination, name);

                    if (filter == null || filter.accept(f)) {
                        if (f.exists() && !overwrite) {
                            _duplicates.add(name);
                        } else {
                            if (f.exists()) {
                                FileUtils.MoveToHistory(f, EventUtils.getTimestamp(ci));
                            }
                            f.getParentFile().mkdirs();
                            File absolute = f.getAbsoluteFile();
                            Path filePath = absolute.toPath();
                            Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                            FileUtils.clearExecutable(absolute);

                            extractedFiles.put(name, absolute);
                        }
                    } else {
                        log.warn("File {} was rejected by the provided filter and will not be extracted.", name);
                    }
                } else {
                    final File subfolder = new File(destination, name);
                    if (!subfolder.exists()) {
                        subfolder.mkdirs();
                    }
                    extractedFiles.put(name, subfolder);
                }
            }
            zis.closeEntry();
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

        String dir = s.substring(0, index);

        final class Expander extends Expand {
            public Expander() {
                setProject(new Project());
                getProject().init();
                setTaskType("unzip");
                setTaskName("unzip");
                setOwningTarget(new Target());
            }
        }
        final File destinationFolder = new File(dir);
        Expander expander = new Expander();
        expander.setSrc(new File(s));
        expander.setDest(destinationFolder);
        expander.execute();

        // Same rationale as the sweep in extract(File, String, boolean): Ant's Expand task offers no per-entry hook
        // to clear the executable bit as each file is written, so sweep the destination afterward.
        clearExecutableRecursively(destinationFolder);
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
