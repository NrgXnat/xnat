/*
 * org.nrg.xnat.helpers.prearchive.PrearcDatabase
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Created 3/12/14 1:49 PM
 */
package org.nrg.xnat.services.messaging.file;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.util.List;

public class MoveStoredFileRequestListener {

    @SuppressWarnings("unused")
    public void onRequest(final MoveStoredFileRequest request) throws Exception {
        boolean success = true;
        List<String> duplicates = null;

        PersistentWorkflowI wrk;
        wrk = WorkflowUtils.getUniqueWorkflow(request.getUser(), request.getWorkflowId());
        wrk.setStatus(PersistentWorkflowUtils.IN_PROGRESS);

        try {
            duplicates = request.getResourceModifier().addFile(request.getWriters(),
                    request.getResourceIdentifier(), request.getType(), request.getFilepath(),
                    request.getResourceInfo(), request.isExtract());
        } catch (Exception e) {
            log.error("Unable to perform move operation on file.", e);
            success = false;
        }

        if (success)
            try {
                WorkflowUtils.complete(wrk, wrk.buildEvent());
            } catch (Exception e) {
                log.error("Could not mark workflow " + wrk.getWorkflowId() + " complete.", e);
                success = false;
            }

        if (success && request.isDelete())
            for (FileWriterWrapperI file : request.getWriters()) {
                file.delete();
            }

        if (request.getNotifyList().length > 0) {
            String subject;
            String message;

            if (success) {
                subject = "Upload by reference complete";
                message = "<p>The upload by reference requested by " + wrk.getUsername() + " has finished successfully.</p>";
                if (duplicates.size() > 0) {
                    message += "<p>The following files were not uploaded because they already exist on the server:<br><ul>";
                    for (String duplicate : duplicates) {
                        message += "<li>" + duplicate + "</li>";
                    }
                    message += "</ul></p>";
                }
            }
            else {
                subject = "Upload by reference error";
                message = "<p>The upload by reference requested by " + request.getUser().getUsername() + " has encountered an error.</p>" +
                        "<p>Please contact your IT staff or the application logs for more information.</p>";
            }

            try {
                XDAT.getMailService().sendHtmlMessage(AdminUtils.getAdminEmailId(), request.getNotifyList(), subject, message);
            } catch (MessagingException e) {
                log.error("Failed to send email.", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MoveStoredFileRequestListener.class);
}
