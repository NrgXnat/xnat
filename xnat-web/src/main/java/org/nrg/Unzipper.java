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
        try {
            for (ZipEntry ze = zipInputStream.getNextEntry(); ze != null; ze = zipInputStream.getNextEntry()) {
                final String name    = ze.getName().replace(notFileSeparator, File.separatorChar);
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
            }
        } catch (IOException e) {
            publishFailure(control, "unable to unpack: " + e.getMessage());
        }
    }
}
