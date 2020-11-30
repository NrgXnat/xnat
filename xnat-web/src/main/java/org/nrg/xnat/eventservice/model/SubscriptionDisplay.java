package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;


@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class SubscriptionDisplay {

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
    @Nullable @JsonProperty("editable") public abstract Boolean editable();

    public static Builder builder() {
        return new AutoValue_SubscriptionDisplay.Builder();
    }

    public abstract SubscriptionDisplay.Builder toBuilder();

    public static SubscriptionDisplay create(final Subscription subscription, final Boolean editable) {
        return builder()
                .id(subscription.id())
                .name(subscription.name())
                .active(subscription.active())
                .customListenerId(subscription.customListenerId())
                .actionKey(subscription.actionKey())
                .attributes(subscription.attributes())
                .eventFilter(subscription.eventFilter())
                .actAsEventUser(subscription.actAsEventUser())
                .subscriptionOwner(subscription.subscriptionOwner())
                .valid(subscription.valid())
                .validationMessage(subscription.validationMessage())
                .created(subscription.created())
                .editable(editable != null ? editable : true)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SubscriptionDisplay.Builder id(Long id);
        public abstract SubscriptionDisplay.Builder name(String name);
        public abstract SubscriptionDisplay.Builder active(Boolean active);
        public abstract SubscriptionDisplay.Builder customListenerId(String listenerId);
        public abstract SubscriptionDisplay.Builder actionKey(String actionKey);
        public abstract SubscriptionDisplay.Builder attributes(Map<String, String> attributes);
        public abstract SubscriptionDisplay.Builder eventFilter(EventFilter eventFilter);
        public abstract SubscriptionDisplay.Builder actAsEventUser(Boolean actAsEventUser);
        public abstract SubscriptionDisplay.Builder subscriptionOwner(String user);
        public abstract SubscriptionDisplay.Builder valid(Boolean valid);
        public abstract SubscriptionDisplay.Builder validationMessage(String validationMessage);
        public abstract SubscriptionDisplay.Builder created(Date created);
        public abstract SubscriptionDisplay.Builder editable(Boolean editable);
        public abstract SubscriptionDisplay build();
    }



}