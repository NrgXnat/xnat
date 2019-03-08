package org.nrg.xnat.services.archive;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status;

import java.util.List;

public interface DicomInboxImportRequestService extends BaseHibernateService<DicomInboxImportRequest> {
    List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequests();

    List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequestsForUser(String username);

    DicomInboxImportRequest getDicomInboxImportRequest(final long id);

    void setStatus(final DicomInboxImportRequest request, final Status status);

    void setToImporting(final DicomInboxImportRequest request);

    void setToAccepted(final DicomInboxImportRequest request);

    void setToProcessed(final DicomInboxImportRequest request);

    void complete(final DicomInboxImportRequest request);

    void complete(final DicomInboxImportRequest request, final String message, final String... parameters);

    void fail(final DicomInboxImportRequest request, final String message, final String... parameters);
}
