/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler
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

import static org.nrg.xnat.archive.Operation.*;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.*;

@Handles(Rebuild)
@Slf4j
public class PrearchiveRebuildHandler extends AbstractPrearchiveOperationHandler {
    public static final String PARAM_OVERRIDE_LOCK = "overrideLock";

    public PrearchiveRebuildHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider, final DicomInboxImportRequestService importRequestService) {
        super(request, eventService, userProvider, importRequestService);
    }

    @Override
    public void execute() {
        try {
            final PrearcUtils.PrearcStatus status    = getSessionData().getStatus();
            final boolean                  receiving = status != null && status.equals(RECEIVING);
            log.info("Received request to process prearchive session at: {}", getSessionData().getExternalUrl());

            final boolean overrideLock = getParameters().containsKey(PARAM_OVERRIDE_LOCK) && ((boolean) getParameters().get(PARAM_OVERRIDE_LOCK));
            final String  folderName   = getSessionData().getFolderName();
            final String  timestamp    = getSessionData().getTimestamp();
            final String  project      = getSessionData().getProject();
            if (status != QUEUED_BUILDING && !PrearcDatabase.setStatus(folderName, timestamp, project, QUEUED_BUILDING, overrideLock)) {
                log.warn("Tried to reset the status of the session {} to QUEUED_BUILDING, but failed. This usually means the session is locked and the override lock parameter was false. This might be OK: I checked whether the session was locked before trying to update the status but maybe a new file arrived in the intervening millisecond(s).", getSessionData());
                return;
            }
            if (!getSessionDir().getParentFile().exists()) {
                try {
                    log.warn("The parent of the indicated session {} could not be found at the indicated location {}", getSessionData().getName(), getSessionDir().getParentFile().getAbsolutePath());
                    PrearcDatabase.unsafeSetStatus(folderName, timestamp, project, _DELETING);
                    PrearcDatabase.deleteCacheRow(folderName, timestamp, project);
                } catch (Exception e) {
                    log.error("An error occurred attempting to clear the prearchive entry for the session " + getSessionData().getName() + ", which doesn't exist at the indicated location " + getSessionDir().getParentFile().getAbsolutePath());
                }
            } else if (PrearcDatabase.setStatus(folderName, timestamp, project, BUILDING)) {
                PrearcDatabase.buildSession(getSessionDir(), folderName, timestamp, project, getSessionData().getVisit(), getSessionData().getProtocol(), getSessionData().getTimeZone(), getSessionData().getSource());
                populateAdditionalFields(getSessionDir());

                // We need to check whether the session was updated to RECEIVING_INTERRUPT while the rebuild operation
                // was happening. If that happened, that means more data started to arrive during the rebuild. If not,
                // we'll proceed down the path where we check for session splits and autoarchive. If so, we'll just
                // reset the status to RECEIVING and update the session timestamp.
                final SessionData current = PrearcDatabase.getSession(getSessionData().getSessionDataTriple());
                if (current.getStatus() != RECEIVING_INTERRUPT) {
                    if (!handleSeparablePetMrSession(folderName, timestamp, project)) {
                        PrearcUtils.resetStatus(getUser(), project, timestamp, folderName, true);

                        // we don't want to autoarchive a session that's just being rebuilt
                        // but we still want to autoarchive sessions that just came from RECEIVING STATE
                        final PrearcSession session = new PrearcSession(project, timestamp, folderName, null, getUser());
                        if (receiving && session.isAutoArchive()) {
                            XDAT.sendJmsRequest(new PrearchiveOperationRequest(getUser().getUsername(), Archive, getSessionData(), getSessionDir(), getParameters()));
                        }
                    }
                } else {
                    log.info("Found session {} in RECEIVING_INTERRUPT state, meaning that data began arriving while session was in an interruptible non-receiving state. No session split or autoarchive checks will be performed and session will be restored to RECEIVING state.", getSessionData().getSessionDataTriple());
                    PrearcDatabase.setStatus(folderName, timestamp, project, RECEIVING);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean handleSeparablePetMrSession(final String folderName, final String timestamp, final String project) throws Exception {
        final boolean separatePetMr = PrearcUtils.isUnassigned(getSessionData()) ? PrearcUtils.shouldSeparatePetMr() : PrearcUtils.shouldSeparatePetMr(project);
        if (!separatePetMr) {
            return false;
        }

        log.debug("Found create separate PET and MR sessions setting for project {}, now working to separate that.", project);
        final File sessionXml = new File(getSessionDir() + ".xml");
        if (!sessionXml.exists()) {
            log.warn("Tried to rebuild a session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            return false;
        }

        log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());
        final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
        if (!(bean instanceof XnatPetmrsessiondataBean)) {
            log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
            return false;
        }

        log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
        PrearcUtils.resetStatus(getUser(), project, timestamp, folderName, true);
        XDAT.sendJmsRequest(new PrearchiveOperationRequest(getUser(), Separate, getSessionData(), getSessionDir(), getParameters()));
        return true;
    }
}
