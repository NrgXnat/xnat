package org.nrg.xnat.helpers.prearchive.handlers;

import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
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
        if (!getSessionDir().getParentFile().exists()) {
            PrearcDatabase.unsafeSetStatus(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject(), PrearcUtils.PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject());
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting session {} from project {}", getSessionData().getFolderName(), getSessionData().getProject());
        }
        PrearcDatabase.deleteSession(getSessionData().getFolderName(), getSessionData().getTimestamp(), getSessionData().getProject());
    }

    private static final Logger _log = LoggerFactory.getLogger(PrearchiveDeleteHandler.class);
}
