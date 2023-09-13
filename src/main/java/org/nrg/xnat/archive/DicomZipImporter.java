/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.dcm4che2.io.DicomCodingException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.helpers.ArchiveEntryFileWriterWrapper;
import org.nrg.xnat.helpers.TarEntryFileWriterWrapper;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandlerResolver;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nrg.xnat.archive.Operation.Rebuild;

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
        this.fw = fw;
        this.in = fw.getInputStream();
        this.format = Format.getFormat(fw.getName());
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.actions.importer.ImporterHandlerA#call()
     */
    @Override
    public List<String> call() throws ClientException, ServerException {
        ClientException nonDcmException = null;
        boolean ignoreUnparsable = PrearcUtils.parseParam(params, IGNORE_UNPARSABLE_PARAM, false);
        final Set<String> uris = Sets.newLinkedHashSet();
        this.processing("Importing sessions to the prearchive");
        this.processing("Importing file ("+fw.getName()+" )");
        try {
            switch (format) {
                case ZIP:
                    try (final ZipInputStream zin = new ZipInputStream(in)) {
                        ZipEntry ze;
                        while (null != (ze = zin.getNextEntry())) {
                            if (!ze.isDirectory()) {
                                try {
                                    importEntry(new ZipEntryFileWriterWrapper(ze, zin), uris);
                                } catch (ClientException e) {
                                    if (ignoreUnparsable && e.getCause() instanceof DicomCodingException) {
                                        nonDcmException = e;
                                    } else {
                                        throw e;
                                    }
                                }
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
                                try {
                                    importEntry(new TarEntryFileWriterWrapper(ze, zin), uris);
                                } catch (ClientException e) {
                                    if (ignoreUnparsable && e.getCause() instanceof DicomCodingException) {
                                        nonDcmException = e;
                                    } else {
                                        throw e;
                                    }
                                }
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

        if (uris.isEmpty() && nonDcmException != null) {
            throw nonDcmException;
        }
        this.processing("Successfully uploaded "+uris.size()+"  sessions to the prearchive.");
        if (params.containsKey("action") && "commit".equals(params.get("action"))) {
            this.processing("Creating XML for DICOM sessions");
            try {
                Set<String> urls = xmlBuild(uris);
                if (isAutoArchive()) {
                    updateStatus(urls);
                    return new ArrayList<>(urls);
                } else {
                    updateStatus(uris);
                }
            } catch (ClientException e) {
                failed(e.getMessage(), true);
                throw e;
            }
        }
        return new ArrayList<>(uris);
    }

    private void updateStatus(Set<String> uris) {
        String message = "Prearchive:" + String.join(";", uris);
        if (isAutoArchive()) {
            message = "Archive:" + String.join(";", uris);
        }
        this.completed(message, true);
    }

    private Set<String> xmlBuild(Set<String> uris) throws ClientException {
        Set<String> archiveUrls = new HashSet<>();
        final boolean override = isBooleanParameter(PrearchiveOperationRequest.PARAM_OVERRIDE_EXCEPTIONS);
        final boolean appendMerge = isBooleanParameter(PrearchiveOperationRequest.PARAM_ALLOW_SESSION_MERGE);
        PrearchiveOperationHandlerResolver resolver = XDAT.getContextService().getBean(PrearchiveOperationHandlerResolver.class);
        for (String session : uris) {
            String[] elements = session.split("/");
            try {
                final SessionData  sessionData = PrearcDatabase.getSession(elements[5], elements[4], elements[3]);
                PrearchiveOperationRequest request = new PrearchiveOperationRequest(u, Rebuild, sessionData, new File(sessionData.getUrl()));
                PrearchiveRebuildHandler handler = (PrearchiveRebuildHandler) resolver.getHandler(request);
                handler.rebuild();
                if (isAutoArchive()) {
                    archiveSession(archiveUrls, override, appendMerge, request);
                }
            } catch (Exception e) {
                throw new ClientException("unable to archive for the Prearchive session", e);
            }
        }
        return archiveUrls;
    }

    private void archiveSession(Set<String> archiveUrls, boolean override, boolean appendMerge, PrearchiveOperationRequest request) throws Exception {
        PrearcSession prearcSession = new PrearcSession(request, this.u);
        if (PrearcDatabase.setStatus(prearcSession.getFolderName(), prearcSession.getTimestamp(), prearcSession.getProject(), PrearcUtils.PrearcStatus.ARCHIVING)) {
            final boolean append = appendMerge ? true : prearcSession.getSessionData() != null && prearcSession.getSessionData().getAutoArchive() != null && prearcSession.getSessionData().getAutoArchive() != PrearchiveCode.Manual;
            String url = PrearcDatabase.archive(listenerControl, prearcSession, override, append, prearcSession.isOverwriteFiles(), this.u, null);
            archiveUrls.add(url);
        } else {
            throw new ServerException("Unable to lock session for archiving.");
        }
    }

    private Boolean isBooleanParameter(final String key) {
        final Object value = params.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private boolean isAutoArchive() {
        if (params.containsKey("AA") && ("true".equalsIgnoreCase((String) params.get("AA")))) {
            return true;
        }
        if (params.containsKey("auto-archive") && ("true".equalsIgnoreCase((String) params.get("auto-archive")))) {
            return true;
        }
        return false;
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
    private final FileWriterWrapperI  fw;
    private static final String       IGNORE_UNPARSABLE_PARAM = "Ignore-Unparsable";
    private static final String       PREARCHIVE_CODE = "prearchive_code";
}
