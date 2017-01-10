/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveSeparatePetMrHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xnat.archive.FinishImageUpload;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.restlet.actions.PrearcImporterA;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

@Handles("Separate")
public class PrearchiveSeparatePetMrHandler extends AbstractPrearchiveOperationHandler {

    public PrearchiveSeparatePetMrHandler(final PrearchiveOperationRequest request) throws Exception {
        super(request);
    }

    @Override
    public void execute() throws Exception {
        final boolean receiving = getSessionData().getStatus() != null && getSessionData().getStatus().equals(PrearcUtils.PrearcStatus.RECEIVING);
        if (!getSessionDir().getParentFile().exists()) {
            try {
                if (_log.isInfoEnabled()) {
                    _log.info("The parent of the indicated session " + getSessionData().getName() + " could not be found at the indicated location " + getSessionDir().getParentFile().getAbsolutePath());
                }
                PrearcDatabase.unsafeSetStatus(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus._DELETING);
                PrearcDatabase.deleteCacheRow(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject());
            } catch (Exception e) {
                _log.error("An error occurred attempting to clear the prearchive entry for the session " + getSessionData().getName() + ", which doesn't exist at the indicated location " + getSessionDir().getParentFile().getAbsolutePath(), e);
            }
        } else if (PrearcDatabase.setStatus(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus.SEPARATING)) {
            final File sessionXml = new File(getSessionDir() + ".xml");
            if (sessionXml.exists()) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Found the session XML in the file {}, processing.", sessionXml.getAbsolutePath());
                }
                final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
                if (bean instanceof XnatPetmrsessiondataBean) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Found a PET/MR session XML in the file {} with the separate PET/MR flag set to true for the site or project, creating a new request to separate the session.", sessionXml.getAbsolutePath());
                    }
                    final Map<String, SessionData> sessions = PrearcDatabase.separatePetMrSession(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), (XnatPetmrsessiondataBean) bean);
                    if (sessions == null) {
                        _log.warn("No sessions returned from separate PET/MR session operation, check your logs for errors.");
                        return;
                    }
                    // Now finish the upload process, including checking for auto-archive.
                    for (final String modality : sessions.keySet()) {
                        final SessionData sessionData = sessions.get(modality);
                        final PrearcImporterA.PrearcSession session = new PrearcImporterA.PrearcSession(sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), null, getUser());
                        final FinishImageUpload uploader = new FinishImageUpload(null, getUser(), session, null, false, true, false);
                        if (receiving || !uploader.isAutoArchive()) {
                            _log.debug("Processing queue entry for {} in project {} to archive {}", getUser().getUsername(), sessionData.getProject(), sessionData.getExternalUrl());
                            uploader.call();
                        }
                    }
                } else if (_log.isDebugEnabled()) {
                    _log.debug("Found a session XML for a {} session in the file {}. Not PET/MR so not separating.", bean.getFullSchemaElementName(), sessionXml.getAbsolutePath());
                }
            } else {
                _log.warn("Tried to separate a PET/MR session from the path {}, but that session XML doesn't exist.", sessionXml.getAbsolutePath());
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PrearchiveSeparatePetMrHandler.class);
}
