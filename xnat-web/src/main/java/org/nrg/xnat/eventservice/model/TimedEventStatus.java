package org.nrg.xnat.eventservice.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Date;

@AutoValue
public abstract class TimedEventStatus {

    @JsonProperty("status")     public abstract String status();
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonProperty("timestamp")  public abstract Date   timestamp();
    @JsonProperty("message")    public abstract String message();

    public static Builder builder() {return new AutoValue_TimedEventStatus.Builder();}

    public static TimedEventStatus create(String status, Date timestamp, String message) {
        return builder()
                .status(status)
                .timestamp(timestamp)
                .message(message)
                .build();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder status(String status);

        public abstract Builder timestamp(Date timestamp);

        public abstract Builder message(String message);

        public abstract TimedEventStatus build();
    }
}
