/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveMoveHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Handles("Move")
public class PrearchiveMoveHandler extends AbstractPrearchiveOperationHandler {

    public PrearchiveMoveHandler(final PrearchiveOperationRequest request) throws Exception {
        super(request);
    }

    @Override
    public void execute() throws Exception {
        if (!getParameters().containsKey("destination")) {
            throw new RuntimeException("A move prearchive session request for the session " + getSessionData().getName() + " in the project " + getSessionData().getProject() + " was received, but the destination project was not specified in the operation parameters!");
        }

        final String destination = getParameters().get(PrearchiveOperationRequest.PARAM_DESTINATION);
        final String session = getSessionData().getFolderName();
        if (!getSessionDir().getParentFile().exists()) {
            PrearcDatabase.unsafeSetStatus(session, getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(session, getSessionData().getTimestamp(), getSessionData().getProject());
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Moving session {} from project {}", session, getSessionData().getProject(), destination);
        }

        PrearcDatabase.moveToProject(session, getSessionData().getTimestamp(), getSessionData().getProject(), destination);

        final File movedFolder = PrearcUtils.getPrearcSessionDir(getUser(), destination, getSessionData().getTimestamp(), session, false);
        final File sessionXml = new File(movedFolder + ".xml");
        if (!sessionXml.exists()) {
            _log.warn("Tried to rebuild a session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            return;
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());
        }

        final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
        final String separatePetMr = PrearcUtils.getSeparatePetMr(destination);
        final String operation;
        if (bean instanceof XnatPetmrsessiondataBean) {
            switch (separatePetMr) {
                case "separate":
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found create separate PET and MR sessions setting for project {}, now working to separate that.", getSessionData().getProject());
                    }
                    operation = "Separate";
                    break;
                case "pet":
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
                    }
                    operation = "Rebuild";
                    break;
                default:
                    operation = "";
                    break;
            }
        } else if (bean instanceof XnatPetsessiondataBean && separatePetMr.equals("petmr")) {
            _log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
            operation = "Rebuild";
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
            }
            operation = "";
        }
        if (StringUtils.isNotBlank(operation)) {
            final SessionData moved = PrearcDatabase.getSession(session, getSessionData().getTimestamp(), destination);
            final PrearchiveOperationRequest request = new PrearchiveOperationRequest(getUser(), moved, movedFolder, operation);
            XDAT.sendJmsRequest(request);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PrearchiveMoveHandler.class);
}
