package org.nrg.xnat.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude
public class BulkLaunchLog {
    private int total = -1;
    private int successCount = 0;
    private int failureCount = 0;

    private Map<Integer, WorkflowLog> workflows = new HashMap<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public Map<Integer, WorkflowLog> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(Map<Integer, WorkflowLog> workflows) {
        this.workflows = workflows;
    }

    @JsonIgnore
    public void addOrUpdateWorkflow(Integer id, String itemId, String status, String details, String containerId) {
        if (status.contains(PersistentWorkflowUtils.FAILED)) {
            failureCount++;
        } else if (status.equals(PersistentWorkflowUtils.COMPLETE)) {
            successCount++;
        }
        workflows.put(id, new WorkflowLog(id, itemId, status, details, containerId));
    }

    @JsonIgnore
    public boolean bulkLaunchComplete() {
        return hasTotal() && successCount + failureCount == total;
    }

    private boolean hasTotal() {
        return total != -1;
    }

    @JsonIgnore
    public boolean bulkLaunchSuccess() {
        return hasTotal() && successCount == total;
    }

    @JsonIgnore
    @Nullable
    public String bulkLaunchMessage() {
        if (!hasTotal()) {
            return null;
        }
        if (bulkLaunchSuccess()) {
            return "All jobs succeeded";
        } else if (failureCount == total) {
            return "All jobs failed";
        } else {
            return successCount + " jobs succeed / " + failureCount + " jobs failed";
        }
    }

    @JsonIgnore
    public void addFailures(Integer failures) {
        failureCount += failures;
    }

    @JsonInclude
    private static class WorkflowLog {
        private Integer id;
        private String itemId;
        private String status;
        private String details;
        private String containerId;

        public WorkflowLog(){}

        public WorkflowLog(Integer id, String itemId, String status, String details, String containerId) {
            this.id = id;
            this.itemId = itemId;
            this.status = status;
            this.details = details;
            this.containerId = containerId;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public String getContainerId() {
            return containerId;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }
    }
}
