package org.nrg.xnat.eventservice.entities;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;
import java.util.Set;


@Entity
@Table

public class SubscriptionEntity extends AbstractHibernateEntity {

    public SubscriptionEntity() {}


    private String name;
    private Boolean active;
    private String customListenerId;
    private String actionKey;
    private Map<String,String> attributes;
    private EventServiceFilterEntity eventServiceFilterEntity;
    private Boolean actAsEventUser;
    private String subscriptionOwner;
    private Set<SubscriptionDeliveryEntity> subscriptionDeliveryEntities;




    public SubscriptionEntity(String name, Boolean active,
                              String customListenerId, String actionKey,
                              Map<String, String> attributes,
                              EventServiceFilterEntity eventServiceFilterEntity, Boolean actAsEventUser,
                              String subscriptionOwner,
                              Set<SubscriptionDeliveryEntity> subscriptionDeliveryEntities) {
        this.name = name;
        this.active = active;
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
        if (!(o instanceof SubscriptionEntity)) return false;
        if (!super.equals(o)) return false;
        SubscriptionEntity that = (SubscriptionEntity) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(active, that.active) &&
                Objects.equal(customListenerId, that.customListenerId) &&
                Objects.equal(actionKey, that.actionKey) &&
                Objects.equal(attributes, that.attributes) &&
                Objects.equal(eventServiceFilterEntity, that.eventServiceFilterEntity) &&
                Objects.equal(actAsEventUser, that.actAsEventUser) &&
                Objects.equal(subscriptionOwner, that.subscriptionOwner) &&
                Objects.equal(subscriptionDeliveryEntities, that.subscriptionDeliveryEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, active, customListenerId, actionKey, attributes, eventServiceFilterEntity, actAsEventUser, subscriptionOwner, subscriptionDeliveryEntities);
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", customListenerId='" + customListenerId + '\'' +
                ", actionKey='" + actionKey + '\'' +
                ", attributes=" + attributes +
                ", eventServiceFilterEntity=" + eventServiceFilterEntity +
                ", actAsEventUser=" + actAsEventUser +
                ", subscriptionOwner='" + subscriptionOwner + '\'' +
                ", subscriptionDeliveryEntities=" + subscriptionDeliveryEntities +

                '}';
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
    public Set<SubscriptionDeliveryEntity> getSubscriptionDeliveryEntities() {
        return subscriptionDeliveryEntities;
    }

    public void setSubscriptionDeliveryEntities(
            Set<SubscriptionDeliveryEntity> subscriptionDeliveryEntities) {
        this.subscriptionDeliveryEntities = subscriptionDeliveryEntities;
    }

    @Transient
    public static SubscriptionEntity fromPojo(final Subscription subscription) {
        return fromPojoWithTemplate(subscription, new SubscriptionEntity());
    }

    @Transient
    public static SubscriptionEntity fromPojoWithTemplate(final Subscription subscription, final SubscriptionEntity template) {
        if(template==null) {
            return fromPojo(subscription);
        }
        if(subscription==null){
            return null;
        }
        template.name = subscription.name() != null ? subscription.name() : template.name;
        template.active = subscription.active() != null ? subscription.active() : (template.active == null ? true : template.active);
        template.customListenerId = subscription.customListenerId() != null ? subscription.customListenerId() : template.customListenerId;
        template.actionKey = subscription.actionKey() != null ? subscription.actionKey() : template.actionKey;
        template.attributes = subscription.attributes() != null ? subscription.attributes() : template.attributes;
        template.eventServiceFilterEntity = subscription.eventFilter() != null ? EventServiceFilterEntity.fromPojo(subscription.eventFilter()) : template.eventServiceFilterEntity;
        template.actAsEventUser = subscription.actAsEventUser() != null ? subscription.actAsEventUser() : template.actAsEventUser;
        template.subscriptionOwner = subscription.subscriptionOwner() != null ? subscription.subscriptionOwner() : template.subscriptionOwner;

        return template;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
