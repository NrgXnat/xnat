package org.nrg.xnat.event.model;

import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xnat.tracking.model.TrackableEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class BulkLaunchEvent implements TrackableEvent {
    private final String id;
    private final Integer userId;
    private Integer n = null;
    private Integer failures = null;
    private Integer steps = null;
    private boolean success = false;
    private boolean completed = false;
    private String message = null;
    private PersistentWorkflowI workflow = null;

    public BulkLaunchEvent(String id, Integer userId) {
        this.id = id;
        this.userId = userId;
    }

    public BulkLaunchEvent(String bulkLaunchId, Integer userId, PersistentWorkflowI workflow) {
        this(bulkLaunchId, userId);
        this.workflow = workflow;
    }

    private BulkLaunchEvent(String bulkLaunchId, Integer userId, boolean success, String message) {
        this(bulkLaunchId, userId);
        this.completed = true;
        this.success = success;
        this.message = message;
    }

    public static BulkLaunchEvent initial(String bulkLaunchId, Integer userId, int n) {
        BulkLaunchEvent ble = new BulkLaunchEvent(bulkLaunchId, userId);
        ble.n = n;
        return ble;
    }

    public static BulkLaunchEvent initial(String bulkLaunchId, Integer userId, int n, int steps) {
        BulkLaunchEvent ble = initial(bulkLaunchId, userId, n);
        ble.steps = steps;
        return ble;
    }

    public static BulkLaunchEvent executorServiceFailureCount(String bulkLaunchId, Integer userId, int failures) {
        BulkLaunchEvent ble = new BulkLaunchEvent(bulkLaunchId, userId);
        ble.failures = failures;
        return ble;
    }

    @Nonnull
    @Override
    public String getTrackingId() {
        return id;
    }

    @Nonnull
    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String updateTrackingPayload(@Nullable String currentPayload) throws IOException {
        BulkLaunchLog statusLog;
        if (currentPayload != null) {
            statusLog = XDAT.getSerializerService().getObjectMapper()
                    .readValue(currentPayload, BulkLaunchLog.class);
        } else {
            statusLog = new BulkLaunchLog();
        }
        if (n != null) {
            statusLog.setTotal(n);
        }
        if (steps != null) {
            statusLog.setSteps(steps);
        }
        if (failures != null) {
            statusLog.addFailures(failures);
        }
        if (workflow != null) {
            statusLog.addOrUpdateWorkflow(workflow);
        }
        if (statusLog.bulkLaunchComplete()) {
            XDAT.getContextService().getBean(NrgEventService.class).triggerEvent(
                    new BulkLaunchEvent(id, userId, statusLog.bulkLaunchSuccess(), statusLog.bulkLaunchMessage()));
        }
        return XDAT.getSerializerService().getObjectMapper().writeValueAsString(statusLog);
    }
}
