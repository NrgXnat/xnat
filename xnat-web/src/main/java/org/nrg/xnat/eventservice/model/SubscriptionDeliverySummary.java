package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Date;


@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class SubscriptionDeliverySummary {

    @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("event-name") public abstract String eventName();
    @Nullable @JsonProperty("subscription-name") public abstract String subscriptionName();
    @Nullable @JsonProperty("user") public abstract String actionUser();
    @Nullable @JsonProperty("project") public abstract String projectId();
    @Nullable @JsonProperty("trigger-label") public abstract String triggerLabel();
    @Nullable @JsonProperty("status") public abstract String status();
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss z",
            timezone = "UTC")
    @Nullable @JsonProperty("timestamp")  public abstract Date timestamp();



    public abstract SubscriptionDeliverySummary.Builder toBuilder();

    public static SubscriptionDeliverySummary create(Long id, String eventName, String subscriptionName, String actionUser,
                                                     String projectId, String triggerLabel, String status,
                                                     Date timestamp) {
        return builder()
                .id(id)
                .eventName(eventName)
                .subscriptionName(subscriptionName)
                .actionUser(actionUser)
                .projectId(projectId)
                .triggerLabel(triggerLabel)
                .status(status)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {return new AutoValue_SubscriptionDeliverySummary.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder eventName(String eventName);

        public abstract Builder subscriptionName(String subscriptionName);

        public abstract Builder actionUser(String actionUser);

        public abstract Builder projectId(String projectId);

        public abstract Builder triggerLabel(String triggerLabel);

        public abstract Builder status(String status);

        public abstract Builder timestamp(Date timestamp);

        public abstract SubscriptionDeliverySummary build();
    }
}

