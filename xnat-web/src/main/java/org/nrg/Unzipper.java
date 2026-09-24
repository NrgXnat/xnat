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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nrg.xft.utils.FileUtils;
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
    public final void unpack(final File zipfile, final File destination) {
        final FileInputStream fis;
        try {
            IOException ioexception = null;
            fis = new FileInputStream(zipfile);
            try {
                publishStatus(zipfile, "unzipping");

                final BufferedInputStream bis = new BufferedInputStream(fis);
                try {
                    final ZipInputStream zis = new ZipInputStream(bis);
                    try {
                        unpack(zipfile, zis, destination == null ? zipfile.getParentFile() : destination);
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
        } catch (IOException e) {
            publishFailure(zipfile, "unable to unpack " + zipfile + ": " + e.getMessage());
            logger.error("unable to unpack " + zipfile, e);
        }
    }

    private static final char notFileSeparator = '/' == File.separatorChar ? '\\' : '/';

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
        // This method used to catch its own IOException here and only report it via publishFailure, rather than
        // letting it propagate as the method's own "throws IOException" already promises. That swallowed the
        // path-traversal rejection below (and any other extraction failure): unpack(File, File) -- the only
        // caller -- has its own correct, more specific failure handling built around this method actually
        // throwing, but with the exception silently absorbed here it saw a normal return and published *success*
        // right after calling this method, and the caller of that (PrearcImporter) would go on to delete the
        // original archive and queue the partially-extracted, still-unsafe destination for import as if nothing
        // had gone wrong.
        for (ZipEntry ze = zipInputStream.getNextEntry(); ze != null; ze = zipInputStream.getNextEntry()) {
            final String name = ze.getName().replace(notFileSeparator, File.separatorChar);
            // Reject a path-traversal ("zip-slip") entry before anything is written for it: an entry name
            // containing e.g. "../" could otherwise resolve to a file outside of destination entirely.
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
