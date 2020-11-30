package org.nrg.xnat.eventservice.entities;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class SubscriptionDeliveryEntity extends AbstractHibernateEntity {

    public SubscriptionDeliveryEntity() {}

    private UUID eventUUID;
    private String eventType;
    private SubscriptionEntity subscription;
    private Long subscriptionId;
    private String description;
    private String actionUserLogin;
    private String projectId;
    private String actionInputs;
    private TriggeringEventEntity triggeringEventEntity;
    private Set<TimedEventStatusEntity> timedEventStatuses = new LinkedHashSet<>();
    private TimedEventStatusEntity.Status status;
    private Date statusTimestamp;
    private String statusMessage;
    private Boolean errorState = null;
    private EventServicePayloadEntity payload;
    private final Integer MAX_TEXT_LENGTH = 255;

    public SubscriptionDeliveryEntity(SubscriptionEntity subscription, String eventType, String actionUserLogin,
                                      String projectId, String actionInputs) {
        this.subscription = subscription;
        this.description = subscription.getName();
        this.subscriptionId = subscription.getId();
        this.eventType = eventType;
        this.actionUserLogin = actionUserLogin;
        this.projectId = projectId;
        this.actionInputs = actionInputs;
    }

    public UUID getEventUUID() {
        return eventUUID;
    }

    public void setEventUUID(UUID eventUUID) {
        this.eventUUID = eventUUID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public SubscriptionEntity getSubscription() { return subscription; }

    public void setSubscription(SubscriptionEntity subscription) {
        this.subscription = subscription;
    }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return this.description; }

    public Long getSubscriptionId() { return subscriptionId; }

    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getActionUserLogin() {
        return actionUserLogin;
    }

    public void setActionUserLogin(String actionUserLogin) {
        this.actionUserLogin = actionUserLogin;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Lob
    public String getActionInputs() {
        return actionInputs;
    }

    public void setActionInputs(String actionInputs) {
        this.actionInputs = actionInputs;
    }

    @OneToOne(cascade=CascadeType.ALL)
    public TriggeringEventEntity getTriggeringEventEntity() { return triggeringEventEntity; }

    public void setTriggeringEventEntity(TriggeringEventEntity triggeringEventEntity) { this.triggeringEventEntity = triggeringEventEntity; }

    public void addTriggeringEventEntity(String eventName, String status, Boolean isXsiType, String xnatType, String xsiUri, String objectLabel) {
        this.triggeringEventEntity = new TriggeringEventEntity(eventName, status, isXsiType, xnatType, xsiUri, objectLabel);
    }

    @OneToMany(mappedBy = "subscriptionDeliveryEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    public Set<TimedEventStatusEntity> getTimedEventStatuses() {
        return timedEventStatuses;
    }

    public void setTimedEventStatuses(Set<TimedEventStatusEntity> timedEventStatuses){
        this.timedEventStatuses = timedEventStatuses;
    }

    public void addTimedEventStatus(TimedEventStatusEntity.Status status, Date statusTimestamp, String message){
        TimedEventStatusEntity timedEventStatus = new TimedEventStatusEntity(status,statusTimestamp, message, this);
        this.setStatus(status);
        this.setStatusMessage(message);
        this.setStatusTimestamp(statusTimestamp);
        this.timedEventStatuses.add(timedEventStatus);
    }


    public TimedEventStatusEntity.Status getStatus() { return status; }

    public void setStatus(TimedEventStatusEntity.Status status) {
        this.status = status;
        this.setErrorState(status == null ? null :
                status.name().matches(".*FAULT.*|.*ERROR.*|.*FAILED.*") ? true : false);
    }

    @Column(name = "status_timestamp")
    public Date getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(Date statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    @Column(name = "status_message")
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {

        this.statusMessage = statusMessage != null ? StringUtils.left(statusMessage, MAX_TEXT_LENGTH) : null;
    }

    public Boolean getErrorState() { return errorState; }

    public void setErrorState(Boolean errorState) { this.errorState = errorState; }


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinTable(name = "subscription_delivery_payload",
            joinColumns =
                    { @JoinColumn(name = "delivery_id", referencedColumnName = "id") },
            inverseJoinColumns =
                    { @JoinColumn(name = "payload_id", referencedColumnName = "id")})
    public EventServicePayloadEntity getPayload() { return payload; }

    @Transient
    public Object getPayloadObject() {
        return payload != null ? payload.getPayload() : null;
    }

    public void setPayload(EventServicePayloadEntity payload) { this.payload = payload; }

}
