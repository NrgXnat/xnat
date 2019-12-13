/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveSeparatePetMrHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import java.io.File;
import java.util.Map;

import static org.nrg.xnat.archive.Operation.Archive;
import static org.nrg.xnat.archive.Operation.Separate;

@Handles(Separate)
@Slf4j
public class PrearchiveSeparatePetMrHandler extends AbstractPrearchiveOperationHandler {
    public PrearchiveSeparatePetMrHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider, final DicomInboxImportRequestService importRequestService) {
        super(request, eventService, userProvider, importRequestService);
    }

    @Override
    public void execute() throws Exception {
        final boolean receiving = getSessionData().getStatus() != null && getSessionData().getStatus().equals(PrearcUtils.PrearcStatus.RECEIVING);
        if (!getSessionDir().getParentFile().exists()) {
            try {
                log.info("The parent of the indicated session {} could not be found at the indicated location {}", getSessionData().getName(), getSessionDir().getParentFile().getAbsolutePath());
                PrearcDatabase.unsafeSetStatus(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus._DELETING);
                PrearcDatabase.deleteCacheRow(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject());
            } catch (Exception e) {
                log.error("An error occurred attempting to clear the prearchive entry for the session {}, which doesn't exist at the indicated location {}", getSessionData().getName(), getSessionDir().getParentFile().getAbsolutePath(), e);
            }
        } else if (PrearcDatabase.setStatus(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus.SEPARATING)) {
            final File sessionXml = new File(getSessionDir() + ".xml");
            if (sessionXml.exists()) {
                log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());
                final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
                if (bean instanceof XnatPetmrsessiondataBean) {
                    log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
                    final Map<String, SessionData> sessions = PrearcDatabase.separatePetMrSession(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), (XnatPetmrsessiondataBean) bean);
                    if (sessions == null) {
                        log.warn("No sessions returned from separate PET/MR session operation, check your logs for errors.");
                        return;
                    }
                    // Now finish the upload process, including checking for auto-archive.
                    for (final String modality : sessions.keySet()) {
                        final SessionData   sessionData = sessions.get(modality);
                        final PrearcSession session     = new PrearcSession(sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), null, getUser());
                        if (receiving && session.isAutoArchive()) {
                            final PrearchiveOperationRequest request = new PrearchiveOperationRequest(getUser(), Archive, session.getSessionData(), session.getSessionDir());
                            XDAT.sendJmsRequest(request);
                        }
                    }
                } else {
                    log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
                }
            } else {
                log.warn("Tried to separate a PET/MR session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            }
        }
    }
}
