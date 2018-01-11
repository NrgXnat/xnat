package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.nrg.xft.security.UserI;

import javax.annotation.Nullable;
import java.util.Map;


@AutoValue
public abstract class Subscription {

    @Nullable @JsonProperty("id") public abstract Long id();
    @JsonProperty("name") public abstract String name();
    @Nullable @JsonProperty("active") public abstract Boolean active();
    @Nullable @JsonProperty("registration-key") public abstract  String listenerRegistrationKey();
    @JsonProperty("event-id") public abstract String eventId();
    @Nullable @JsonProperty("custom-listener-id") public abstract String customListenerId();
    @JsonProperty("action-key") public abstract String actionKey();
    @Nullable @JsonProperty("attributes") public abstract Map<String, String> attributes();
    @Nullable @JsonProperty("event-filter") public abstract EventFilter eventFilter();
    @Nullable @JsonProperty("act-as-event-user") public abstract Boolean actAsEventUser();
    @Nullable @JsonProperty("subscription-owner") public abstract String subscriptionOwner();
    @Nullable @JsonProperty("use-counter") public abstract Integer useCounter();
    @Nullable @JsonProperty("valid") public abstract Boolean valid();
    @Nullable @JsonProperty("validation-message") public abstract String validationMessage();


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

    @JsonCreator
    public static Subscription create(@JsonProperty("id") final Long id,
                                      @JsonProperty("name") final String name,
                                      @JsonProperty("active") final Boolean active,
                                      @JsonProperty("registration-key") final String listenerRegistrationKey,
                                      @JsonProperty("event-id") final String eventId,
                                      @Nullable @JsonProperty("custom-listener-id") String customListenerId,
                                      @JsonProperty("action-key") final String actionKey,
                                      @JsonProperty("attributes") final Map<String, String> attributes,
                                      @JsonProperty("eventId-filter") final EventFilter eventFilter,
                                      @JsonProperty("act-as-eventId-user") final Boolean actAsEventUser,
                                      @JsonProperty("subscription-owner") final String subscriptionOwner) {
        return builder()
                .id(id)
                .name(name)
                .active(active)
                .listenerRegistrationKey(listenerRegistrationKey)
                .eventId(eventId)
                .customListenerId(customListenerId)
                .actionKey(actionKey)
                .attributes(attributes)
                .eventFilter(eventFilter)
                .actAsEventUser(actAsEventUser)
                .subscriptionOwner(subscriptionOwner)
                .build();
    }

    @Deprecated
    public static Subscription create(final SubscriptionCreator creator) {
        return builder()
                .name(creator.name())
                .active(creator.active())
                .eventId(creator.eventId())
                .customListenerId(creator.customListenerId())
                .actionKey(creator.actionKey())
                .attributes(creator.attributes())
                .eventFilter(creator.eventFilter())
                .actAsEventUser(creator.actAsEventUser())
                .build();
    }

    public static Subscription create(final SubscriptionCreator creator, final String subscriptionOwner) {
        return builder()
                .name(creator.name())
                .active(creator.active())
                .eventId(creator.eventId())
                .customListenerId(creator.customListenerId())
                .actionKey(creator.actionKey())
                .attributes(creator.attributes())
                .eventFilter(creator.eventFilter())
                .actAsEventUser(creator.actAsEventUser())
                .subscriptionOwner(subscriptionOwner)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder name(String name);

        public abstract Builder listenerRegistrationKey(String listenerRegistrationKey);

        public abstract Builder active(Boolean active);

        public abstract Builder eventId(String eventId);

        public abstract Builder customListenerId(String listenerId);

        public abstract Builder actionKey(String actionKey);

        public abstract Builder attributes(Map<String, String> attributes);

        public abstract Builder eventFilter(EventFilter eventFilter);

        public abstract Builder actAsEventUser(Boolean actAsEventUser);

        public abstract Builder subscriptionOwner(String user);

        public abstract Builder useCounter(Integer useCounter);

        public abstract Builder valid(Boolean valid);

        public abstract Builder validationMessage(String validationMessage);

        public abstract Subscription build();
    }

}
