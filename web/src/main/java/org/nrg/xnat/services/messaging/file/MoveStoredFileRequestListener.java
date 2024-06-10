/*
 * web: org.nrg.xnat.services.messaging.file.MoveStoredFileRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.file;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

import static lombok.AccessLevel.PRIVATE;

@Component
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class MoveStoredFileRequestListener implements JmsRequestListener<MoveStoredFileRequest> {
    @Autowired
    public MoveStoredFileRequestListener(final UserProjectCache cache) {
        _cache = cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "moveStoredFileRequest", destination = "moveStoredFileRequest")
    public void onRequest(final MoveStoredFileRequest request) {
        log.info("Now handling request: {}", request);
        boolean            success    = true;
        final List<String> duplicates = new ArrayList<>();

        final PersistentWorkflowI wrk = WorkflowUtils.getUniqueWorkflow(request.getUser(), request.getWorkflowId());
        assert wrk != null;
        wrk.setStatus(PersistentWorkflowUtils.IN_PROGRESS);

        try {
            duplicates.addAll(request.getResourceModifier().addFile(request.getWriters(),
                                                                    request.getResourceIdentifier(),
                                                                    request.getType(),
                                                                    request.getFilePath(),
                                                                    request.getResourceInfo(),
                                                                    request.isExtract()));
        } catch (Exception e) {
            log.error("Unable to perform move operation on file.", e);
            success = false;
        }

        if (success) {
            try {
                final String projectId = request.getProject();
                if (StringUtils.isNotBlank(projectId)) {
                    getCache().clearProjectCacheEntry(projectId);
                    XDAT.triggerXftItemEvent(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, XftItemEventI.UPDATE);
                }
                WorkflowUtils.complete(wrk, wrk.buildEvent());
            } catch (Exception e) {
                log.error("Could not mark workflow " + wrk.getWorkflowId() + " complete.", e);
                success = false;
            }
        }

        if (success && request.isDelete()) {
            for (FileWriterWrapperI file : request.getWriters()) {
                file.delete();
            }
        }

        if (request.getNotifyList().length > 0) {
            final String body;
            final String subject;
            if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                if (success) {
                    subject = "Upload by reference complete";
                    final String duplicatesList;
                    if (!duplicates.isEmpty()) {
                        duplicatesList = "The following files were not uploaded because they already exist on the server:<br>\n" + String.join("<br>\n", duplicates);
                    } else {
                        duplicatesList = "";
                    }
                    body = XDAT.getNotificationsPreferences().getEmailMessageUploadByReferenceSuccess()
                               .replaceAll("USER_USERNAME", wrk.getUsername())
                               .replaceAll("DUPLICATES_LIST", duplicatesList);
                } else {
                    subject = "Upload by reference error";
                    body    = XDAT.getNotificationsPreferences().getEmailMessageUploadByReferenceFailure().replaceAll("USER_USERNAME", wrk.getUsername());
                }

                try {
                    XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), request.getNotifyList(), subject, body);
                } catch (MessagingException e) {
                    log.error("Failed to send email.", e);
                }
            }

        }
    }

    private final UserProjectCache _cache;
}
