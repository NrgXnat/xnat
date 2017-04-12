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
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ImporterHandler(handler = ImporterHandlerA.DICOM_ZIP_IMPORTER)
public final class DicomZipImporter extends ImporterHandlerA {
    public DicomZipImporter(final Object listenerControl,
                            final UserI u,
                            final FileWriterWrapperI fw,
                            final Map<String, Object> params)
            throws ClientException, IOException {
        super(listenerControl, u, fw, params);
        this.listenerControl = listenerControl;
        this.u = u;
        this.params = params;
        this.in = fw.getInputStream();
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.actions.importer.ImporterHandlerA#call()
     */
    @Override
    public List<String> call() throws ClientException, ServerException {
        try {
            try (final ZipInputStream zin = new ZipInputStream(in)) {
                final Set<String> uris = Sets.newLinkedHashSet();
                ZipEntry ze;
                while (null != (ze = zin.getNextEntry())) {
                    if (!ze.isDirectory()) {
                        final GradualDicomImporter importer = new GradualDicomImporter(listenerControl, u, new ZipEntryFileWriterWrapper(ze, zin), params);
                        importer.setIdentifier(getIdentifier());
                        if (null != getNamer()) {
                            importer.setNamer(getNamer());
                        }
                        uris.addAll(importer.call());
                    }
                }
                return Lists.newArrayList(uris);
            }
        } catch (IOException e) {
            throw new ClientException("unable to read data from zip file", e);
        }
    }

    private final InputStream         in;
    private final Object              listenerControl;
    private final UserI               u;
    private final Map<String, Object> params;
}
