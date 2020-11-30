package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class EventFilterCreator {
    @Nullable @JsonProperty("name") public abstract String name();

    // ** Reactor bus filter components ** //
    @JsonProperty("event-type")             public abstract String eventType();
    @Nullable @JsonProperty("project-ids")  public abstract List<String> projectIds();
    @Nullable @JsonProperty("status")       public abstract String status();

    // ** Post-detection filter components ** //
    @Nullable @JsonProperty("payload-filter") public abstract String jsonPathFilter();
    @Nullable @JsonProperty("filter-nodes")     public abstract Map<String, JsonPathFilterNode> nodeFilters();



    public abstract Builder toBuilder();

    @JsonCreator
    public static EventFilterCreator create(
            @Nullable @JsonProperty("name")        String name,
            @JsonProperty("event-type")            String eventType,
            @Nullable @JsonProperty("project-ids") List<String> projectIds,
            @Nullable @JsonProperty("status")      String status,
            @Nullable @JsonProperty("payload-filter") String jsonPathFilter,
            @Nullable @JsonProperty("filter-nodes")     Map<String, JsonPathFilterNode> nodeFilters) {
        return builder()
                .name(name)
                .eventType(eventType)
                .projectIds(projectIds)
                .status(status)
                .jsonPathFilter(jsonPathFilter)
                .nodeFilters(nodeFilters)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventFilterCreator.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder eventType(String eventType);

        public abstract Builder projectIds(List<String> projectIds);

        public abstract Builder status(String status);

        public abstract Builder jsonPathFilter(String jsonPathFilter);

        public abstract Builder nodeFilters(Map<String, JsonPathFilterNode> nodeFilters);

        public abstract EventFilterCreator build();
    }
}
