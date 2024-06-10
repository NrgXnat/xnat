package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ActionAttributeConfiguration {

    @Nullable @JsonProperty("description")   public abstract String description();
    @Nullable @JsonProperty("type")          public abstract String type();
    @Nullable @JsonProperty("default-value") public abstract String defaultValue();
    @Nullable @JsonProperty("required")      public abstract Boolean required();
    @Nullable @JsonProperty("user-settable") public abstract Boolean userSettable();
    @Nullable @JsonProperty("restrict-to-list") public abstract Map<String, List<AttributeContextValue>> restrictTo();

    @JsonCreator
    public static ActionAttributeConfiguration create(@JsonProperty("description")   String description,
                                                      @JsonProperty("type")          String type,
                                                      @JsonProperty("default-value") String defaultValue,
                                                      @JsonProperty("required")      Boolean required,
                                                      @JsonProperty("user-settable") Boolean userSettable,
                                                      @JsonProperty("restrict-to-list") Map<String, List<AttributeContextValue>> restrictTo){
        return builder()
                .description(description)
                .type(type)
                .defaultValue(defaultValue)
                .required(required)
                .userSettable(userSettable)
                .restrictTo(restrictTo)
                .build();
    }

    public static Builder builder() {return new AutoValue_ActionAttributeConfiguration.Builder();}

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder description(String description);

        public abstract Builder type(String type);

        public abstract Builder defaultValue(String defaultValue);

        public abstract Builder required(Boolean required);

        public abstract Builder userSettable(Boolean userSettable);

        public abstract Builder restrictTo(Map<String, List<AttributeContextValue>> restrictTo);

        public abstract ActionAttributeConfiguration build();

    }


    @AutoValue
    public static abstract class AttributeContextValue {
        @Nullable @JsonProperty("type") public abstract String type();
        @Nullable @JsonProperty("label") public abstract String label();
        @Nullable @JsonProperty("value") public abstract String value();

        public static AttributeContextValue create(String type, String label, String value) {
            return builder()
                    .type(type)
                    .label(label)
                    .value(value)
                    .build();
        }

        public static Builder builder() {return new AutoValue_ActionAttributeConfiguration_AttributeContextValue.Builder();}

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder type(String type);

            public abstract Builder label(String label);

            public abstract Builder value(String value);

            public abstract AttributeContextValue build();
        }
    }

}
