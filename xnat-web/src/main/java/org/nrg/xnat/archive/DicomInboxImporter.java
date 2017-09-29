/*
 * web: org.nrg.xnat.archive.DicomZipImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.entities.DicomInboxImportRequest;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.system.DicomInboxImportRequestService;
import org.restlet.data.Status;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@ImporterHandler(handler = ImporterHandlerA.DICOM_INBOX_IMPORTER, allowCallsWithoutFiles = true)
public final class DicomInboxImporter extends ImporterHandlerA {
    public DicomInboxImporter(final Object listener, final UserI user, @SuppressWarnings("unused") final FileWriterWrapperI writer, final Map<String, Object> parameters) throws ClientException, ConfigServiceException {
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

        _service = XDAT.getContextService().getBean(DicomInboxImportRequestService.class);
        _jmsTemplate = XDAT.getContextService().getBean(JmsTemplate.class);
        _user = user;
        _parameters = parameters;
    }

    /**
     * Processes the folder specified by the session or path parameter, importing all of the files located in the folder
     * and its subfolders.
     *
     * @return A list of all of the files that were imported into XNAT.
     */
    @Override
    public List<String> call() {
        /*
        final DicomInboxImportRequest request = DicomInboxImportRequest.builder()
                                                                       .username(_user.getUsername())
                                                                       .sessionPath(_sessionPath.getAbsolutePath())
                                                                       .parameters(Maps.transformEntries(_parameters, TRANSFORMER))
                                                                       .build();
        */
        final DicomInboxImportRequest request = new DicomInboxImportRequest();
        request.setUsername(_user.getUsername());
        request.setSessionPath(_sessionPath.getAbsolutePath());
        request.setParameters(Maps.transformEntries(_parameters, TRANSFORMER));
        _service.create(request);

        XDAT.sendJmsRequest(_jmsTemplate, request);

        log.info("Created and queued import request {} for the inbox session located at {}.", request.getId(), _sessionPath.getAbsolutePath());
        try {
            return Collections.singletonList(new URL(XDAT.getSiteConfigurationProperty("siteUrl") + "/xapi/dicom/" + request.getId()).getPath());
        } catch (MalformedURLException | ConfigServiceException e) {
            log.error("An error occurred trying to retrieve the site URL when composing DICOM inbox import request response, panicking.", e);
            return Collections.singletonList("/xapi/dicom/" + request.getId());
        }
    }

    private static final Maps.EntryTransformer<String, Object, String> TRANSFORMER = new Maps.EntryTransformer<String, Object, String>() {
        @Override
        public String transformEntry(@Nullable final String key, @Nullable final Object value) {
            return value == null ? "" : value.toString();
        }
    };

    private final DicomInboxImportRequestService _service;
    private final JmsTemplate                    _jmsTemplate;
    private final UserI                          _user;
    private final Map<String, Object>            _parameters;
    private final File                           _sessionPath;
}
