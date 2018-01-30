package org.nrg.xnat.eventservice.entities;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.eventservice.model.Subscription;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
@Table(uniqueConstraints= {
        @UniqueConstraint(columnNames="name"),
        @UniqueConstraint(columnNames = "listenerRegistrationKey")
})
public class SubscriptionEntity extends AbstractHibernateEntity {

    public SubscriptionEntity() {}


    private String name;
    private Boolean active;
    private String listenerRegistrationKey;
    private String eventType;
    private String customListenerId;
    private String actionKey;
    private String projectId;
    private Map<String,String> attributes;
    private EventServiceFilterEntity eventServiceFilterEntity;
    private Boolean actAsEventUser;
    private String subscriptionOwner;

    public SubscriptionEntity(String name, Boolean active, String listenerRegistrationKey, String eventType,
                              String customListenerId, String actionKey, String projectId,
                              Map<String, String> attributes,
                              EventServiceFilterEntity eventServiceFilterEntity, Boolean actAsEventUser,
                              String subscriptionOwner) {
        this.name = name;
        this.active = active;
        this.listenerRegistrationKey = listenerRegistrationKey;
        this.eventType = eventType;
        this.customListenerId = customListenerId;
        this.actionKey = actionKey;
        this.projectId = projectId;
        this.attributes = attributes;
        this.eventServiceFilterEntity = eventServiceFilterEntity;
        this.actAsEventUser = actAsEventUser;
        this.subscriptionOwner = subscriptionOwner;
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", listenerRegistrationKey='" + listenerRegistrationKey + '\'' +
                ", eventType='" + eventType + '\'' +
                ", customListenerId='" + customListenerId + '\'' +
                ", actionKey='" + actionKey + '\'' +
                ", project=" + projectId +
                ", attributes=" + attributes +
                ", eventServiceFilterEntity=" + eventServiceFilterEntity +
                ", actAsEventUser=" + actAsEventUser +
                ", subscriptionOwner='" + subscriptionOwner + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubscriptionEntity that = (SubscriptionEntity) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(active, that.active) &&
                Objects.equal(listenerRegistrationKey, that.listenerRegistrationKey) &&
                Objects.equal(eventType, that.eventType) &&
                Objects.equal(customListenerId, that.customListenerId) &&
                Objects.equal(actionKey, that.actionKey) &&
                Objects.equal(projectId, that.projectId) &&
                Objects.equal(attributes, that.attributes) &&
                Objects.equal(eventServiceFilterEntity, that.eventServiceFilterEntity) &&
                Objects.equal(actAsEventUser, that.actAsEventUser) &&
                Objects.equal(subscriptionOwner, that.subscriptionOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, active, listenerRegistrationKey, eventType, customListenerId, actionKey, projectId, attributes, eventServiceFilterEntity, actAsEventUser, subscriptionOwner);
    }

    @Transactional
    @Nonnull
    public SubscriptionEntity update(@Nonnull final Subscription subscription) {
        this.name = Strings.isNullOrEmpty(subscription.name()) ? this.name : subscription.name();
        this.projectId = Strings.isNullOrEmpty(subscription.projectId()) ? this.projectId : subscription.projectId();
        this.active = subscription.active() == null ? this.active : subscription.active();
        this.listenerRegistrationKey = subscription.listenerRegistrationKey() == null ? this.listenerRegistrationKey : subscription.listenerRegistrationKey();
        this.eventType = Strings.isNullOrEmpty(subscription.eventId()) ? this.eventType : subscription.eventId();
        this.customListenerId = Strings.isNullOrEmpty(subscription.customListenerId()) ? this.customListenerId : subscription.customListenerId();
        this.actionKey = Strings.isNullOrEmpty(subscription.actionKey()) ? this.actionKey : subscription.actionKey();
        this.attributes = subscription.attributes() == null ? this.attributes : subscription.attributes();
        this.eventServiceFilterEntity = subscription.eventFilter() == null ? this.eventServiceFilterEntity : EventServiceFilterEntity.fromPojo(subscription.eventFilter());
        this.actAsEventUser = subscription.actAsEventUser() == null ? this.actAsEventUser : subscription.actAsEventUser();
        this.subscriptionOwner = subscription.subscriptionOwner() == null ? this.subscriptionOwner : subscription.subscriptionOwner();
        return this;
    }


    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEventType() { return eventType; }

    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getCustomListenerId() {return this.customListenerId;}

    public void setCustomListenerId(String customListenerId) {this.customListenerId = customListenerId;}

    public String getActionProvider() { return actionKey; }

    public void setActionProvider(String actionKey) { this.actionKey = actionKey; }

    @ElementCollection(fetch = FetchType.EAGER)
    public Map<String, String> getAttributes() { return attributes; }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes == null ?
            Maps.<String, String>newHashMap() :
                attributes; }

    @OneToOne(cascade=CascadeType.ALL)
    public EventServiceFilterEntity getEventServiceFilterEntity() { return eventServiceFilterEntity; }

    public void setEventServiceFilterEntity(EventServiceFilterEntity eventServiceFilterEntity) { this.eventServiceFilterEntity = eventServiceFilterEntity; }

    public Boolean getActAsEventUser() { return actAsEventUser; }

    public void setActAsEventUser(Boolean actAsEventUser) { this.actAsEventUser = actAsEventUser; }






    public static SubscriptionEntity fromPojo(final Subscription subscription) {
        return fromPojoWithTemplate(subscription, new SubscriptionEntity());
    }

    public static SubscriptionEntity fromPojoWithTemplate(final Subscription subscription, final SubscriptionEntity template) {
        if(template==null) {
            return fromPojo(subscription);
        }
        template.name = subscription.name() != null ? subscription.name() : template.name;
        template.active = subscription.active() != null ? subscription.active() : (template.active == null ? true : template.active);
        template.listenerRegistrationKey = subscription.listenerRegistrationKey() != null ? subscription.listenerRegistrationKey() : template.listenerRegistrationKey;
        template.eventType = subscription.eventId() != null ? subscription.eventId() : template.eventType;
        template.customListenerId = subscription.customListenerId() != null ? subscription.customListenerId() : template.customListenerId;
        template.actionKey = subscription.actionKey() != null ? subscription.actionKey() :template.actionKey;
        template.projectId = subscription.projectId() != null ? subscription.projectId() : template.projectId;
        template.attributes = subscription.attributes() != null ? subscription.attributes() : template.attributes;
        template.eventServiceFilterEntity = subscription.eventFilter() != null ? EventServiceFilterEntity.fromPojo(subscription.eventFilter()) : template.eventServiceFilterEntity;
        template.actAsEventUser = subscription.actAsEventUser() != null ? subscription.actAsEventUser() : template.actAsEventUser;
        template.subscriptionOwner = subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : template.subscriptionOwner;
        return template;
    }

    @Transient
    public Subscription toPojo() {
        return Subscription.builder()
                           .id(this.getId())
                           .name(this.name)
                           .active(this.active)
                           .listenerRegistrationKey(this.listenerRegistrationKey)
                           .eventId(this.eventType)
                           .customListenerId(this.customListenerId)
                           .actionKey(this.actionKey)
                           .projectId(this.projectId)
                           .attributes(this.attributes)
                           .eventFilter(this.eventServiceFilterEntity != null ? this.eventServiceFilterEntity.toPojo() : null)
                           .actAsEventUser(this.actAsEventUser)
                           .subscriptionOwner(this.subscriptionOwner)
                           .build();
    }

    @Nonnull
    @Transient
    static public List<Subscription> toPojo(final List<SubscriptionEntity> subscriptionEntities) {
        List<Subscription> subscriptions = new ArrayList<>();
        if(subscriptionEntities!= null) {
            for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
                subscriptions.add(subscriptionEntity.toPojo());
            }
        }
        return subscriptions;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getListenerRegistrationKey() {
        return listenerRegistrationKey;
    }

    public void setListenerRegistrationKey(String listenerRegistrationKey) {
        this.listenerRegistrationKey = listenerRegistrationKey;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    public String getSubscriptionOwner() { return subscriptionOwner; }

    public void setSubscriptionOwner(String subscriptionOwner) { this.subscriptionOwner = subscriptionOwner; }

}
