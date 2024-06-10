package org.nrg.xnat.eventservice.entities;

import java.util.Date;


public class SubscriptionDeliverySummaryEntity {

    private Long id;
    private String eventName;
    private Long subscriptionId;
    private String subscriptionName;
    private String actionUser;
    private String projectId;
    private String triggerLabel;
    private TimedEventStatusEntity.Status status;
    private Boolean errorState;
    private Date timestamp;

    public SubscriptionDeliverySummaryEntity(Long id, String eventName, Long subscriptionId, String subscriptionName,
                                             String actionUser, String projectId, String triggerLabel,
                                             TimedEventStatusEntity.Status status, Boolean errorState,
                                             Date timestamp) {
        this.id = id;
        this.eventName = eventName;
        this.subscriptionId = subscriptionId;
        this.subscriptionName = subscriptionName;
        this.actionUser = actionUser;
        this.projectId = projectId;
        this.triggerLabel = triggerLabel;
        this.status = status;
        this.errorState = errorState;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public Long getSubscriptionId() { return subscriptionId; }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getActionUser() {
        return actionUser;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTriggerLabel() {
        return triggerLabel;
    }

    public TimedEventStatusEntity.Status getStatus() {
        return status;
    }

    public Boolean getErrorState() { return errorState; }

    public Date getTimestamp() {
        return timestamp;
    }

}

