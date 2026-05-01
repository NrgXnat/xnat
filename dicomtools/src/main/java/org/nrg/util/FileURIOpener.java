/*
 * DicomDB: org.nrg.util.FileURIOpener
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens URIs that are local file references.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class FileURIOpener implements Opener<URI> {
    /**
     * Returns a relative (no scheme) URI for the provided (possibly absolute) File
     * @param file    The file for which a URI should be created.
     * @return The URI for the submitted file.
     */
    public static URI toURI(final File file) {
        try {
            return new URI(file.getPath());
        } catch (URISyntaxException e) {
            return file.toURI();
        }
    }

    public static FileURIOpener getInstance() { return instance; }

    /*
     * (non-Javadoc)
     * @see org.nrg.util.IOUtils.Opener#open(java.lang.Object)
     */
    @SuppressWarnings("resource")
    public InputStream open(final URI uri) throws IOException {
        logger.debug("opening {} as {}", uri, uri.getPath());
        final String path = uri.getPath();
        final InputStream in = new FileInputStream(uri.getPath());
        return path.endsWith(".gz") ? new GZIPInputStream(in) : in;
    }

    private FileURIOpener() {}

    private static final Logger logger = LoggerFactory.getLogger(FileURIOpener.class);
    private static FileURIOpener instance = new FileURIOpener();
}
