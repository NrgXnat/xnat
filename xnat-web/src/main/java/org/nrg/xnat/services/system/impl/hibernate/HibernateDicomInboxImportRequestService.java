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
package org.nrg.xnat.services.system.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.daos.DicomInboxImportRequestDAO;
import org.nrg.xnat.entities.DicomInboxImportRequest;
import org.nrg.xnat.entities.DicomInboxImportRequest.Status;
import org.nrg.xnat.services.system.DicomInboxImportRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.nrg.xnat.entities.DicomInboxImportRequest.Status.*;

/**
 * {@inheritDoc}
 */
@Slf4j
@Transactional
@Service
public class HibernateDicomInboxImportRequestService extends AbstractHibernateEntityService<DicomInboxImportRequest, DicomInboxImportRequestDAO> implements DicomInboxImportRequestService {
    @Override
    public List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequests() {
        log.debug("Getting outstanding DICOM inbox import request");
        return getDao().findAllOutstandingDicomInboxImportRequests();
    }

    @Override
    public DicomInboxImportRequest getDicomInboxImportRequest(final long id) {
        return getDao().findById(id);
    }

    @Override
    public void setStatus(final DicomInboxImportRequest request, final Status status) {
        log.debug("Setting status of request {} to {}", request.getId(), status);
        request.setStatus(status);
        update(request);
    }

    @Override
    public void setToAccepted(final DicomInboxImportRequest request) {
        setStatus(request, Accepted);
    }

    @Override
    public void setToProcessed(final DicomInboxImportRequest request) {
        setStatus(request, Processed);
    }

    @Override
    public void setToImporting(final DicomInboxImportRequest request) {
        setStatus(request, Importing);
    }

    @Override
    public void setToCompleted(final DicomInboxImportRequest request) {
        setStatus(request, Completed);
    }
}
