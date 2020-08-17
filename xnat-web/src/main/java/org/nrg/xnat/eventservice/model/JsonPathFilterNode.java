package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Null;

@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class JsonPathFilterNode {

    @Nullable @JsonProperty("value")          public abstract String  value();
    @Nullable @JsonProperty(value = "match-case", defaultValue = "false")     public abstract Boolean matchCase();
    @Nullable @JsonProperty(value = "match-word", defaultValue = "false")     public abstract Boolean matchWord();
    @Nullable @JsonProperty(value = "match-null-value", defaultValue = "false") public abstract Boolean matchNullValue();
    @JsonProperty("value-type")               public abstract String  type();
    @Nullable @JsonProperty("sample-value")   public abstract String  sampleValue();

    public abstract Builder toBuilder();

    public static JsonPathFilterNode create(@Nullable @JsonProperty("value")          String value,
                                            @Nullable @JsonProperty("match-case")     Boolean matchCase,
                                            @Nullable @JsonProperty("match-word")     Boolean matchWord,
                                            @Nullable @JsonProperty("match-null-value") Boolean matchNullValue,
                                            @JsonProperty("value-type")               String type,
                                            @Nullable @JsonProperty("sample-value")   String sampleValue) {
        return builder()
                .value(value)
                .matchCase(matchCase)
                .matchWord(matchWord)
                .matchNullValue(matchNullValue)
                .type(type)
                .sampleValue(sampleValue)
                .build();
    }

    public static Builder builder() {return new AutoValue_JsonPathFilterNode.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder value(String value);

        public abstract Builder matchCase(Boolean matchCase);

        public abstract Builder matchWord(Boolean matchWord);

        public abstract Builder matchNullValue(Boolean matchNullValue);

        public abstract Builder type(String type);

        public abstract Builder sampleValue(String sampleValue);

        public abstract JsonPathFilterNode build();
    }
}

