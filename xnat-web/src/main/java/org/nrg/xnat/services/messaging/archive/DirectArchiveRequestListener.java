package org.nrg.xnat.services.messaging.archive;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DirectArchiveRequestListener implements JmsRequestListener<DirectArchiveRequest> {

    @Autowired
    public DirectArchiveRequestListener(DirectArchiveSessionService directArchiveSessionService) {
        this.directArchiveSessionService = directArchiveSessionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "directArchiveRequest", destination = "directArchiveRequest")
    public void onRequest(final DirectArchiveRequest request) {
        log.info("Now handling request: {}", request);
        try {
            log.debug("Received request to direct archive {}", request.getId());
            directArchiveSessionService.build(request.getId());
            directArchiveSessionService.archive(request.getId());
        } catch (Exception e) {
            log.error("An error occurred during direct archive of {}", request.getId(), e);
        }
    }

    private final DirectArchiveSessionService directArchiveSessionService;
}
