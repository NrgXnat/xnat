package org.nrg.xnat.eventservice.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
//@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"eventUUID", "subscription"})})
public class SubscriptionDeliveryEntity extends AbstractHibernateEntity {

    public SubscriptionDeliveryEntity() {}

    private UUID eventUUID;
    private String eventType;
    private SubscriptionEntity subscription;
    private String actionUserLogin;
    private String projectId;
    private String actionInputs;
    private TriggeringEventEntity triggeringEventEntity;
    private Set<TimedEventStatusEntity> timedEventStatuses = new LinkedHashSet<>();
    private TimedEventStatusEntity.Status status;
    private Date statusTimestamp;

    public SubscriptionDeliveryEntity(SubscriptionEntity subscription, String eventType, String actionUserLogin,
                                      String projectId, String actionInputs) {
        this.subscription = subscription;
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

    public void addTriggeringEventEntity(String eventName, Boolean isXsiType, String xnatType, String xsiUri, String objectLabel) {
        this.triggeringEventEntity = new TriggeringEventEntity(eventName, isXsiType, xnatType, xsiUri, objectLabel);
    }

    @OneToMany(mappedBy = "subscriptionDeliveryEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    public Set<TimedEventStatusEntity> getTimedEventStatuses() {
        return timedEventStatuses;
    }

    public void setTimedEventStatuses(Set<TimedEventStatusEntity> timedEventStatuses){
        this.timedEventStatuses = timedEventStatuses;
    }

    public void addTimedEventStatus(TimedEventStatusEntity.Status status, Date statusTimestamp, String message, Object payload){
        TimedEventStatusEntity timedEventStatus = new TimedEventStatusEntity(status,statusTimestamp, message, payload, this);
        this.setStatus(status);
        this.setStatusTimestamp(statusTimestamp);
        this.timedEventStatuses.add(timedEventStatus);
    }

    public TimedEventStatusEntity.Status getStatus() { return status; }

    public void setStatus(TimedEventStatusEntity.Status status) { this.status = status; }


    @Column(name = "status_timestamp")
    public Date getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(Date statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

}
