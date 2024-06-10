/*
 * web: org.nrg.xnat.services.system.impl.hibernate.HibernateHostInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * 
 */
package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.*;

/**
 * {@inheritDoc}
 */
@Slf4j
@Transactional
@Service("dicomInboxImportRequestService")
public class HibernateDicomInboxImportRequestService extends AbstractHibernateEntityService<DicomInboxImportRequest, DicomInboxImportRequestDAO> implements DicomInboxImportRequestService {
    @Override
    public List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequests() {
        log.debug("Getting all outstanding DICOM inbox import requests");
        return getDao().findAllOutstandingDicomInboxImportRequests();
    }

    @Override
    public List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequestsForUser(final String username) {
        log.debug("Getting outstanding DICOM inbox import requests for user {}", username);
        return getDao().findAllOutstandingDicomInboxImportRequestsForUser(username);
    }

    @Override
    public List<DicomInboxImportRequest> getDicomInboxImportRequestsForUser(final String username) {
        log.debug("Getting all DICOM inbox import requests for user {}", username);
        return getDao().findAllDicomInboxImportRequestsForUser(username);
    }

    @Override
    public DicomInboxImportRequest getDicomInboxImportRequest(final long id) {
        return getDao().findById(id);
    }

    public void setStatus(final DicomInboxImportRequest request, final Status status) {
        log.debug("Setting status of request {} to {}", request.getId(), status);
        request.setStatus(status);
        update(request);
    }

    public void setToAccepted(final DicomInboxImportRequest request) {
        setStatus(request, Accepted);
    }

    public void setToProcessed(final DicomInboxImportRequest request) {
        setStatus(request, Processed);
    }

    public void setToImporting(final DicomInboxImportRequest request) {
        setStatus(request, Importing);
    }

    public void complete(final DicomInboxImportRequest request) {
        complete(request, null);
    }

    public void complete(final DicomInboxImportRequest request, final String message, final String... parameters) {
        request.setStatus(Completed);
        request.setResolution(message == null ? null : format(message, parameters));
        update(request);
    }

    public void fail(final DicomInboxImportRequest request, final String message, final String... parameters) {
        if (StringUtils.isBlank(message)) {
            throw new NrgServiceRuntimeException("No message set for failure of request, you must provide a reason for failures!");
        }
        request.setStatus(Failed);
        request.setResolution(format(message, parameters));
        update(request);
    }

    private static String format(final String message, final String... parameters) {
        return StringUtils.isBlank(message) ? "" : parameters.length == 0 ? message : MessageFormatter.arrayFormat(message, parameters).getMessage();
    }
}
