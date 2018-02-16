package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.nrg.xnat.eventservice.events.EventServiceEvent;

import javax.annotation.Nullable;

@AutoValue
public abstract class TriggeringEvent {

    @JsonProperty("event-name")      public abstract String eventName();
    @JsonProperty("is-xsi-type")     public abstract boolean isXsiType();
    @JsonProperty("xnat-type")       public abstract String xnatType();
    @Nullable @JsonProperty("URI")   public abstract String xsiUri();
    @Nullable @JsonProperty("label") public abstract String objectLabel();

    public static TriggeringEvent create(@JsonProperty("event-name")      String eventName,
                                         @JsonProperty("is-xsi-type")     boolean isXsiType,
                                         @JsonProperty("xnat-type")       String xnatType,
                                         @Nullable @JsonProperty("URI")   String xsiUri,
                                         @Nullable @JsonProperty("label") String objectLabel) {
        return builder()
                .eventName(eventName)
                .isXsiType(isXsiType)
                .xnatType(xnatType)
                .xsiUri(xsiUri)
                .objectLabel(objectLabel)
                .build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_TriggeringEvent.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder eventName(String eventName);

        public abstract Builder isXsiType(boolean isXsiType);

        public abstract Builder xnatType(String xnatType);

        public abstract Builder xsiUri(String xsiUri);

        public abstract Builder objectLabel(String objectLabel);

        public abstract TriggeringEvent build();
    }
}
