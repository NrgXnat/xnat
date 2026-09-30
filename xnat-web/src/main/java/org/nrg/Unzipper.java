/*
 * PrearcImporter: org.nrg.Unzipper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.zip.PathTraversalScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * Unpacks zip files
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class Unzipper extends Unpacker {
    private final Logger logger = LoggerFactory.getLogger(Unzipper.class);

    @Override
    public final boolean unpack(final File zipfile, final File destination) {
        final File dest = destination == null ? zipfile.getParentFile() : destination;
        dest.mkdirs();

        final ZipFile zip;
        try {
            zip = new ZipFile(zipfile);
        } catch (final FileNotFoundException e) {
            publishFailure(zipfile, "unable to locate: " + e.getMessage());
            logger.error("could not find zipfile " + zipfile, e);
            return false;
        } catch (final ZipException e) {
            // Some legitimate but non-standard archives (an empty file, certain legacy CP437-encoded entry
            // names, a truncated/streamed file with no proper central directory, etc.) can't be opened as a
            // ZipFile at all, even though the tolerant streaming path below can still read them entry-by-entry.
            // This fallback is deliberately scoped to just this open call: nothing has been written to dest yet
            // at this point, unlike a ZipException raised once extraction is already under way.
            logger.debug("{} could not be opened as a well-formed zip archive ({}); falling back to a tolerant, streaming extraction.", zipfile, e.getMessage());
            return unpackUsingZipInputStream(zipfile, dest);
        } catch (final IOException e) {
            publishFailure(zipfile, "unable to unpack " + zipfile + ": " + e.getMessage());
            logger.error("unable to unpack " + zipfile, e);
            return false;
        }

        try (zip) {
            publishStatus(zipfile, "unzipping");

            // Scanning and extracting both read entries from this same ZipFile instance -- reading names once
            // (from the central directory) to decide whether the upload is safe, then extracting via
            // zip.getInputStream(entry) against those same ZipEntry objects. A zip file carries two independent
            // copies of each entry's metadata (the central directory and a local file header immediately before
            // that entry's data); a tool that scans one and extracts via the other -- e.g. scanning with a
            // ZipFile but extracting with a separate ZipInputStream -- can be fed a zip whose local header name
            // differs from its central-directory name: the scan sees the safe name, the extraction writes the
            // unsafe one. Reading both from the same ZipFile instance closes that gap by construction, the same
            // as ZipUtils.extractMapFromFile() does. This also avoids ZipInputStream's cost for the scan: it can
            // only advance past an entry by fully inflating it, so a ZipInputStream-based scan of a large archive
            // costs as much as actually decompressing it a second time, where ZipFile reads only the compact
            // central-directory index.
            final List<? extends ZipEntry>     entries  = Collections.list(zip.entries());
            final Iterator<? extends ZipEntry> iterator = entries.iterator();
            final List<String> unsafeEntries = PathTraversalScanner.findPathTraversalEntries(() ->
                    iterator.hasNext() ? iterator.next().getName().replace(notFileSeparator, File.separatorChar) : null,
                    dest);

            if (!unsafeEntries.isEmpty()) {
                // Reject the whole upload before extracting anything -- the same atomic scan-then-extract
                // guarantee ZipUtils.extractMapFromFile() and TarUtils's tar handling already provide for every
                // other archive upload path in this project: an archive containing even one path-traversal entry
                // must never result in its other, legitimate entries being written to disk.
                publishFailure(zipfile, "rejected: " + unsafeEntries.size()
                        + (unsafeEntries.size() == 1 ? " entry resolves" : " entries resolve")
                        + " outside of the destination directory: " + unsafeEntries);
                return false;
            }

            for (final ZipEntry entry : entries) {
                extractZipFileEntry(zip, entry, dest);
            }
            publishSuccess(zipfile, "unzipped");
            return true;
        } catch (final IOException e) {
            publishFailure(zipfile, "unable to unpack " + zipfile + ": " + e.getMessage());
            logger.error("unable to unpack " + zipfile, e);
            return false;
        }
    }

    private void extractZipFileEntry(final ZipFile zip, final ZipEntry entry, final File destination) throws IOException {
        final String name    = entry.getName().replace(notFileSeparator, File.separatorChar);
        final File   outfile = new File(destination, name);
        logger.trace("extracting {} to {}", entry.getName(), outfile);
        if (entry.isDirectory()) {
            outfile.mkdirs();
            return;
        } else if (outfile.exists()) {
            publishWarning(outfile, "file exists, will not overwrite");
            return;
        }

        outfile.getParentFile().mkdirs();
        try (final InputStream entryStream = zip.getInputStream(entry);
             final FileOutputStream fos = new FileOutputStream(outfile)) {
            ByteStreams.copy(entryStream, fos);
        }
        // Every extracted file is cleared of any executable bit its archive metadata may have carried, the
        // same as every other extraction entry point this project has (ZipUtils, TarUtils).
        FileUtils.clearExecutable(outfile);
    }

    private static final char notFileSeparator = '/' == File.separatorChar ? '\\' : '/';

    /**
     * Tolerant fallback used when {@link ZipFile} refuses to open the archive at all (see the {@link ZipException}
     * handling in unpack(File, File)). Scans and extracts using two separate passes of {@link ZipInputStream}
     * over the same file -- unlike mixing a {@link ZipFile} scan with a {@link ZipInputStream} extraction, both
     * passes here read entry names from the same place (the local file headers, in stream order), so there's no
     * way for the name that was validated to differ from the name that gets written. This costs a full second
     * decompression pass that the {@link ZipFile}-based path above avoids, but it's only reached for a malformed
     * or non-standard archive, not the common case.
     */
    private boolean unpackUsingZipInputStream(final File zipfile, final File destination) {
        final List<String> unsafeEntries;
        try {
            unsafeEntries = findPathTraversalEntriesByStreaming(zipfile, destination);
        } catch (FileNotFoundException e) {
            publishFailure(zipfile, "unable to locate: " + e.getMessage());
            logger.error("could not find zipfile " + zipfile, e);
            return false;
        } catch (IOException e) {
            publishFailure(zipfile, "unable to unpack " + zipfile + ": " + e.getMessage());
            logger.error("unable to unpack " + zipfile, e);
            return false;
        }

        if (!unsafeEntries.isEmpty()) {
            publishFailure(zipfile, "rejected: " + unsafeEntries.size()
                    + (unsafeEntries.size() == 1 ? " entry resolves" : " entries resolve")
                    + " outside of the destination directory: " + unsafeEntries);
            return false;
        }

        final FileInputStream fis;
        try {
            IOException ioexception = null;
            fis = new FileInputStream(zipfile);
            try {
                final BufferedInputStream bis = new BufferedInputStream(fis);
                try {
                    final ZipInputStream zis = new ZipInputStream(bis);
                    try {
                        unpack(zipfile, zis, destination);
                        publishSuccess(zipfile, "unzipped");
                    } catch (IOException e) {
                        throw ioexception = e;
                    } finally {
                        try {
                            zis.close();
                        } catch (IOException e) {
                            throw ioexception = null == ioexception ? e : ioexception;
                        }
                    }
                } finally {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        throw ioexception = null == ioexception ? e : ioexception;
                    }
                }
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw null == ioexception ? e : ioexception;
                }
            }
        } catch (FileNotFoundException e) {
            publishFailure(zipfile, "unable to locate: " + e.getMessage());
            logger.error("could not find zipfile " + zipfile, e);
            return false;
        } catch (IOException e) {
            publishFailure(zipfile, "unable to unpack " + zipfile + ": " + e.getMessage());
            logger.error("unable to unpack " + zipfile, e);
            return false;
        }
        return true;
    }

    private List<String> findPathTraversalEntriesByStreaming(final File zipfile, final File destination) throws IOException {
        try (final InputStream fis = new FileInputStream(zipfile);
             final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
            return PathTraversalScanner.findPathTraversalEntries(() -> {
                final ZipEntry ze = zis.getNextEntry();
                return ze == null ? null : ze.getName().replace(notFileSeparator, File.separatorChar);
            }, destination);
        }
    }

    /**
     * Unpacks from a stream.
     *
     * @param control         Control object.
     * @param zipInputStream  Zip input stream.
     * @param destination     Destination directory (must be non-null).
     * @throws IOException When an error occurs reading or writing data.
     */
    public final void unpack(final Object control, final ZipInputStream zipInputStream, final File destination) throws IOException {
        destination.mkdirs();
        for (ZipEntry ze = zipInputStream.getNextEntry(); ze != null; ze = zipInputStream.getNextEntry()) {
            final String name = ze.getName().replace(notFileSeparator, File.separatorChar);
            // Defense in depth: every entry name here was already validated by
            // findPathTraversalEntriesByStreaming() before unpackUsingZipInputStream() ever called this method --
            // but an extraction loop should never trust an entry name it hasn't checked itself either, so this
            // check stays even though it should never trip in practice (the same belt-and-suspenders approach
            // ZipUtils.extractUsingZipInputStream takes).
            if (!FileUtils.isCanonicalPath(destination, name)) {
                throw new IOException("Zip entry \"" + ze.getName() + "\" resolves outside of the destination directory.");
            }
            final File   outfile = new File(destination, name);
            logger.trace("extracting {} to {}", ze.getName(), outfile);
            if (ze.isDirectory()) {
                outfile.mkdirs();
                continue;
            } else if (outfile.exists()) {
                publishWarning(outfile, "file exists, will not overwrite");
                continue;
            }

            outfile.getParentFile().mkdirs();
            IOException            ioexception = null;
            final FileOutputStream fos         = new FileOutputStream(outfile);
            try {
                ByteStreams.copy(zipInputStream, fos);
            } catch (IOException e) {
                throw ioexception = e;
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw null == ioexception ? e : ioexception;
                }
            }
            // Every extracted file is cleared of any executable bit its archive metadata may have carried, the
            // same as every other extraction entry point this project has (ZipUtils, TarUtils).
            FileUtils.clearExecutable(outfile);
        }
    }
}
