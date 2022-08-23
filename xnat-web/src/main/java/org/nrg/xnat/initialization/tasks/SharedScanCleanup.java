package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagescandataShareI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.nrg.xft.event.persist.PersistentWorkflowUtils.ADMIN_EXTERNAL_ID;

@Component
@Slf4j
public class SharedScanCleanup extends AbstractInitializingTask {
    private final XnatAppInfo appInfo;
    private final XnatUserProvider primaryAdminUserProvider;
    private static final String WORKFLOW_ACTION = "Unshare orphaned scans";
    private static final String SITE_TYPE = "site";

    @Autowired
    public SharedScanCleanup(final XnatAppInfo appInfo,
                             final XnatUserProvider primaryAdminUserProvider) {
        this.appInfo = appInfo;
        this.primaryAdminUserProvider = primaryAdminUserProvider;
    }

    @Override
    public String getTaskName() {
        return "Unshare orphaned scans";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (!appInfo.isPrimaryNode()) {
            return;
        }

        if (!XFTManager.isComplete()) {
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        UserI adminUser = primaryAdminUserProvider.get();
        if (scanCleanupRanAndSucceeded(adminUser)) {
            return;
        }

        // Run this in a background thread so as not to block initialization
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> removeOrphanedScans(adminUser));
    }

    private boolean scanCleanupRanAndSucceeded(UserI adminUser) {
        final CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".ExternalID", ADMIN_EXTERNAL_ID);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".ID", ADMIN_EXTERNAL_ID);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".pipeline_name", WORKFLOW_ACTION);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".status", PersistentWorkflowUtils.COMPLETE);
        return WrkWorkflowdata.getWrkWorkflowdatasByField(cc, adminUser, false).stream()
                .anyMatch(w -> Roles.isSiteAdmin(w.getCreateUser()));
    }

    private void removeOrphanedScans(UserI adminUser) {
        final PersistentWorkflowI wrk;
        try {
            wrk = makeWorkflow(adminUser);
        } catch (PersistentWorkflowUtils.JustificationAbsent | PersistentWorkflowUtils.ActionNameAbsent |
                PersistentWorkflowUtils.IDAbsent e) {
            log.error("Unable to create workflow for orphaned scan cleanup, which blocks said cleanup", e);
            return;
        }

        boolean success = true;
        // Loop over projects to limit size of experiment list (avoid memory pressure)
        for (final XnatProjectdata project : XnatProjectdata.getAllXnatProjectdatas(adminUser, true)) {
            log.warn("Shared scan cleanup running on project {}", project.getId());
            for (final Object experiment : project.getExperiments()) {
                if (!(experiment instanceof XnatImagesessiondata)) {
                    continue;
                }
                final XnatImagesessiondata session = (XnatImagesessiondata) experiment;
                if (!session.getProject().equals(project.getId())) {
                    // It's a shared session, which we'll handle when we get to its primary project
                    continue;
                }
                if (!removeOrphanedScansFromSession(session, adminUser, wrk.buildEvent())) {
                    success = false;
                }
            }
        }

        try {
            if (success) {
                WorkflowUtils.complete(wrk, wrk.buildEvent());
            } else {
                WorkflowUtils.fail(wrk, wrk.buildEvent());
            }
        } catch (Exception e) {
            log.warn("Unable to save orphaned scan unshare workflow", e);
        }
    }

    private PersistentWorkflowI makeWorkflow(UserI adminUser) throws PersistentWorkflowUtils.IDAbsent,
            PersistentWorkflowUtils.JustificationAbsent, PersistentWorkflowUtils.ActionNameAbsent {
        EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE,
                WORKFLOW_ACTION, "Cleanup", null);
        return WorkflowUtils.buildOpenWorkflow(adminUser, SITE_TYPE, ADMIN_EXTERNAL_ID,
                ADMIN_EXTERNAL_ID, eventDetails);
    }

    private boolean removeOrphanedScansFromSession(XnatImagesessiondata session, UserI user, EventMetaI c) {
        // Projects into which this session is shared
        Set<String> sessionSharedIntoProjects = session.getSharing_share().stream()
                .map(XnatExperimentdataShareI::getProject)
                .collect(Collectors.toSet());

        boolean success = true;
        for (final XnatImagescandataI scan : session.getScans_scan()) {
            for (final XnatImagescandataShareI sharedScan : scan.getSharing_share()) {
                if (sessionSharedIntoProjects.contains(sharedScan.getProject())) {
                    continue;
                }
                // If a scan is shared into a project while its parent session is NOT, it's an orphaned shared scan
                try {
                    SaveItemHelper.authorizedRemoveChild(((XnatImagescandata) scan).getItem(),
                            XnatImagescandata.SCHEMA_ELEMENT_NAME + "/sharing/share",
                            ((XnatImagescandataShare) sharedScan).getItem(), user, c);
                } catch (Exception e) {
                    log.error("Unable to unshare scan id {} shared id {}", scan.getXnatImagescandataId(),
                            sharedScan.getXnatImagescandataShareId(), e);
                    success = false;
                }
            }
        }
        return success;
    }
}
