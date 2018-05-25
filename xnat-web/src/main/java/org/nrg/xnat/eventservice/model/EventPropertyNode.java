package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class EventPropertyNode {
    @JsonProperty("name") public abstract String  name();
    @JsonProperty("replacement-key") public abstract String  replacementKey();
    @JsonProperty("type") public abstract String  type();
    @Nullable @JsonProperty("value") public abstract String  value();

    abstract Builder toBuilder();

    public static EventPropertyNode withName(@Nonnull String name, String type){
        return EventPropertyNode.builder().name(name).replacementKey("#" + name + "#").type(type).build();
    }

    public EventPropertyNode withValue(@Nonnull String value){
        return this.toBuilder().value(value).build();

    }

    private static Builder builder() {return new AutoValue_EventPropertyNode.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder name(String name);

        public abstract Builder replacementKey(String replacementKey);

        public abstract Builder value(String value);

        public abstract Builder type(String type);

        public abstract EventPropertyNode build();
    }
}

