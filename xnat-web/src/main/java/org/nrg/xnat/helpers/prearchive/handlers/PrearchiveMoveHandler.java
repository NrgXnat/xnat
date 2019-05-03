/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveMoveHandler
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
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import java.io.File;

import static org.nrg.xnat.archive.Operation.*;
import static org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest.PARAM_DESTINATION;

@Handles(Move)
@Slf4j
public class PrearchiveMoveHandler extends AbstractPrearchiveOperationHandler {
    public PrearchiveMoveHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider) {
        super(request, eventService, userProvider);
    }

    @Override
    public void execute() throws Exception {
        if (!getParameters().containsKey(PARAM_DESTINATION)) {
            throw new RuntimeException("A move prearchive session request for the session " + getSessionData().getName() + " in the project " + getSessionData().getProject() + " was received, but the destination project was not specified in the operation parameters!");
        }

        final String destination = (String) getParameters().get(PARAM_DESTINATION);
        final String session = getSessionData().getFolderName();
        if (!getSessionDir().getParentFile().exists()) {
            PrearcDatabase.unsafeSetStatus(session, getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(session, getSessionData().getTimestamp(), getSessionData().getProject());
        }

        log.debug("Moving session {} from project {} to {}", session, getSessionData().getProject(), destination);

        PrearcDatabase.moveToProject(session, getSessionData().getTimestamp(), getSessionData().getProject(), destination);

        final UserI user        = getUser();
        final File  movedFolder = PrearcUtils.getPrearcSessionDir(user, destination, getSessionData().getTimestamp(), session, false);
        final File  sessionXml  = new File(movedFolder + ".xml");
        if (!sessionXml.exists()) {
            log.warn("Tried to rebuild a session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            return;
        }

        log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());

        final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
        final String                   separatePetMr = PrearcUtils.getSeparatePetMr(destination);
        final Operation operation;
        if (bean instanceof XnatPetmrsessiondataBean) {
            switch (separatePetMr) {
                case "separate":
                    log.debug("Found create separate PET and MR sessions setting for project {}, now working to separate that.", getSessionData().getProject());
                    operation = Separate;
                    break;
                case "pet":
                    log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
                    operation = Rebuild;
                    break;
                default:
                    log.debug("Found a PET/MR session XML in the file {} but the separate PET/MR flag set to false or not set for the site or project. No more to be done.", sessionXml.getAbsolutePath());
                    return;
            }
        } else if (bean instanceof XnatPetsessiondataBean && separatePetMr.equals("petmr")) {
            log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
            operation = Rebuild;
        } else {
            log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
            return;
        }

        XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, operation, PrearcDatabase.getSession(session, getSessionData().getTimestamp(), destination), movedFolder));
    }
}
