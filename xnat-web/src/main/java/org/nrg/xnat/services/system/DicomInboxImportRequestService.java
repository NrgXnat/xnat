package org.nrg.xnat.services.system;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.entities.DicomInboxImportRequest;

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
