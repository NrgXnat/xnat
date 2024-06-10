/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.archive;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Component
@Slf4j
public final class DicomInboxImportRequestListener implements JmsRequestListener<DicomInboxImportRequest> {
    @Autowired
    public DicomInboxImportRequestListener(final DicomInboxImportRequestService service,
                                           final Map<String, DicomObjectIdentifier<XnatProjectdata>> identifiers,
                                           final Map<String, DicomFileNamer> namers) {
        _service     = service;
        _identifiers = identifiers;
        _namers      = namers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "dicomInboxImportRequest", destination = "dicomInboxImportRequest")
    public void onRequest(final DicomInboxImportRequest request) {
        log.info("Now handling request: {}", request);
        final String username    = request.getUsername();
        final String sessionPath = request.getSessionPath();

        // Check for debug because it's not cheap to convert parameter map to string.
        if (log.isDebugEnabled()) {
            log.debug("Processing inbox import request for path {} requested by user {} with parameters {}", sessionPath, username, request.getParameters());
        } else {
            log.info("Processing inbox import request for path {} requested by user {}", sessionPath, username);
        }

        try {
            final UserI user = Users.getUser(username);
            _service.setToAccepted(request);

            final Map<String, Object> parameters = request.getObjectParametersMap();

            final DicomObjectIdentifier<XnatProjectdata> identifier;
            if (parameters.containsKey("identifier")) {
                final String key = String.valueOf(parameters.get("identifier"));
                if (!_identifiers.containsKey(key)) {
                    _service.fail(request, "Import operation for session " + sessionPath + " specified using the identifier \"" + key + "\", but there's no identifier with that key.");
                }
                identifier = _identifiers.get(key);
            } else {
                identifier = _identifiers.get("dicomObjectIdentifier");
            }

            final DicomFileNamer namer;
            if (parameters.containsKey("namer")) {
                final String key = String.valueOf(parameters.get("namer"));
                if (!_namers.containsKey(key)) {
                    _service.fail(request, "Import operation for session " + sessionPath + " specified using the namer \"" + key + "\", but there's no namer with that key.");
                }
                namer = _namers.get(key);
            } else {
                namer = _namers.get("dicomFileNamer");
            }

            try (final DicomInboxImportRequestImporter importer = new DicomInboxImportRequestImporter(user, _service, request, identifier, namer)) {
                final List<String> uris = importer.call();
                if (log.isDebugEnabled()) {
                    final StringBuilder message = new StringBuilder("Processed ").append(uris.size()).append(" URIs:\n");
                    for (final String uri : uris) {
                        message.append(" * ").append(uri).append("\n");
                    }
                    log.debug(message.toString());
                }

                for (final String uri : uris) {
                    final Map<String, Object> properties = PrearcUtils.parseURI(uri);
                    final String              project    = (String) properties.get(URIManager.PROJECT_ID);
                    final String              timestamp  = (String) properties.get(PrearcUtils.PREARC_TIMESTAMP);
                    final String              session    = (String) properties.get(PrearcUtils.PREARC_SESSION_FOLDER);

                    final Map<String, Object> rebuildParameters = new HashMap<>(parameters);
                    rebuildParameters.put(URIManager.PROJECT_ID, project);
                    rebuildParameters.put(PrearcUtils.PREARC_TIMESTAMP, timestamp);
                    rebuildParameters.put(PrearcUtils.PREARC_SESSION_FOLDER, session);
                    rebuildParameters.put(DicomInboxImportRequest.IMPORT_REQUEST_ID, request.getId());

                    PrearcUtils.queuePrearchiveOperation(new PrearchiveOperationRequest(user, Operation.Rebuild, new PrearcSession(project, timestamp, session, rebuildParameters, user)));
                }
            }
        } catch (FileNotFoundException e) {
            // This is a little weird: in the importer class, if the specified session path can't be found, it throws a
            // FNF exception with the bad path. If it finds the path but the path indicates something that's not a
            // folder, it throws FNF exception with no value set.
            final String path = e.getMessage();
            if (StringUtils.isBlank(path)) {
                _service.fail(request, "The location specified must be a folder but isn't: " + sessionPath);
            } else {
                _service.fail(request, "No session folder was found at the specified location :" + sessionPath);
            }
        } catch (UserInitException e) {
            _service.fail(request, "An error occurred trying to initialize the user for " + username + " while trying to import the session located at: " + sessionPath + "\n" + e.getMessage());
        } catch (UserNotFoundException e) {
            _service.fail(request, "Couldn't find a user by the name of " + username + " while trying to import the session located at: " + sessionPath + "\n" + e.getMessage());
        } catch (Exception e) {
            final String exception = e.getClass().getSimpleName();
            _service.fail(request, "The request to import data located at " + sessionPath + " failed due to an exception of type " + exception + ":\n" + e.getMessage());
        }
    }

    private class DicomInboxImportRequestImporter extends ImporterHandlerA implements FileVisitor<Path> {
        DicomInboxImportRequestImporter(final UserI user, final DicomInboxImportRequestService service, final DicomInboxImportRequest request, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer) throws FileNotFoundException {
            super(null, user);
            _dicomFiles = 0;
            _service    = service;
            _request    = request;
            _user       = user;
            _parameters = request.getObjectParametersMap();

            setIdentifier(identifier);
            setNamer(namer);

            _sessionPath = Paths.get(request.getSessionPath()).toFile();

            if (!_sessionPath.exists()) {
                throw new FileNotFoundException(request.getSessionPath());
            }
            if (!_sessionPath.isDirectory()) {
                throw new FileNotFoundException();
            }
        }

        /**
         * Processes the folder specified by the session or path parameter, importing all the files located in the
         * folder and its subfolders.
         *
         * @return A list of all the files that were imported into XNAT.
         */
        @Override
        public List<String> call() {
            _service.setToImporting(_request);
            try {
                Files.walkFileTree(_sessionPath.toPath(), WALKER_OPTIONS, Integer.MAX_VALUE, this);
                if (_dicomFiles == 0) {
                    _service.fail(_request, "No valid DICOM files found for the specified session :" + _request.getSessionPath());
                } else {
                    log.info("Completed processing the inbox session located at {}, with a total of {} folders and {} files found.", _sessionPath.getAbsolutePath(), _folderUris.size(), _fileUris.size());
                    _service.setToProcessed(_request);
                    if (_request.getCleanupAfterImport() && isInboxDirectory()) {
                        log.debug("Cleaning up inbox files after import - " + _sessionPath.getAbsolutePath());
                        FileUtils.deleteDirQuietly(_sessionPath);
                    }
                }
            } catch (IOException e) {
                _service.fail(_request, "An error occurred reading data while processing the session located at {}:\n{}", _request.getSessionPath(), e.getMessage());
            }
            return new ArrayList<>(_fileUris);
        }

        private boolean isInboxDirectory() {
            final String inboxPath = XDAT.getSiteConfigPreferences().getInboxPath();
            if (inboxPath == null || inboxPath.length() < 1) {
                return false;
            }
            final File inboxFile = new File(inboxPath);
            if (!inboxFile.exists() && inboxFile.isDirectory()) {
                return false;
            }
            File parentFile = _sessionPath.getParentFile();
            while (parentFile != null) {
                if (parentFile.equals(inboxFile)) {
                    return true;
                }
                parentFile = parentFile.getParentFile();
            }
            log.warn("Inbox cleanup requested, but file location is not under the configured inbox (" + inboxPath + ").  Files not removed.");
            return false;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path folder, final BasicFileAttributes attributes) {
            Objects.requireNonNull(folder);
            Objects.requireNonNull(attributes);
            final String path = folder.toString();
            _folderUris.add(path);
            log.info("Visiting the folder {} while processing the inbox session located at {}", path, _sessionPath.getAbsolutePath());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
            try {
                final GradualDicomImporter importer = new GradualDicomImporter(null, _user, new StoredFile(file.toFile(), false), _parameters);
                importer.setIdentifier(getIdentifier());
                if (null != getNamer()) {
                    importer.setNamer(getNamer());
                }
                _fileUris.addAll(importer.call());
                _dicomFiles++;
            } catch (ClientException | ServerException e) {
                log.warn("An error occurred importing the file {} while processing the inbox session located at {}", file, _sessionPath.getAbsolutePath(), e);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exception) {
            log.warn("An error occurred importing the file {} while processing the inbox session located at {}", file.toString(), _sessionPath.getAbsolutePath(), exception);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path folder, final IOException exception) {
            log.info("Finished visiting the folder {} while processing the inbox session located at {}", folder.toString(), _sessionPath.getAbsolutePath());
            return FileVisitResult.CONTINUE;
        }

        private final Set<FileVisitOption> WALKER_OPTIONS = ImmutableSet.of(FileVisitOption.FOLLOW_LINKS);

        private final Set<String> _folderUris = new LinkedHashSet<>();
        private final Set<String> _fileUris   = new LinkedHashSet<>();

        private final DicomInboxImportRequestService _service;
        private final DicomInboxImportRequest        _request;
        private final UserI                          _user;
        private final Map<String, Object>            _parameters;
        private final File                           _sessionPath;
    }

    private final DicomInboxImportRequestService                      _service;
    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _identifiers;
    private final Map<String, DicomFileNamer>                         _namers;
    private       int                                                 _dicomFiles;
}
