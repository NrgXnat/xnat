package org.nrg.xnat.services.archive.impl.hibernate;

import org.junit.Before;
import org.junit.Test;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * An inbox import request's resolution column holds 255 characters. A reason that names file paths runs longer, and
 * an update that failed on it would leave the request where it was, importing, however the import had ended.
 */
public class HibernateDicomInboxImportRequestServiceTest {
    private DicomInboxImportRequestDAO              dao;
    private HibernateDicomInboxImportRequestService service;
    private DicomInboxImportRequest                 request;

    @Before
    public void setUp() {
        dao     = mock(DicomInboxImportRequestDAO.class);
        service = new HibernateDicomInboxImportRequestService();
        service.setDao(dao);
        request = new DicomInboxImportRequest();
    }

    @Test
    public void aFailureTooLongForItsColumnKeepsItsStart() {
        service.fail(request, "Stopped at {}: {}", "object.dcm", "x".repeat(400));

        assertEquals(Status.Failed, request.getStatus());
        assertEquals(255, request.getResolution().length());
        assertTrue(request.getResolution().startsWith("Stopped at object.dcm: xxx"));
        verify(dao).update(request);
    }

    @Test
    public void aCompletionTooLongForItsColumnKeepsItsStart() {
        service.complete(request, "x".repeat(400));

        assertEquals(Status.Completed, request.getStatus());
        assertEquals(255, request.getResolution().length());
    }

    @Test
    public void aShortReasonIsKeptWhole() {
        service.fail(request, "No valid DICOM files found for the specified session :{}", "/data/xnat/inbox/PROJ/session");

        assertEquals("No valid DICOM files found for the specified session :/data/xnat/inbox/PROJ/session", request.getResolution());
    }
}
