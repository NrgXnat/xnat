package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class EventFilter {

    @JsonIgnore @Nullable @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @Nullable @JsonProperty("json-path-filter") public abstract String jsonPathFilter();

    public static Builder builder() {
        return new AutoValue_EventFilter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder jsonPathFilter(String jsonPathFilter);

        public abstract Builder name(String name);

        public abstract EventFilter build();


    }


    public String toRegexMatcher(String eventType, String projectId) {
        String pattern = ".*(?:" + eventType + ")";
        pattern += "__";
        pattern += "project-id:";
        if (!Strings.isNullOrEmpty(projectId)) {
            pattern += ".*(?:" + projectId + ")";
        } else {
            pattern += ".*";
        }
        return pattern;
    }

    public String toRegexKey(String eventType, String projectId) {
        String pattern = "event-type:" + eventType;
        pattern += "__";
        pattern += "project-id:";
        if (!Strings.isNullOrEmpty(projectId)) {
            pattern += projectId;
        }
        return pattern;
    }

    @JsonCreator
    public static EventFilter create(@JsonProperty("id") final Long id,
                                     @JsonProperty("name") final String name,
                                     @JsonProperty("json-path-filter") final String jsonPathFilter) {
        return builder()
                .id(id)
                .name(name)
                .jsonPathFilter(jsonPathFilter)
                .build();
    }
}
