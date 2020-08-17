package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;

import javax.annotation.Nullable;


@AutoValue
public abstract class EventSignature {
    @JsonProperty("event-type")   public abstract String eventType();
    @Nullable @JsonProperty("project-id") public abstract String projectId();
    @Nullable @JsonProperty("status")     public abstract String status();
    @Nullable @JsonProperty("payload")    public abstract Object payload();



    public abstract Builder toBuilder();

    public static EventSignature create(@JsonProperty("event-type")   String eventType,
                                        @Nullable @JsonProperty("project-id") String projectId,
                                        @JsonProperty("status")     String status,
                                        @JsonProperty("payload")    Object payload) {
        return builder()
                .eventType(eventType)
                .projectId(projectId)
                .status(status)
                .payload(payload)
                .build();
    }

    public static Builder builder() {return new AutoValue_EventSignature.Builder();}

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder eventType(String eventType);

        public abstract Builder projectId(String projectId);

        public abstract Builder status(String status);

        public abstract Builder payload(Object payload);

        public abstract EventSignature build();
    }

}
