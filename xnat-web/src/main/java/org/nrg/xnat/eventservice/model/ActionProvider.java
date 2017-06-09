package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ActionProvider {

    @JsonProperty("name") public abstract String className();
    @Nullable
    @JsonProperty("display-name") public abstract String displayName();
    @Nullable
    @JsonProperty("description") public abstract String description();
    @Nullable
    @JsonProperty("actions") public abstract List<Action> actions();
    @Nullable
    @JsonProperty("events") public abstract List<String> events();

    public static ActionProvider create(@JsonProperty("name") final String className,
                                        @JsonProperty("displayName") final String displayName,
                                        @JsonProperty("description") final String description,
                                        @JsonProperty("actions") final List<Action> actions,
                                        @JsonProperty("events") final List<String> events) {
        return builder()
                .className(className)
                .displayName(displayName)
                .description(description)
                .actions(actions)
                .events(events)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActionProvider.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder className(String name);

        public abstract Builder displayName(String displayName);

        public abstract Builder description(String description);

        public abstract Builder actions(List<Action> actions);

        public abstract Builder events(List<String> events);

        public abstract ActionProvider build();
    }
}
