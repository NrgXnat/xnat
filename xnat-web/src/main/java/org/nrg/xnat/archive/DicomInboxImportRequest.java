/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.ImmutableSet;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@ImporterHandler(handler = ImporterHandlerA.DICOM_INBOX_IMPORTER, allowCallsWithoutFiles = true)
public final class DicomInboxImportRequest extends ImporterHandlerA implements FileVisitor<Path> {
    public DicomInboxImportRequest(final Object listener, final UserI user, @SuppressWarnings("unused") final FileWriterWrapperI writer, final Map<String, Object> parameters) throws ClientException, ConfigServiceException {
        super(listener, user);

        final boolean hasSessionParameter = parameters.containsKey("session");
        final boolean hasPathParameter    = parameters.containsKey("path");

        // == here functions as XOR: if both are false or true (i.e. ==), XOR is false.
        if (hasSessionParameter == hasPathParameter) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                                      "You must specify *either* the session parameter with a label for processing a session folder from the configured " +
                                      "Inbox location *or* the path parameter specifying a full path to the session data to be imported.");
        }

        final String parameter = String.valueOf(parameters.get(hasSessionParameter ? "session" : "path"));
        _sessionPath = (hasSessionParameter ? Paths.get(XDAT.getSiteConfigurationProperty("inboxPath"), parameter) : Paths.get(parameter)).toFile();

        if (!_sessionPath.exists()) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "No session folder was found at the specified " + (hasSessionParameter ? "inbox location " : "path: ") + parameter);
        }
        if (!_sessionPath.isDirectory()) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "The location specified by the specified " + (hasSessionParameter ? "inbox location" : "path") + " must be a folder: " + parameter);
        }

        _listener = listener;
        _user = user;
        _parameters = parameters;
    }

    /**
     * Processes the folder specified by the session or path parameter, importing all of the files located in the folder and its subfolders.
     *
     * @return A list of all of the files that were imported into XNAT.
     */
    @Override
    public List<String> call() throws ServerException {
        try {
            Files.walkFileTree(_sessionPath.toPath(), WALKER_OPTIONS, Integer.MAX_VALUE, this);
        } catch (IOException e) {
            throw new ServerException("unable to read data from zip file", e);
        }
        _log.info("Completed processing the inbox session located at {}, with a total of {} folders and {} files found.", _sessionPath.getAbsolutePath(), _folderUris.size(), _fileUris.size());
        return new ArrayList<>(_fileUris);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path folder, final BasicFileAttributes attributes) {
        Objects.requireNonNull(folder);
        Objects.requireNonNull(attributes);
        final String path = folder.toString();
        _folderUris.add(path);
        _log.info("Visiting the folder {} while processing the inbox session located at {}", path, _sessionPath.getAbsolutePath());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        try {
            final GradualDicomImporter importer = new GradualDicomImporter(_listener, _user, new StoredFile(file.toFile(), false), _parameters);
            importer.setIdentifier(getIdentifier());
            if (null != getNamer()) {
                importer.setNamer(getNamer());
            }
            _fileUris.addAll(importer.call());
        } catch (ClientException | ServerException e) {
            _log.warn("An error occurred importing the file " + file.toString() + " while processing the inbox session located at " + _sessionPath.getAbsolutePath(), e);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exception) {
        _log.warn("An error occurred importing the file " + file.toString() + " while processing the inbox session located at " + _sessionPath.getAbsolutePath(), exception);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path folder, final IOException exception) {
        _log.info("Finished visiting the folder {} while processing the inbox session located at {}", folder.toString(), _sessionPath.getAbsolutePath());
        return FileVisitResult.CONTINUE;
    }

    private static final Set<FileVisitOption> WALKER_OPTIONS = ImmutableSet.of(FileVisitOption.FOLLOW_LINKS);

    private static final Logger _log = LoggerFactory.getLogger(DicomInboxImportRequest.class);

    private final Set<String> _folderUris = new LinkedHashSet<>();
    private final Set<String> _fileUris   = new LinkedHashSet<>();

    private final Object              _listener;
    private final UserI               _user;
    private final Map<String, Object> _parameters;
    private final File                _sessionPath;
}
