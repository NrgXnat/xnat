package org.nrg.xnat.services.archive.impl.hibernate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * An inbox import request is looked up in a transaction of its own and used after it has ended: the status API
 * serializes it, and the prearchive operations move it on as they finish. Against the real ORM test database, since
 * a lazy proxy only fails once its session has closed.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DicomInboxImportRequestOrmTestConfig.class)
public class DicomInboxImportRequestLookupTest {
    @Autowired
    private DicomInboxImportRequestService service;

    @Test
    public void aRequestLookedUpCanBeReadAndMovedOnAfterItsTransaction() {
        final long id = service.create(DicomInboxImportRequest.builder().username("admin").sessionPath("/data/xnat/inbox/PROJ/session")
                                                              .parameters(Collections.singletonMap("PROJECT_ID", "PROJ")).build()).getId();

        final DicomInboxImportRequest request = service.getDicomInboxImportRequest(id);
        assertEquals(Status.Queued, request.getStatus());
        assertEquals("PROJ", request.getParameters().get("PROJECT_ID"));

        service.complete(request, "Archived");
        assertEquals(Status.Completed, service.getDicomInboxImportRequest(id).getStatus());
        assertEquals("Archived", service.getDicomInboxImportRequest(id).getResolution());
    }

    @Test
    public void aRequestThatDoesNotExistIsNull() {
        assertNull(service.getDicomInboxImportRequest(Long.MAX_VALUE));
    }
}
