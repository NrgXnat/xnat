package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Map;

@AutoValue
public abstract class EventFilter {

    @JsonIgnore @Nullable @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @Nullable @JsonProperty("json-path-filter") public abstract String jsonPathFilter();
    @Nullable @JsonProperty("node-filters") public abstract Map<String, JsonPathFilterNode> nodeFilters();

    public static Builder builder() {
        return new AutoValue_EventFilter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder jsonPathFilter(String jsonPathFilter);

        public abstract Builder nodeFilters(Map<String, JsonPathFilterNode> nodeFilters);

        public abstract Builder name(String name);

        public abstract EventFilter build();


    }

    public void populateJsonPathFilter() {
        // TODO: construct JSONPath filter from nodeFilters
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
