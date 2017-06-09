package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class EventFilter {

    @JsonIgnore @Nullable @JsonProperty("id") public abstract Long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @JsonProperty("project-ids") public abstract ImmutableList<String> projectIds();
    @Nullable @JsonProperty("json-path-filter") public abstract String jsonPathFilter();

    public static Builder builder() {
        return new AutoValue_EventFilter.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder projectIds(@Nonnull List<String> projectIds);

        public abstract Builder jsonPathFilter(String jsonPathFilter);

        abstract ImmutableList.Builder<String> projectIdsBuilder();

        public Builder addProjectId(final @Nonnull String projectId) {
            projectIdsBuilder().add(projectId);
            return this;
        }

        public abstract Builder name(String name);

        public abstract EventFilter build();


    }


    public String toRegexMatcher(String eventType) {
        String pattern = ".*(?:" + eventType + ")";
        if (projectIds() != null && !projectIds().isEmpty()) {
            pattern += ".*(?:" + Joiner.on('|').join(projectIds()) + ")";
        }
        return pattern;
    }

    public String toRegexKey(String eventType) {
        String pattern = "event-type:" + eventType;
        pattern += "__";
        pattern += "project-id:";
        if (projectIds() != null && !projectIds().isEmpty()) {
            pattern += projectIds().get(0);
        }
        return pattern;
    }

    @JsonCreator
    public static EventFilter create(@JsonProperty("id") final Long id,
                                     @JsonProperty("name") final String name,
                                     @JsonProperty("project-ids") final List<String> projectIds,
                                     @JsonProperty("json-path-filter") final String jsonPathFilter) {
        return builder()
                .id(id)
                .name(name)
                .projectIds(projectIds == null ? Collections.<String>emptyList() : projectIds)
                .jsonPathFilter(jsonPathFilter)
                .build();
    }
}
