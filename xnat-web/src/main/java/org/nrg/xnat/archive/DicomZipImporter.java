/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.helpers.ArchiveEntryFileWriterWrapper;
import org.nrg.xnat.helpers.TarEntryFileWriterWrapper;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ImporterHandler(handler = ImporterHandlerA.DICOM_ZIP_IMPORTER)
public final class DicomZipImporter extends ImporterHandlerA {
    public DicomZipImporter(final Object listenerControl,
                            final UserI u,
                            final FileWriterWrapperI fw,
                            final Map<String, Object> params)
            throws ClientException, IOException {
        super(listenerControl, u);
        this.listenerControl = getControlString();
        this.u = u;
        this.params = params;
        this.in = fw.getInputStream();
        this.format = Format.getFormat(fw.getName());
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.actions.importer.ImporterHandlerA#call()
     */
    @Override
    public List<String> call() throws ClientException, ServerException {
        final Set<String> uris = Sets.newLinkedHashSet();
        try {
            switch (format) {
                case ZIP:
                    try (final ZipInputStream zin = new ZipInputStream(in)) {
                        ZipEntry ze;
                        while (null != (ze = zin.getNextEntry())) {
                            if (!ze.isDirectory()) {
                                importEntry(new ZipEntryFileWriterWrapper(ze, zin), uris);
                            }
                        }
                    }
                    break;
                case TAR:
                case TGZ:
                    InputStream is = new BufferedInputStream(in);
                    if (format == Format.TGZ) {
                        is = new GZIPInputStream(is);
                    }
                    try (final TarArchiveInputStream zin = new TarArchiveInputStream(is)) {
                        TarArchiveEntry ze;
                        while (null != (ze = zin.getNextTarEntry())) {
                            if (!ze.isDirectory()) {
                                importEntry(new TarEntryFileWriterWrapper(ze, zin), uris);
                            }
                        }
                    }
                    break;
                default:
                    throw new ClientException("Unsupported format " + format);
            }
        } catch (IOException e) {
            throw new ClientException("unable to read data from file", e);
        }

        return Lists.newArrayList(uris);
    }

    private void importEntry(ArchiveEntryFileWriterWrapper entryFileWriter, Set<String> uris)
            throws ServerException, ClientException {
        final GradualDicomImporter importer = new GradualDicomImporter(listenerControl, u, entryFileWriter, params);
        importer.setIdentifier(getIdentifier());
        if (null != getNamer()) {
            importer.setNamer(getNamer());
        }
        uris.addAll(importer.call());
    }

    private final InputStream         in;
    private final Object              listenerControl;
    private final UserI               u;
    private final Map<String, Object> params;
    private final Format              format;
}
