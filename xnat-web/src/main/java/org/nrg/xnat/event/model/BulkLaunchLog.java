package org.nrg.xnat.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@JsonInclude
public class BulkLaunchLog {
    private int total = -1;
    private int steps = 1;
    private int successCount = 0;
    private int failureCount = 0;

    private Map<Integer, WorkflowLog> workflows = new TreeMap<>();
    private Map<String, Integer> itemSteps = new HashMap<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
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

    public Map<String, Integer> getItemSteps() {
        return itemSteps;
    }

    public void setItemSteps(Map<String, Integer> itemSteps) {
        this.itemSteps = itemSteps;
    }

    @JsonIgnore
    public void addOrUpdateWorkflow(PersistentWorkflowI workflow) {
        Integer id = workflow.getWorkflowId();
        String status = workflow.getStatus();
        WorkflowLog lastLog = workflows.get(id);
        boolean statusRecorded = lastLog != null && lastLog.statusRecorded;
        if (!statusRecorded) {
            if (status.startsWith(PersistentWorkflowUtils.FAILED)) {
                failureCount++;
                statusRecorded = true;
            } else if (status.equals(PersistentWorkflowUtils.COMPLETE)) {
                // Keep track of completions so we know when we've completed all the steps
                // No need to track failures bc as soon as we hit one, we abort
                String itemId = getWorkflowItemId(workflow);
                int count = itemSteps.getOrDefault(itemId, 0);
                itemSteps.put(itemId, ++count);
                if (count == steps) {
                    // if this is the last step, increment the success count
                    successCount++;
                }
                statusRecorded = true;
            }
        }
        workflows.put(id, new WorkflowLog(workflow, statusRecorded));
    }

    private String getWorkflowItemId(PersistentWorkflowI workflow) {
        String suffix = StringUtils.defaultIfBlank(workflow.getScanId(), "");
        if (StringUtils.isNotBlank(suffix)) {
            suffix = "-" + suffix;
        }
        return workflow.getId() + suffix;
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
    public static class WorkflowLog {
        private Integer id;
        private String itemId;
        private String status;
        private String details;
        private String containerId;
        private String pipelineName;
        private String justification;
        private String src;
        private String externalId;
        private String type;
        private String category;
        private String itemType;
        private String currentStepId;
        private String stepDescription;
        private Date launchTime;
        private Date currentStepLaunchTime;
        private String percentageComplete;
        private String jobId;
        private String scanId;
        private boolean statusRecorded;

        public WorkflowLog(){}

        public WorkflowLog(PersistentWorkflowI workflow, boolean statusRecorded) {
            this.id = workflow.getWorkflowId();
            this.itemId = workflow.getId();
            this.status = workflow.getStatus();
            this.details = workflow.getDetails();
            this.containerId = workflow.getComments();
            this.pipelineName = workflow.getPipelineName();
            this.justification = workflow.getJustification();
            this.src = workflow.getSrc();
            this.externalId = workflow.getExternalid();
            this.type = workflow.getType();
            this.category = workflow.getCategory();
            this.itemType = workflow.getDataType();
            this.currentStepId = workflow.getCurrentStepId();
            this.stepDescription = workflow.getStepDescription();
            this.launchTime = workflow.getLaunchTimeDate();
            this.currentStepLaunchTime = workflow.getCurrentStepLaunchTimeDate();
            this.percentageComplete = workflow.getPercentagecomplete();
            this.jobId = workflow.getJobid();
            this.scanId = workflow.getScanId();
            this.statusRecorded = statusRecorded;
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

        public boolean isStatusRecorded() {
            return statusRecorded;
        }

        public void setStatusRecorded(boolean statusRecorded) {
            this.statusRecorded = statusRecorded;
        }

        public String getPipelineName() {
            return pipelineName;
        }

        public void setPipelineName(String pipelineName) {
            this.pipelineName = pipelineName;
        }

        public String getJustification() {
            return justification;
        }

        public void setJustification(String justification) {
            this.justification = justification;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public String getCurrentStepId() {
            return currentStepId;
        }

        public void setCurrentStepId(String currentStepId) {
            this.currentStepId = currentStepId;
        }

        public String getStepDescription() {
            return stepDescription;
        }

        public void setStepDescription(String stepDescription) {
            this.stepDescription = stepDescription;
        }

        public Date getLaunchTime() {
            return launchTime;
        }

        public void setLaunchTime(Date launchTime) {
            this.launchTime = launchTime;
        }

        public Date getCurrentStepLaunchTime() {
            return currentStepLaunchTime;
        }

        public void setCurrentStepLaunchTime(Date currentStepLaunchTime) {
            this.currentStepLaunchTime = currentStepLaunchTime;
        }

        public String getPercentageComplete() {
            return percentageComplete;
        }

        public void setPercentageComplete(String percentageComplete) {
            this.percentageComplete = percentageComplete;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getScanId() {
            return scanId;
        }

        public void setScanId(String scanId) {
            this.scanId = scanId;
        }
    }
}
