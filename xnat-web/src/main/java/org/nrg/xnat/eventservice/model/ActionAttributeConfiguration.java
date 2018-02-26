package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class ActionAttributeConfiguration {

    @Nullable @JsonProperty("description")   public abstract String description();
    @Nullable @JsonProperty("type")          public abstract String type();
    @Nullable @JsonProperty("default-value") public abstract String defaultValue();
    @Nullable @JsonProperty("required")      public abstract Boolean required();

    public static ActionAttributeConfiguration create(@JsonProperty("description")   String description,
                                                      @JsonProperty("type")          String type,
                                                      @JsonProperty("default-value") String defaultValue,
                                                      @JsonProperty("required")      Boolean required) {
        return builder()
                .description(description)
                .type(type)
                .defaultValue(defaultValue)
                .required(required)
                .build();
    }

    public static Builder builder() {return new AutoValue_ActionAttributeConfiguration.Builder();}

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder description(String description);

        public abstract Builder type(String type);

        public abstract Builder defaultValue(String defaultValue);

        public abstract Builder required(Boolean required);

        public abstract ActionAttributeConfiguration build();
    }
}
