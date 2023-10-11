/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.io.DicomCodingException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.helpers.ArchiveEntryFileWriterWrapper;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.TarEntryFileWriterWrapper;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandlerResolver;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.actions.SessionImporter;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nrg.xnat.archive.GradualDicomImporter.isAutoArchive;
import static org.nrg.xnat.archive.Operation.Rebuild;

@ImporterHandler(handler = ImporterHandlerA.DICOM_ZIP_IMPORTER)
public final class DicomZipImporter extends ImporterHandlerA {

    private static final String ACTION = "action";
    private static final String COMMIT = "commit";
    private static final String PROJECT = "project";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String SLASH = "/";
    private static final String FALSE = "false";

    public DicomZipImporter(final Object listenerControl,
                            final UserI u,
                            final FileWriterWrapperI fw,
                            final Map<String, Object> params) {
        super(listenerControl, u);
        this.listenerControl = getControlString();
        this.u = u;
        this.params = params;
        this.fw = fw;
        this.ignoreUnparsable = PrearcUtils.parseParam(params, IGNORE_UNPARSABLE_PARAM, false);
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.restlet.actions.importer.ImporterHandlerA#call()
     */
    @Override
    public List<String> call() throws ClientException, ServerException {
        final Set<String> uris = Sets.newLinkedHashSet();
        this.processing("Determining destination");
        try {
            determiningDestination();
        } catch (Exception e) {
            this.failed(e.getMessage(), true);
            throw new ClientException("Parameter dest format error", e);
        }
        if (this.params.containsKey(PROJECT)) {
            this.params.put(PROJECT_ID, this.params.get(PROJECT));
        }
        this.processing("Importing sessions to the prearchive");
        try {
            importCompressedFile(fw, uris);
        } catch (ServerException | ClientException e) {
            this.failed(e.getMessage(), true);
            throw e;
        }

        if (uris.isEmpty() && nonDcmException != null) {
            this.failed(nonDcmException.getMessage(), true);
            throw nonDcmException;
        }

        this.processing("Successfully uploaded " + uris.size() + "  sessions to the prearchive.");
        if (params.containsKey(ACTION) && COMMIT.equals(params.get(ACTION))) {
            this.processing("Creating XML for DICOM sessions");
            try {
                Set<String> urls = xmlBuild(uris);
                if (isAutoArchive(params)) {
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

    private void importCompressedFile(FileWriterWrapperI fw, Set<String> uris) throws ServerException, ClientException {
        this.processing("Importing file (" + fw.getName() + " )");
        Format format = Format.getFormat(fw.getName());
        try {
            switch (format) {
                case ZIP:
                    importZipFile(fw, uris);
                    break;
                case TAR:
                case TGZ:
                    importTgzFile(fw, uris);
                    break;
                default:
                    throw new ClientException("Unsupported format " + format);
            }
        } catch (IOException e) {
            throw new ClientException("unable to read data from file", e);
        }
    }

    private void importZipFile(FileWriterWrapperI fw, Set<String> uris) throws IOException, ServerException, ClientException {
        try (final ZipInputStream zin = new ZipInputStream(fw.getInputStream())) {
            ZipEntry ze;
            while (null != (ze = zin.getNextEntry())) {
                if (ze.isDirectory()) {
                    continue;
                }
                try {
                    if (Format.UNKNOWN != Format.getFormat(ze.getName())) {
                        importCompressedFile(new ZipEntryFileWriterWrapper(ze, zin), uris);
                        continue;
                    }
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

    private void importTgzFile(FileWriterWrapperI fw, Set<String> uris) throws IOException, ServerException, ClientException {
        InputStream is = new BufferedInputStream(fw.getInputStream());
        Format format = Format.getFormat(fw.getName());
        if (format == Format.TGZ) {
            is = new GZIPInputStream(is);
        }
        try (final TarArchiveInputStream zin = new TarArchiveInputStream(is)) {
            TarArchiveEntry ze;
            while (null != (ze = zin.getNextTarEntry())) {
                if (!ze.isDirectory()) {
                    try {
                        if (Format.UNKNOWN != Format.getFormat(ze.getName())) {
                            importCompressedFile(new TarEntryFileWriterWrapper(ze, zin), uris);
                            continue;
                        }
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
    }

    private void updateStatus(Set<String> uris) {
        String message = "Prearchive:" + String.join(";", uris);
        if (isAutoArchive(params)) {
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
            String[] elements = session.split(SLASH);
            try {
                final SessionData sessionData = PrearcDatabase.getSession(elements[5], elements[4], elements[3]);
                PrearchiveOperationRequest request = new PrearchiveOperationRequest(u, Rebuild, sessionData, new File(sessionData.getUrl()), populateAdditionalValues(params));
                PrearchiveRebuildHandler handler = (PrearchiveRebuildHandler) resolver.getHandler(request);
                handler.rebuild();
                if (isAutoArchive(params)) {
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

    private void importEntry(ArchiveEntryFileWriterWrapper entryFileWriter, Set<String> uris)
            throws ServerException, ClientException {
        final GradualDicomImporter importer = new GradualDicomImporter(listenerControl, u, entryFileWriter, params);
        importer.setIdentifier(getIdentifier());
        if (null != getNamer()) {
            importer.setNamer(getNamer());
        }
        uris.addAll(importer.call());
    }

    private void determiningDestination() throws MalformedURLException {
        String dest = (String) params.get(RequestUtil.DEST);
        final URIManager.DataURIA destination = (!StringUtils.isEmpty(dest)) ? UriParserUtils.parseURI(dest) : null;
        if (destination == null) {
            return;
        }

        params.put(ACTION, COMMIT);

        if (destination instanceof URIManager.ArchiveURI) {
            params.put(RequestUtil.AA, RequestUtil.TRUE);
            params.put(RequestUtil.AUTO_ARCHIVE, RequestUtil.TRUE);
        } else {
            params.put(RequestUtil.AA, FALSE);
            params.put(RequestUtil.AUTO_ARCHIVE, FALSE);
        }

        //check for existing session by URI
        params.putAll(destination.getProps());
        if (destination instanceof URIManager.PrearchiveURI) {
            return;
        }

        String exptId = (String) destination.getProps().get(URIManager.EXPT_ID);
        if (!StringUtils.isEmpty(exptId)) {
            XnatImagesessiondata experiment = SessionImporter.getExperimentByIdOrLabel(PrearcImporterHelper.identifyProject(destination.getProps()), exptId, u);
            if (experiment != null) {
                params.put(URIManager.EXPT_LABEL, experiment.getLabel());
            }
        }
    }

    private Map<String, Object> populateAdditionalValues(Map<String, Object> parameters) {
        Map<String, Object> additionalValues = new HashMap<>();
        if (parameters.containsKey(RequestUtil.OVERWRITE_FILES)) {
            additionalValues.put(RequestUtil.OVERWRITE_FILES, parameters.get(RequestUtil.OVERWRITE_FILES));
        }
        return additionalValues;
    }

    private final Object listenerControl;
    private final UserI u;
    private final Map<String, Object> params;
    private final FileWriterWrapperI fw;
    private final boolean ignoreUnparsable;
    private ClientException nonDcmException = null;
}
