/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveDeleteHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Handles("Delete")
public class PrearchiveDeleteHandler extends AbstractPrearchiveOperationHandler {

    public PrearchiveDeleteHandler(final PrearchiveOperationRequest request) throws Exception {
        super(request);
    }

    @Override
    public void execute() throws Exception {
        SessionData sessionData = getSessionData();
        String studyInstanceUid = null;
        if(sessionData!=null){
            studyInstanceUid = sessionData.getTag();
        }
        if (!getSessionDir().getParentFile().exists()) {
            PrearcDatabase.unsafeSetStatus(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcUtils.PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting session {} from project {}", sessionData.getFolderName(), sessionData.getProject());
        }
        PrearcDatabase.deleteSession(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());

        //Clear study routing/remapping
        if(StringUtils.isNotBlank(studyInstanceUid)){
            try {
                XDAT.getContextService().getBean(StudyRoutingService.class).close(studyInstanceUid);
            }
            catch(Exception e){
                _log.error("Error when clearing study routing information.",e);
            }
            try {
                String script = DefaultAnonUtils.getService().getStudyScript(studyInstanceUid);
                if (StringUtils.isNotBlank(script)) {
                    DefaultAnonUtils.getService().disableStudy(AdminUtils.getAdminUser().getLogin(), studyInstanceUid);
                }
            }
            catch(Exception e){
                _log.error("Error when clearing study remapping information.",e);
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PrearchiveDeleteHandler.class);
}
