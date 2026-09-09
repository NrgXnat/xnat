/*
 * core: org.nrg.xft.utils.zip.TarUtils
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
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import static org.nrg.xft.utils.zip.ZipUtils.bufferToCache;
import static org.nrg.xft.utils.zip.ZipUtils.rejectArchiveUpload;

/**
 * @author timo
 */
@Slf4j
public class TarUtils implements ZipI {
    TarOutputStream out                = null;
    int             _compressionMethod = ZipOutputStream.STORED;
    boolean         decompress         = false;
    private final List<String> _duplicates = new ArrayList<>();

    public void setOutputStream(OutputStream outStream) throws IOException {
        out = new TarOutputStream(outStream);
        out.setLongFileMode(TarOutputStream.LONGFILE_POSIX);
    }

    public void setOutputStream(OutputStream outStream, int compressionMethod) throws IOException {
        _compressionMethod = compressionMethod;
        if (compressionMethod == ZipOutputStream.DEFLATED) {
            GZIPOutputStream gzip = new GZIPOutputStream(outStream);
            out = new TarOutputStream(gzip);
        } else {
            out = new TarOutputStream(outStream);
        }
        out.setLongFileMode(TarOutputStream.LONGFILE_POSIX);
    }

    @Override
    public List<File> extract(InputStream is, String dir) throws IOException {
        return extract(is, dir, true, null);
    }

    @Override
    public List<File> extract(final InputStream is, final String dir, final boolean overwrite, final EventMetaI ci) throws IOException {
        return extract(is, dir, overwrite, ci, null);
    }

    @Override
    public List<File> extract(final InputStream is, final String dir, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        // A plain InputStream isn't seekable, so it has to be buffered before it can be scanned and extracted in two
        // passes. Buffer it under the XNAT cache path rather than the default java.io.tmpdir, which may be tmpfs
        // (RAM-backed) in containerized deployments -- buffering a large upload there could otherwise exhaust memory.
        final File buffered = bufferToCache(is, "tar-upload-");
        try {
            return extractFromFile(buffered, dir, overwrite, ci, filter);
        } finally {
            FileUtils.DeleteFile(buffered);
        }
    }

    /**
     * Scans the given archive file for path-traversal entries and, if it's clean, extracts it directly -- avoiding
     * any redundant buffering copy when the caller already has a materialized, seekable file. If any entry is
     * unsafe, the entire upload is rejected via {@link ZipUtils#rejectArchiveUpload(File, String, Path, List)} and
     * nothing is extracted.
     *
     * @param archiveFile The tar (optionally gzipped, per {@link #_compressionMethod}) file to scan and extract.
     * @param dir         The destination folder to extract into.
     * @param overwrite   Whether existing files at the destination should be overwritten.
     * @param ci          Event metadata used when moving overwritten files to history.
     * @param filter      An optional filter restricting which entries are extracted.
     *
     * @return The files that were extracted.
     *
     * @throws IOException When an error occurs reading the archive, or when the upload is rejected.
     */
    List<File> extractFromFile(final File archiveFile, final String dir, final boolean overwrite, final EventMetaI ci, final IOFileFilter filter) throws IOException {
        final File dest = new File(dir);
        dest.mkdirs();

        final List<String> unsafeEntries = findPathTraversalEntries(archiveFile, dest);
        if (!unsafeEntries.isEmpty()) {
            rejectArchiveUpload(archiveFile, _compressionMethod == ZipOutputStream.DEFLATED ? "upload.tar.gz" : "upload.tar", Path.of(dir), unsafeEntries);
            return new ArrayList<>();
        }

        final List<File> extractedFiles = new ArrayList<>();
        try (final InputStream fis = new FileInputStream(archiveFile);
             final TarInputStream tis = _compressionMethod == ZipOutputStream.DEFLATED ? new TarInputStream(new GZIPInputStream(fis)) : new TarInputStream(fis)) {
            TarEntry te = tis.getNextEntry();
            while (te != null) {
                final String name     = te.getName();
                final File   destPath = new File(dest, name);
                if (te.isDirectory()) {
                    destPath.mkdirs();
                } else {
                    if (destPath.exists() && !overwrite) {
                        _duplicates.add(name);
                    } else {
                        if (filter == null || filter.accept(destPath)) {
                            if (destPath.exists()) {
                                FileUtils.MoveToHistory(destPath, EventUtils.getTimestamp(ci));
                            }
                            destPath.getParentFile().mkdirs();
                            log.debug("Writing: {}", name);
                            try (final FileOutputStream output = new FileOutputStream(destPath)) {
                                tis.copyEntryContents(output);
                            }
                            // A tar entry can carry a Unix mode (e.g. from `chmod +x` on the machine that built the
                            // archive) marking it executable; never let an extracted file inherit that.
                            FileUtils.clearExecutable(destPath);
                            extractedFiles.add(destPath);
                        } else {
                            log.warn("File {} was rejected by the provided filter and will not be extracted.", name);
                        }
                    }
                }
                te = tis.getNextEntry();
            }
        }
        return extractedFiles;
    }

    /**
     * Scans every entry of the specified tar (optionally gzipped, per {@link #_compressionMethod}) file and returns
     * the names of any entries whose relative path, once resolved against <b>destinationDir</b>, escapes that
     * directory (a path traversal / "zip-slip" attempt).
     *
     * @param archiveFile    The tar file to scan.
     * @param destinationDir The directory the archive is intended to be extracted into.
     *
     * @return The (possibly empty) list of unsafe entry names found in the archive.
     *
     * @throws IOException When an error occurs reading the archive.
     */
    private List<String> findPathTraversalEntries(final File archiveFile, final File destinationDir) throws IOException {
        final List<String> unsafeEntries = new ArrayList<>();
        try (final InputStream fis = new FileInputStream(archiveFile);
             final TarInputStream tis = _compressionMethod == ZipOutputStream.DEFLATED ? new TarInputStream(new GZIPInputStream(fis)) : new TarInputStream(fis)) {
            TarEntry te;
            while ((te = tis.getNextEntry()) != null) {
                if (!FileUtils.isCanonicalPath(destinationDir, te.getName())) {
                    unsafeEntries.add(te.getName());
                }
            }
        }
        return unsafeEntries;
    }

    @Override
    public List<String> getDuplicates() {
        return _duplicates;
    }

    public void extract(File f, String dir, boolean deleteZip) throws IOException {
        // Route through extractFromFile so this direct File-based entry point gets the same path-traversal scan as
        // extract(InputStream, ...) -- this method used to write entries straight out with no validation at all.
        extractFromFile(f, dir, true, null, null);

        f.deleteOnExit();
    }

    @SuppressWarnings("unused")
    public File unGzip(final File f, final String dir, final boolean deleteZip) throws IOException {
        File destF = Path.of(dir, "upload.tar").toFile();

        try (final GZIPInputStream gis = new GZIPInputStream(new FileInputStream(f));
             final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destF))) {
            log.debug("Uploading file: {}", destF.getAbsolutePath());
            IOUtils.copy(gis, bos);
        }

        if (deleteZip) {
            f.deleteOnExit();
        }

        return destF;
    }

    /**
     * @param relativePath path name for zip file
     * @param absolutePath Absolute path used to load file.
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, String absolutePath) throws IOException {
        write(relativePath, new File(absolutePath));
    }

    /**
     * @param relativePath path name for zip file
     * @param in           The input stream.
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, InputStream in) throws IOException {
        TarEntry tarAdd = new TarEntry(relativePath);
        tarAdd.setModTime(Calendar.getInstance().getTimeInMillis());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        out.putNextEntry(tarAdd);
        // Write file to archive
        IOUtils.copy(in, out);
        in.close();
        out.closeEntry();
    }

    /**
     * @param relativePath path name for zip file
     * @param file         The file
     *
     * @throws IOException When an error occurs writing the file.
     */
    public void write(String relativePath, File file) throws IOException {
        TarEntry tarAdd = new TarEntry(file);
        tarAdd.setModTime(file.lastModified());
        tarAdd.setMode(TarEntry.LF_NORMAL);
        tarAdd.setName(relativePath.replace('\\', '/'));
        out.putNextEntry(tarAdd);
        // Write file to archive
        try (final FileInputStream in = new FileInputStream(file)) {
            IOUtils.copy(in, out);
        }
        out.closeEntry();
    }

    public void writeDirectory(File dir) throws IOException {
        writeDirectory("", dir);
    }

    private void writeDirectory(String parentPath, File dir) throws IOException {
        String dirName = dir.getName() + "/";
        final File[] files = dir.listFiles();
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
     * Closes the output stream.
     *
     * @throws IOException When the output stream is not defined.
     */
    public void close() throws IOException {
        if (out == null) {
            throw new IOException("Undefined OutputStream");
        }
        out.close();
    }

    /**
     * @return Returns the _compressionMethod.
     */
    public int getCompressionMethod() {
        return _compressionMethod;
    }

    /**
     * @param method The _compressionMethod to set.
     */
    public void setCompressionMethod(int method) {
        _compressionMethod = method;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#getDecompressFilesBeforeZipping()
     */
    public boolean getDecompressFilesBeforeZipping() {
        return decompress;
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.utils.zip.ZipI#setDecompressFilesBeforeZipping(boolean)
     */
    public void setDecompressFilesBeforeZipping(boolean method) {
        decompress = method;
    }
}
