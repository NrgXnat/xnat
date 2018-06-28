package org.nrg.xnat.eventservice.entities;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
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
    private String customListenerId;
    private String actionKey;
    private Map<String,String> attributes;
    private EventServiceFilterEntity eventServiceFilterEntity;
    private Boolean actAsEventUser;
    private String subscriptionOwner;
    private List<SubscriptionDeliveryEntity> subscriptionDeliveryEntities;

    public SubscriptionEntity(String name, Boolean active, String listenerRegistrationKey, String customListenerId, String actionKey, Map<String, String> attributes, EventServiceFilterEntity eventServiceFilterEntity, Boolean actAsEventUser, String subscriptionOwner, List<SubscriptionDeliveryEntity> subscriptionDeliveryEntities) {
        this.name = name;
        this.active = active;
        this.listenerRegistrationKey = listenerRegistrationKey;
        this.customListenerId = customListenerId;
        this.actionKey = actionKey;
        this.attributes = attributes;
        this.eventServiceFilterEntity = eventServiceFilterEntity;
        this.actAsEventUser = actAsEventUser;
        this.subscriptionOwner = subscriptionOwner;
        this.subscriptionDeliveryEntities = subscriptionDeliveryEntities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubscriptionEntity that = (SubscriptionEntity) o;
        return java.util.Objects.equals(name, that.name) &&
                java.util.Objects.equals(active, that.active) &&
                java.util.Objects.equals(listenerRegistrationKey, that.listenerRegistrationKey) &&
                java.util.Objects.equals(customListenerId, that.customListenerId) &&
                java.util.Objects.equals(actionKey, that.actionKey) &&
                java.util.Objects.equals(attributes, that.attributes) &&
                java.util.Objects.equals(eventServiceFilterEntity, that.eventServiceFilterEntity) &&
                java.util.Objects.equals(actAsEventUser, that.actAsEventUser) &&
                java.util.Objects.equals(subscriptionOwner, that.subscriptionOwner) &&
                java.util.Objects.equals(subscriptionDeliveryEntities, that.subscriptionDeliveryEntities);
    }

    @Override
    public int hashCode() {

        return java.util.Objects.hash(super.hashCode(), name, active, listenerRegistrationKey, customListenerId, actionKey, attributes, eventServiceFilterEntity, actAsEventUser, subscriptionOwner, subscriptionDeliveryEntities);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("active", active)
                .add("listenerRegistrationKey", listenerRegistrationKey)
                .add("customListenerId", customListenerId)
                .add("actionKey", actionKey)
                .add("attributes", attributes)
                .add("eventServiceFilterEntity", eventServiceFilterEntity)
                .add("actAsEventUser", actAsEventUser)
                .add("subscriptionOwner", subscriptionOwner)
                .add("subscriptionDeliveryEntities", subscriptionDeliveryEntities)
                .toString();
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

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

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public List<SubscriptionDeliveryEntity> getSubscriptionDeliveryEntities() {
        return subscriptionDeliveryEntities;
    }

    public void setSubscriptionDeliveryEntities(
            List<SubscriptionDeliveryEntity> subscriptionDeliveryEntities) {
        this.subscriptionDeliveryEntities = subscriptionDeliveryEntities;
    }

    public static SubscriptionEntity fromPojo(final Subscription subscription) {
        return fromPojoWithTemplate(subscription, new SubscriptionEntity());
    }

    public static SubscriptionEntity fromPojoWithTemplate(final Subscription subscription, final SubscriptionEntity template) {
        if(template==null) {
            return fromPojo(subscription);
        }
        if(subscription==null){
            return null;
        }
        template.name = subscription.name() != null ? subscription.name() : template.name;
        template.active = subscription.active() != null ? subscription.active() : (template.active == null ? true : template.active);
        template.listenerRegistrationKey = subscription.listenerRegistrationKey() != null ? subscription.listenerRegistrationKey() : template.listenerRegistrationKey;
        template.customListenerId = subscription.customListenerId() != null ? subscription.customListenerId() : template.customListenerId;
        template.actionKey = subscription.actionKey() != null ? subscription.actionKey() :template.actionKey;
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
                           .customListenerId(this.customListenerId)
                           .actionKey(this.actionKey)
                           .attributes(this.attributes)
                           .eventFilter(eventServiceFilterEntity != null ? eventServiceFilterEntity.toPojo() : null)
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
