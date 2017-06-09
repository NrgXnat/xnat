package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Listener {

    @JsonProperty("class-name") public abstract String className();

    public static Listener create(String className) {
        return builder()
                .className(className)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Listener.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder className(String className);

        public abstract Listener build();
    }
}
