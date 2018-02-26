package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class JsonPathFilterNode {

    @Nullable @JsonProperty("value")        public abstract String value();
    @JsonProperty("type")                   public abstract String type();
    @Nullable @JsonProperty("sample-value") public abstract String sampleValue();
    @Nullable @JsonProperty("is-regex")     public abstract Boolean isRegex();

    public static JsonPathFilterNode create(@Nullable @JsonProperty("value")        String value,
                                            @JsonProperty("type")                   String type,
                                            @Nullable @JsonProperty("sample-value") String sampleValue,
                                            @Nullable @JsonProperty("is-regex")     Boolean isRegex) {
        return builder()
                .value(value)
                .type(type)
                .sampleValue(sampleValue)
                .isRegex(isRegex)
                .build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {return new AutoValue_JsonPathFilterNode.Builder();}

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder value(String value);

        public abstract Builder type(String type);

        public abstract Builder sampleValue(String sampleValue);

        public abstract Builder isRegex(Boolean isRegex);

        public abstract JsonPathFilterNode build();
    }
}
