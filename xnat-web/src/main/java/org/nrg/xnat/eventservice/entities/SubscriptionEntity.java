package org.nrg.xnat.eventservice.entities;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.transaction.Transactional;
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
    private Map<String,String> attributes;
    private EventServiceFilterEntity eventServiceFilterEntity;
    private Boolean actAsEventUser;
    private Integer ownerId;
    private Integer counter;



    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", getId())
                          .add("name", name)
                          .add("active", active)
                          .add("listenerRegistrationKey", listenerRegistrationKey)
                          .add("eventType", eventType)
                          .add("customListenerId", customListenerId)
                          .add("actionKey", actionKey)
                          .add("attributes", attributes)
                          .add("eventServiceFilterEntity", eventServiceFilterEntity)
                          .add("actAsEventUser", actAsEventUser).toString();

    }

    public SubscriptionEntity(String name, java.lang.Boolean active, String listenerRegistrationKey, String eventType, String customListenerId, String actionKey, Map<String, String> attributes, EventServiceFilterEntity eventServiceFilterEntity, Boolean actAsEventUser) {
        this.name = name;
        this.active = active;
        this.listenerRegistrationKey = listenerRegistrationKey;
        this.eventType = eventType;
        this.customListenerId = customListenerId;
        this.actionKey = actionKey;
        this.attributes = attributes;
        this.eventServiceFilterEntity = eventServiceFilterEntity;
        this.actAsEventUser = actAsEventUser;
    }

    @Transactional
    @Nonnull
    public SubscriptionEntity update(@Nonnull final Subscription subscription) {
        this.name = Strings.isNullOrEmpty(subscription.name()) ? this.name : subscription.name();
        this.active = subscription.active() == null ? this.active : subscription.active();
        this.listenerRegistrationKey = subscription.listenerRegistrationKey() == null ? this.listenerRegistrationKey : subscription.listenerRegistrationKey();
        this.eventType = Strings.isNullOrEmpty(subscription.eventId()) ? this.eventType : subscription.eventId();
        this.customListenerId = Strings.isNullOrEmpty(subscription.customListenerId()) ? this.customListenerId : subscription.customListenerId();
        this.actionKey = Strings.isNullOrEmpty(subscription.actionKey()) ? this.actionKey : subscription.actionKey();
        this.attributes = subscription.attributes() == null ? this.attributes : subscription.attributes();
        this.eventServiceFilterEntity = subscription.eventFilter() == null ? this.eventServiceFilterEntity : EventServiceFilterEntity.fromPojo(subscription.eventFilter());
        this.actAsEventUser = subscription.actAsEventUser() == null ? this.actAsEventUser : subscription.actAsEventUser();
        this.ownerId = subscription.subscriptionOwner() == null ? this.ownerId : subscription.subscriptionOwner();
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

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes == null ?
            Maps.<String, String>newHashMap() :
                attributes; }

    @OneToOne(cascade=CascadeType.ALL)
    public EventServiceFilterEntity getEventServiceFilterEntity() { return eventServiceFilterEntity; }

    public void setEventServiceFilterEntity(EventServiceFilterEntity eventServiceFilterEntity) { this.eventServiceFilterEntity = eventServiceFilterEntity; }

    public Boolean getActAsEventUser() { return actAsEventUser; }

    public void setActAsEventUser(Boolean actAsEventUser) { this.actAsEventUser = actAsEventUser; }


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
                Objects.equal(attributes, that.attributes) &&
                Objects.equal(eventServiceFilterEntity, that.eventServiceFilterEntity) &&
                Objects.equal(actAsEventUser, that.actAsEventUser);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, active, listenerRegistrationKey, eventType, actionKey, attributes, eventServiceFilterEntity, actAsEventUser);
    }

    public static SubscriptionEntity fromPojo(final Subscription subscription) {
        return fromPojoWithTemplate(subscription, new SubscriptionEntity());
    }

    public static SubscriptionEntity fromPojoWithTemplate(final Subscription subscription, final SubscriptionEntity template) {
        if(template==null) {
            return fromPojo(subscription);
        }
        template.name = subscription.name() != null ? subscription.name() : template.name;
        template.active = subscription.active() != null ? subscription.active() : template.active;
        template.listenerRegistrationKey = subscription.listenerRegistrationKey() != null ? subscription.listenerRegistrationKey() : template.listenerRegistrationKey;
        template.eventType = subscription.eventId() != null ? subscription.eventId() : template.eventType;
        template.customListenerId = subscription.customListenerId() != null ? subscription.customListenerId() : template.customListenerId;
        template.actionKey = subscription.actionKey() != null ? subscription.actionKey() :template.actionKey;
        template.attributes = subscription.attributes() != null ? subscription.attributes() : template.attributes;
        template.eventServiceFilterEntity = subscription.eventFilter() != null ? EventServiceFilterEntity.fromPojo(subscription.eventFilter()) : template.eventServiceFilterEntity;
        template.actAsEventUser = subscription.actAsEventUser() != null ? subscription.actAsEventUser() : template.actAsEventUser;
        template.ownerId = subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : template.ownerId;
        template.counter = subscription.useCounter() != null ? subscription.useCounter() : template.counter;
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
                           .attributes(this.attributes)
                           .eventFilter(this.eventServiceFilterEntity != null ? this.eventServiceFilterEntity.toPojo() : null)
                           .actAsEventUser(this.actAsEventUser)
                           .subscriptionOwner(this.ownerId)
                           .useCounter(this.counter)
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

    public Integer getOwnerId() { return ownerId; }

    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public Integer getCounter() { return counter; }

    public void setCounter(Integer counter) { this.counter = counter; }

    public void incCounter() {this.counter++;}
}
