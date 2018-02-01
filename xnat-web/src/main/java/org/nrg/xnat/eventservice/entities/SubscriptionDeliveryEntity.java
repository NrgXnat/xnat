package org.nrg.xnat.eventservice.entities;

import com.google.common.collect.Lists;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
//@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"eventUUID", "subscription"})})
public class SubscriptionDeliveryEntity extends AbstractHibernateEntity {

    public SubscriptionDeliveryEntity() {}

    private UUID eventUUID;
    private SubscriptionEntity subscription;
    private String actionUserLogin;
    private String projectId;
    private String actionInputs;
    private List<TimedEventStatusEntity> timedEventStatuses = Lists.newArrayList();

    public SubscriptionDeliveryEntity(SubscriptionEntity subscription, UUID eventUUID, String actionUserLogin,
                                      String projectId, String actionInputs) {
        this.subscription = subscription;
        this.eventUUID = eventUUID;
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
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

    public String getActionInputs() {
        return actionInputs;
    }

    public void setActionInputs(String actionInputs) {
        this.actionInputs = actionInputs;
    }

    @OneToMany(mappedBy = "subscriptionDeliveryEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public List<TimedEventStatusEntity> getTimedEventStatuses() {
        return timedEventStatuses;
    }

    public void setTimedEventStatuses(List<TimedEventStatusEntity> timedEventStatuses){
        this.timedEventStatuses = timedEventStatuses;
    }

    public void addTimedEventStatus(TimedEventStatusEntity.Status status, Date statusTimestamp, String message){
        TimedEventStatusEntity timedEventStatus = new TimedEventStatusEntity(status,statusTimestamp, message, this);
        this.timedEventStatuses.add(timedEventStatus);
    }
}
