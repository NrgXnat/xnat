/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@ImporterHandler(handler = ImporterHandlerA.DICOM_ZIP_IMPORTER)
public final class DicomZipImporter extends ImporterHandlerA {
    private final InputStream in;
    private final Object listenerControl;
    private final UserI u;
    private final Map<String,Object> params;
    private DicomObjectIdentifier<XnatProjectdata> identifier;
    private DicomFileNamer namer = null;

    public DicomZipImporter(final Object listenerControl,
            final UserI u,
            final FileWriterWrapperI fw,
            final Map<String, Object> params)
    throws ClientException,IOException {
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
    public List<String> call() throws ClientException,ServerException {
        try {
            IOException ioexception = null;
            final ZipInputStream zin = new ZipInputStream(in);
            try {
                final Set<String> uris = Sets.newLinkedHashSet();
                ZipEntry ze;
                while (null != (ze = zin.getNextEntry())) {
                    if (!ze.isDirectory()) {
                        final GradualDicomImporter importer = new GradualDicomImporter(listenerControl,
                                u, new ZipEntryFileWriterWrapper(ze, zin), params);
                        importer.setIdentifier(identifier);
                        if (null != namer) {
                            importer.setNamer(namer);
                        }
                        uris.addAll(importer.call());
                    }
                }
                return Lists.newArrayList(uris);
            } finally {
                try {
                    zin.close();
                } catch (IOException e) {
                    throw null == ioexception ? e : ioexception;
                }
            }
        } catch (IOException e) {
            throw new ClientException("unable to read data from zip file", e);
        }
    }
    
    public DicomZipImporter setIdentifier(DicomObjectIdentifier<XnatProjectdata> identifier) {
        this.identifier = identifier;
        return this;
    }
    
    public DicomZipImporter setNamer(DicomFileNamer namer) {
        this.namer = namer;
        return this;
    }
}
