package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ActionFilter {
    @Nullable @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @JsonProperty("events") public abstract ImmutableList<String> events();
    @JsonProperty("parameters") public abstract ImmutableMap<String, String> parameters();

    @JsonCreator
    public static ActionFilter create(@JsonProperty("id") final Long id,
                                      @JsonProperty("name") final String name,
                                      @JsonProperty("events") final List<String> events,
                                      @JsonProperty("parameters") final Map<String, String> parameters) {
        return builder()
                .id(id)
                .name(name)
                .events(events == null ? Collections.<String>emptyList() : events)
                .parameters(parameters == null ? Collections.<String, String>emptyMap() : parameters)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActionFilter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder events(List<String> events);

        public abstract Builder parameters(Map<String, String> parameters);

        public abstract Builder name(String name);

        public abstract ActionFilter build();
    }
}
