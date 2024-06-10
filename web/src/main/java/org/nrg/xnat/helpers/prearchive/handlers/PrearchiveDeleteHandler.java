/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveDeleteHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.actions.postArchive.ClearStudyRemappingAction;
import org.nrg.xnat.actions.postArchive.ClearStudyRoutingAction;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import java.util.Map;

import static org.nrg.xnat.archive.Operation.Delete;

@Handles(Delete)
@Slf4j
public class PrearchiveDeleteHandler extends AbstractPrearchiveOperationHandler {
    public PrearchiveDeleteHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider, final DicomInboxImportRequestService importRequestService) {
        super(request, eventService, userProvider, importRequestService);
    }

    @Override
    public void execute() throws Exception {
        final SessionData sessionData = getSessionData();
        if (sessionData == null) {
            log.warn("Tried to process prearchive delete request for directory {}, but the session data object is null, which makes things difficult", getSessionDir());
            return;
        }
        final String studyInstanceUid = sessionData.getTag();
        final String project          = sessionData.getProject();
        final String timestamp        = sessionData.getTimestamp();
        final String folderName       = sessionData.getFolderName();

        if (!getSessionDir().getParentFile().exists()) {
            PrearcDatabase.unsafeSetStatus(folderName, timestamp, project, PrearcUtils.PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(folderName, timestamp, project);
        }

        log.debug("Deleting session {} from project {}", folderName, project);
        PrearcDatabase.deleteSession(folderName, timestamp, project);

        // Clear study routing and remapping if the study instance UID is valid.
        if (StringUtils.isNotBlank(studyInstanceUid)) {
            final Map<String, Object> uidParam = ImmutableMap.of("studyInstanceUid", studyInstanceUid);
            new ClearStudyRoutingAction().execute(Users.getAdminUser(), null, uidParam);
            new ClearStudyRemappingAction().execute(Users.getAdminUser(), null, uidParam);
        }
    }
}
