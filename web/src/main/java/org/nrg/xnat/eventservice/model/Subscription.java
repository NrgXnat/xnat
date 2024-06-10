package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;


@AutoValue
public abstract class Subscription {

    @Nullable @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @Nullable @JsonProperty("active") public abstract Boolean active();
    @JsonIgnore @Nullable public abstract String customListenerId();
    @JsonProperty("action-key") public abstract String actionKey();
    @Nullable @JsonProperty("attributes") public abstract Map<String, String> attributes();
    @JsonProperty("event-filter") public abstract EventFilter eventFilter();
    @Nullable @JsonProperty("act-as-event-user") public abstract Boolean actAsEventUser();
    @Nullable @JsonProperty("subscription-owner") public abstract String subscriptionOwner();
    @Nullable @JsonProperty("valid") public abstract Boolean valid();
    @Nullable @JsonProperty("validation-message") public abstract String validationMessage();

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss z",
            timezone = "UTC")
    @Nullable @JsonProperty("created") public abstract Date created();


    public static Builder builder() {
        return new AutoValue_Subscription.Builder();
    }

    public abstract Builder toBuilder();

    public Subscription setInvalid(String message) {
        return this.toBuilder().valid(false).validationMessage(message).build();
   }

    public Subscription setValid() {
        return this.toBuilder().valid(true).validationMessage("").build();
    }

    public static Subscription create(Long id, String name, Boolean active,
                                      String customListenerId, String actionKey, Map<String, String> attributes,
                                      EventFilter eventFilter, Boolean actAsEventUser, String subscriptionOwner,
                                      Boolean valid, String validationMessage) {
        return builder()
                .id(id)
                .name(name)
                .active(active)
                .customListenerId(customListenerId)
                .actionKey(actionKey)
                .attributes(attributes)
                .eventFilter(eventFilter)
                .actAsEventUser(actAsEventUser)
                .subscriptionOwner(subscriptionOwner)
                .valid(valid)
                .validationMessage(validationMessage)
                .build();
    }

    @JsonCreator
    public static Subscription create(@Nullable @JsonProperty("id")     Long id,
                                      @Nullable @JsonProperty("name")   String name,
                                      @Nullable @JsonProperty("active") Boolean active,
                                      @JsonProperty("action-key")       String actionKey,
                                      @Nullable @JsonProperty("attributes") Map<String, String> attributes,
                                      @JsonProperty("event-filter")     EventFilter eventFilter,
                                      @Nullable @JsonProperty("act-as-event-user") Boolean actAsEventUser) {
        return builder()
                .id(id)
                .name(name)
                .active(active)
                .customListenerId(null)
                .actionKey(actionKey)
                .attributes(attributes)
                .eventFilter(eventFilter)
                .actAsEventUser(actAsEventUser)
                .subscriptionOwner(null)
                .valid(null)
                .validationMessage(null)
                .build();
    }

    @Deprecated
    public static Subscription create(final SubscriptionCreator creator) {
        EventFilter filter = EventFilter.create(creator.eventFilter());
        return builder()
                .name(creator.name())
                .active(creator.active())
                .customListenerId(creator.customListenerId())
                .actionKey(creator.actionKey())
                .attributes(creator.attributes())
                .eventFilter(filter)
                .actAsEventUser(creator.actAsEventUser())
                .build();
    }

    public static Subscription create(final SubscriptionCreator creator, final String subscriptionOwner) {
        // Support projectIds, eventType, and status in either subscription creator or filter
        EventFilter filter = EventFilter.create(creator.eventFilter());
        return builder()
                .name(creator.name())
                .active(creator.active())
                .customListenerId(creator.customListenerId())
                .actionKey(creator.actionKey())
                .attributes(creator.attributes())
                .eventFilter(filter)
                .actAsEventUser(creator.actAsEventUser())
                .subscriptionOwner(subscriptionOwner)
                .build();

    }


    public static Subscription createOnProject(final ProjectSubscriptionCreator creator, final String subscriptionOwner) {
        // Support projectIds, eventType, and status in either subscription creator or filter
        EventFilter filter = EventFilter.create(creator.eventFilter());
        return builder()
                .name(creator.name())
                .active(creator.active())
                .customListenerId(creator.customListenerId())
                .actionKey(creator.actionKey())
                .attributes(creator.attributes())
                .eventFilter(filter)
                .actAsEventUser(false)
                .subscriptionOwner(subscriptionOwner)
                .build();
    }

    public Subscription update(final SubscriptionUpdate update){
        return this.toBuilder()
            .name(update.name() != null ? update.name() : name())
            .active(update.active() != null ? update.active() : active())
            .customListenerId(update.customListenerId() != null ? update.customListenerId() : customListenerId())
            .actionKey(update.actionKey() != null ? update.actionKey() : actionKey())
            .attributes(update.attributes() != null ? update.attributes() : attributes())
            .eventFilter(update.eventFilter() != null ? this.eventFilter().update(update.eventFilter()) : eventFilter())
            .actAsEventUser(update.actAsEventUser() != null ? update.actAsEventUser() : actAsEventUser())
            .build();

    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder name(String name);

        public abstract Builder active(Boolean active);

        public abstract Builder customListenerId(String listenerId);

        public abstract Builder actionKey(String actionKey);

        public abstract Builder attributes(Map<String, String> attributes);

        public abstract Builder eventFilter(EventFilter eventFilter);

        public abstract Builder actAsEventUser(Boolean actAsEventUser);

        public abstract Builder subscriptionOwner(String user);

        public abstract Builder valid(Boolean valid);

        public abstract Builder validationMessage(String validationMessage);

        public abstract Builder created(Date created);

        public abstract Subscription build();
    }

}
