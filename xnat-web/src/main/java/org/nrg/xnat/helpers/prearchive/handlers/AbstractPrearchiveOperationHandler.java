/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.AbstractPrearchiveOperationHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.event.archive.ArchiveEvent;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractPrearchiveOperationHandler implements PrearchiveOperationHandler {
    protected AbstractPrearchiveOperationHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider, final DicomInboxImportRequestService importRequestService) {
        _eventService = eventService;
        _userProvider = userProvider;
        _importRequestService = importRequestService;
        _username = request.getUsername();
        _sessionData = request.getSessionData();
        _sessionDir = request.getSessionDir();
        _parameters = new HashMap<>(ObjectUtils.defaultIfNull(request.getParameters(), Collections.<String, Object>emptyMap()));
        _operation = getConfiguredOperation();
        _requestId = (request.getParameters() != null && request.getParameters().containsKey(DicomInboxImportRequest.IMPORT_REQUEST_ID)) ? (Long) request.getParameters().get(DicomInboxImportRequest.IMPORT_REQUEST_ID) : -1;
        _listenerId = request.getListenerId();
    }

    /**
     * {@inheritDoc}
     */
    public abstract void execute() throws Exception;

    @JsonIgnore
    protected UserI getUser() {
        if (_user == null) {
            if (StringUtils.isNotBlank(_username)) {
                try {
                    _user = Users.getUser(_username);
                } catch (UserInitException | UserNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                _user = _userProvider.get();
            }
        }
        return _user;
    }

    protected void progress(final int progress) {
        progress(progress, null);
    }

    protected void progress(final int progress, final String message) {
        getEventService().triggerEvent(ArchiveEvent.progress(_user.getID(), _operation, progress, _sessionData, _listenerId, message));
        if (_requestId > 0) {
            final DicomInboxImportRequest request = _importRequestService.getDicomInboxImportRequest(_requestId);
            if (request != null) {
                _importRequestService.setToImporting(request);
            }
        }
    }

    protected void completed() {
        completed(null);
    }

    protected void completed(final String message) {
        getEventService().triggerEvent(ArchiveEvent.completed(_user.getID(), _operation, _sessionData, _listenerId, message));
        if (_requestId > 0) {
            final DicomInboxImportRequest request = _importRequestService.getDicomInboxImportRequest(_requestId);
            if (request != null) {
                _importRequestService.complete(request, message);
            }
        }
    }

    protected void failed() {
        failed(null);
    }

    protected void failed(final String message) {
        getEventService().triggerEvent(ArchiveEvent.failed(_user.getID(), _operation, _sessionData, _listenerId, message));
        if (_requestId > 0) {
            final DicomInboxImportRequest request = _importRequestService.getDicomInboxImportRequest(_requestId);
            if (request != null) {
                _importRequestService.fail(request, message);
            }
        }
    }

    /**
     * Allows users to pass XML paths as parameters.  The values supplied are copied into the loaded session.
     *
     * @param folder The folder containing the prearchive session XML.
     *
     * @throws IOException              When an error occurs reading or writing data.
     * @throws SAXException             When an error occurs parsing the session XML.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException   When a specified field isn't found on the object.
     * @throws InvalidValueException    When an invalid value is specified for item properties.
     */
    protected void populateAdditionalFields(final File folder) throws IOException, SAXException, ElementNotFoundException, FieldNotFoundException, InvalidValueException {
        //prepare params by removing non xml path names
        final Map<String, Object> cleaned = XMLPathShortcuts.identifyUsableFields(getParameters(), XMLPathShortcuts.EXPERIMENT_DATA, false);

        if (!cleaned.isEmpty()) {
            final SAXReader reader = new SAXReader(getUser());
            final File      xml    = folder.getParentFile().toPath().resolve(folder.getName() + ".xml").toFile();
            final XFTItem   item   = reader.parse(xml.getAbsolutePath());
            item.setProperties(cleaned, true);
            try (final FileWriter writer = new FileWriter(xml)) {
                item.toXML(writer, false);
            }
        }
    }

    private Operation getConfiguredOperation() {
        final Handles annotation = getClass().getAnnotation(Handles.class);
        if (annotation == null) {
            log.warn("The class {} extends AbstractPrearchiveOperationHandler, but doesn't include the @Handles annotation.", getClass().getName());
            return null;
        }
        return annotation.value();
    }

    @Getter(PRIVATE)
    private final NrgEventServiceI _eventService;
    @Getter(PRIVATE)
    private final XnatUserProvider _userProvider;

    private final String                         _username;
    private final SessionData                    _sessionData;
    private final File                           _sessionDir;
    private final Map<String, Object>            _parameters;
    private final Operation                      _operation;
    private final DicomInboxImportRequestService _importRequestService;
    private final long                           _requestId;
    private final String                         _listenerId;

    private UserI _user;
}
