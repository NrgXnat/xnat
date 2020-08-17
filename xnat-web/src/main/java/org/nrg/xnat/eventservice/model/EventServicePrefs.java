package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.nrg.xnat.eventservice.services.EventServicePrefsBean;

import javax.annotation.Nullable;


@AutoValue
public abstract class EventServicePrefs {
    @Nullable @JsonProperty("enabled")                     public abstract Boolean enabled();
    @Nullable @JsonProperty("respondToEvents")             public abstract Boolean respondToEvents();
    @Nullable @JsonProperty("triggerCoreEvents")           public abstract Boolean triggerCoreEvents();
    @Nullable @JsonProperty("triggerPluginEvents")         public abstract Boolean triggerPluginEvents();
    @Nullable @JsonProperty("triggerWorkflowStatusEvents") public abstract Boolean triggerWorkflowStatusEvents();


    @JsonCreator
    public static EventServicePrefs create(@Nullable @JsonProperty("enabled")                     Boolean enabled,
                                           @Nullable @JsonProperty("respondToEvents")             Boolean respondToEvents,
                                           @Nullable @JsonProperty("triggerCoreEvents")           Boolean triggerCoreEvents,
                                           @Nullable @JsonProperty("triggerPluginEvents")         Boolean triggerPluginEvents,
                                           @Nullable @JsonProperty("triggerWorkflowStatusEvents") Boolean triggerWorkflowStatusEvents) {
        return builder()
                .enabled(enabled)
                .respondToEvents(respondToEvents)
                .triggerCoreEvents(triggerCoreEvents)
                .triggerPluginEvents(triggerPluginEvents)
                .triggerWorkflowStatusEvents(triggerWorkflowStatusEvents)
                .build();
    }

    public static EventServicePrefs create(EventServicePrefsBean prefsBean){
        return builder()
                .enabled(prefsBean.getEnabled())
                .respondToEvents(prefsBean.getRespondToEvents())
                .triggerCoreEvents(prefsBean.getTriggerCoreEvents())
                .triggerPluginEvents(prefsBean.getTriggerPluginEvents())
                .triggerWorkflowStatusEvents(prefsBean.getTriggerWorkflowStatusEvents())
                .build();

    }

    public static Builder builder() {return new AutoValue_EventServicePrefs.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder enabled(Boolean enabled);

        public abstract Builder respondToEvents(Boolean respondToEvents);

        public abstract Builder triggerCoreEvents(Boolean triggerCoreEvents);

        public abstract Builder triggerPluginEvents(Boolean triggerPluginEvents);

        public abstract Builder triggerWorkflowStatusEvents(Boolean triggerWorkflowStatusEvents);

        public abstract EventServicePrefs build();
    }
}
