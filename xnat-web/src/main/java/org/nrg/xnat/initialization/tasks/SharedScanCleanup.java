package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatImagescandataShareI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagescandataShare;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.nrg.xft.event.persist.PersistentWorkflowUtils.ADMIN_EXTERNAL_ID;

@Component
@Slf4j
public class SharedScanCleanup extends AbstractInitializingTask {
    private final XnatAppInfo appInfo;
    private final XnatUserProvider primaryAdminUserProvider;
    private static final String WORKFLOW_ACTION = "Unshare orphaned scans v2";
    private static final String SITE_TYPE = "site";
    private static final String XNAT_IMAGESSCANDATA_ID = "xnat_imagescandata_id";
    private static final String PROJECT = "project";
    private static final String ORPHANED_SCANS = "SELECT scan.xnat_imagescandata_id, share.project "
            + "FROM xnat_imageScandata scan "
            + "LEFT JOIN xnat_imageScandata_share share "
            + "ON scan.xnat_imagescandata_id = share.sharing_share_xnat_imagescandat_xnat_imagescandata_id "
            + "LEFT JOIN (SELECT expt.id,share.project FROM xnat_experimentdata expt "
            + "LEFT JOIN xnat_experimentdata_share share ON expt.id = share.sharing_share_xnat_experimentda_id) expts "
            + "ON scan.image_session_id=expts.id AND share.project=expts.project "
            + "WHERE share.project IS NOT NULL AND expts.project IS NULL;";

    @Autowired
    public SharedScanCleanup(final XnatAppInfo appInfo,
                             final XnatUserProvider primaryAdminUserProvider) {
        this.appInfo = appInfo;
        this.primaryAdminUserProvider = primaryAdminUserProvider;
    }

    @Override
    public String getTaskName() {
        return WORKFLOW_ACTION;
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (!appInfo.isPrimaryNode()) {
            return;
        }

        if (!XFTManager.isComplete()) {
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        final UserI adminUser = primaryAdminUserProvider.get();
        if (scanCleanupRanAndSucceeded(adminUser)) {
            return;
        }

        // Run this in a background thread so as not to block initialization
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> removeOrphanedScans(adminUser));
    }

    private boolean scanCleanupRanAndSucceeded(final UserI adminUser) {
        final CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".ExternalID", ADMIN_EXTERNAL_ID);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".ID", ADMIN_EXTERNAL_ID);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".pipeline_name", WORKFLOW_ACTION);
        cc.addClause(WrkWorkflowdata.SCHEMA_ELEMENT_NAME + ".status", PersistentWorkflowUtils.COMPLETE);
        return WrkWorkflowdata.getWrkWorkflowdatasByField(cc, adminUser, false).stream()
                .anyMatch(w -> Roles.isSiteAdmin(w.getInsertUser()));
    }

    private void removeOrphanedScans(final UserI adminUser) {
        final PersistentWorkflowI wrk;
        try {
            wrk = makeWorkflow(adminUser);
        } catch (PersistentWorkflowUtils.JustificationAbsent | PersistentWorkflowUtils.ActionNameAbsent |
                 PersistentWorkflowUtils.IDAbsent e) {
            log.error("Unable to create workflow for orphaned scan cleanup, which blocks said cleanup", e);
            return;
        }

        boolean success = true;
        try {
            final XFTTable scans = XFTTable.Execute(ORPHANED_SCANS, null, null);
            while (scans.hasMoreRows()) {
                final Map scanRow = scans.nextRowHash();
                final Integer scanId = (Integer) scanRow.get(XNAT_IMAGESSCANDATA_ID);
                final String projectId = (String) scanRow.get(PROJECT);
                final XnatImagescandata scan = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scanId, null, false);
                if (!removeOrphanedScansFromSession(scan, projectId, adminUser, wrk.buildEvent())) {
                    success = false;
                }
            }
        } catch (SQLException | DBPoolException e) {
            log.error("Unable to query for orphaned scans", e);
            success = false;
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

    private PersistentWorkflowI makeWorkflow(final UserI adminUser) throws PersistentWorkflowUtils.IDAbsent,
            PersistentWorkflowUtils.JustificationAbsent, PersistentWorkflowUtils.ActionNameAbsent {
        final EventDetails eventDetails = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE,
                WORKFLOW_ACTION, "Cleanup", null);
        return WorkflowUtils.buildOpenWorkflow(adminUser, SITE_TYPE, ADMIN_EXTERNAL_ID,
                ADMIN_EXTERNAL_ID, eventDetails);
    }

    private boolean removeOrphanedScansFromSession(final XnatImagescandata scan, final String projectId, final UserI user, final EventMetaI c) {
        boolean success = true;
        for (final XnatImagescandataShareI sharedScan : scan.getSharing_share()) {
            if (StringUtils.equals(projectId, sharedScan.getProject())) {
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
