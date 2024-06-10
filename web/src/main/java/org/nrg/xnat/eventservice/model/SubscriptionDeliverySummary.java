package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;


@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class SubscriptionDeliverySummary {

    @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("event-name") public abstract String eventName();
    @Nullable @JsonProperty("subscription-id") public abstract Long subscriptionId();
    @Nullable @JsonProperty("subscription-name") public abstract String subscriptionName();
    @Nullable @JsonProperty("user") public abstract String actionUser();
    @Nullable @JsonProperty("project") public abstract String projectId();
    @Nullable @JsonProperty("trigger-label") public abstract String triggerLabel();
    @Nullable @JsonProperty("status") public abstract String status();
    @Nullable @JsonProperty(value = "error", defaultValue = "false") public abstract Boolean errorState();
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss z",
            timezone = "UTC")
    @Nullable @JsonProperty("timestamp")  public abstract Date timestamp();



    public abstract SubscriptionDeliverySummary.Builder toBuilder();

    public static SubscriptionDeliverySummary create(Long id, String eventName, Long subscriptionId, String subscriptionName, String actionUser,
                                                     String projectId, String triggerLabel, String status, Boolean errorState,
                                                     Date timestamp) {
        return builder()
                .id(id)
                .eventName(eventName)
                .subscriptionId(subscriptionId)
                .subscriptionName(subscriptionName)
                .actionUser(actionUser)
                .projectId(projectId)
                .triggerLabel(triggerLabel)
                .status(status)
                .errorState(errorState)
                .timestamp(timestamp)
                .build();
    }

    public static SubscriptionDeliverySummary create(@Nonnull SubscriptionDeliverySummaryEntity summaryEntity){
        return builder()
                .id(summaryEntity.getId())
                .eventName(summaryEntity.getEventName())
                .subscriptionId(summaryEntity.getSubscriptionId())
                .subscriptionName(summaryEntity.getSubscriptionName())
                .actionUser(summaryEntity.getActionUser())
                .projectId(summaryEntity.getProjectId())
                .triggerLabel(summaryEntity.getTriggerLabel())
                .status(summaryEntity.getStatus() != null ? summaryEntity.getStatus().name() : null)
                .errorState(summaryEntity.getErrorState())
                .timestamp(summaryEntity.getTimestamp())
                .build();
    }

    public static Builder builder() {return new AutoValue_SubscriptionDeliverySummary.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder eventName(String eventName);

        public abstract Builder subscriptionId(Long subscriptionId);

        public abstract Builder subscriptionName(String subscriptionName);

        public abstract Builder actionUser(String actionUser);

        public abstract Builder projectId(String projectId);

        public abstract Builder triggerLabel(String triggerLabel);

        public abstract Builder status(String status);

        public abstract Builder errorState(Boolean errorState);

        public abstract Builder timestamp(Date timestamp);

        public abstract SubscriptionDeliverySummary build();
    }
}

