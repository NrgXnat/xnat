package org.nrg.xnat.eventservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.nrg.xnat.eventservice.services.EventServicePrefsBean;

import javax.annotation.Nullable;


@AutoValue
public abstract class EventServicePrefs {
    @Nullable @JsonProperty("enabled")  public abstract Boolean enabled();

    @JsonCreator
    public static EventServicePrefs create(@Nullable @JsonProperty("enabled") Boolean enabled){
        return builder()
                .enabled(enabled)
                .build();
    }

    public static EventServicePrefs create(EventServicePrefsBean prefsBean){
        return builder()
                .enabled(prefsBean.getEnabled())
                .build();

    }

    public static Builder builder() {return new AutoValue_EventServicePrefs.Builder();}


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder enabled(Boolean enabled);

        public abstract EventServicePrefs build();
    }
}
