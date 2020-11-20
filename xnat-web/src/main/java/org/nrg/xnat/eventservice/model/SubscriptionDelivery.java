package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;


@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class SubscriptionDelivery {

    @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("event") public abstract SimpleEvent event();
    @Nullable @JsonProperty("event-type") public abstract String eventType();
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss z",
            timezone = "UTC")
    @Nullable @JsonProperty("timestamp")  public abstract Date timestamp();
    @Nullable @JsonProperty("subscription") public abstract Subscription subscription();
    @JsonProperty("user") public abstract String actionUser();
    @JsonProperty("project") public abstract String projectId();
    @JsonProperty("inputs") public abstract String actionInputs();
    @Nullable @JsonProperty("trigger") public abstract  TriggeringEvent triggeringEvent();
    @Nullable @JsonProperty("status") public abstract List<TimedEventStatus> timedEventStatuses();
    @Nullable @JsonProperty("status-message") public abstract String statusMessage();

    @JsonProperty(value = "error", defaultValue = "false") public abstract Boolean errorState();

    

    public static Builder builder() {
        return new AutoValue_SubscriptionDelivery.Builder();
    }

    public abstract SubscriptionDelivery.Builder toBuilder();

    public static SubscriptionDelivery create(Long id, SimpleEvent event, String eventType ,Subscription subscription,
                                              String actionUser, String projectId, String actionInputs,
                                              TriggeringEvent triggeringEvent, List<TimedEventStatus> timedEventStatuses,
                                              String statusMessage, Date timestamp, Boolean errorState) {
        return builder()
                .id(id)
                .event(event)
                .eventType(eventType)
                .subscription(subscription)
                .actionUser(actionUser)
                .projectId(projectId)
                .actionInputs(actionInputs)
                .triggeringEvent(triggeringEvent)
                .timedEventStatuses(timedEventStatuses)
                .statusMessage(statusMessage)
                .timestamp(timestamp)
                .errorState(errorState)
                .build();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder event(SimpleEvent event);

        public abstract Builder eventType(String eventType);

        public abstract Builder subscription(Subscription subscription);

        public abstract Builder actionUser(String actionUser);

        public abstract Builder projectId(String projectId);

        public abstract Builder actionInputs(String actionInputs);

        public abstract Builder triggeringEvent(TriggeringEvent triggeringEvent);

        public abstract Builder timedEventStatuses(List<TimedEventStatus> timedEventStatuses);

        public abstract Builder statusMessage(String statusMessage);

        public abstract Builder timestamp(Date timestamp);

        public abstract Builder errorState(Boolean errorState);

        public abstract SubscriptionDelivery build();
    }
}
