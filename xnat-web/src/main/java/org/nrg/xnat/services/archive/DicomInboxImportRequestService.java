package org.nrg.xnat.services.archive;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;

import java.util.List;

public interface DicomInboxImportRequestService extends BaseHibernateService<DicomInboxImportRequest> {
    List<DicomInboxImportRequest> getOutstandingDicomInboxImportRequests();

    DicomInboxImportRequest getDicomInboxImportRequest(final long id);

    void setStatus(DicomInboxImportRequest request, DicomInboxImportRequest.Status status);

    void setToImporting(final DicomInboxImportRequest request);

    void setToAccepted(final DicomInboxImportRequest request);

    void setToProcessed(DicomInboxImportRequest request);

    void setToCompleted(DicomInboxImportRequest request);
}
