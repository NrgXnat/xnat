package org.nrg.xnat.tracking.entities;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;

@JsonInclude
public class EventTrackingDataPojo {
    private String key;
    @Nullable private String payload;
    @Nullable private Boolean succeeded;
    @Nullable private String finalMessage;

    public EventTrackingDataPojo() {}

    public EventTrackingDataPojo(String key) {
        this.key = key;
    }

    public EventTrackingDataPojo(String key,
                                 @Nullable String payload,
                                 @Nullable Boolean succeeded,
                                 @Nullable String finalMessage) {
        this(key);
        this.payload = payload;
        this.succeeded = succeeded;
        this.finalMessage = finalMessage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    public void setPayload(@Nullable String payload) {
        this.payload = payload;
    }

    @Nullable
    public Boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(@Nullable Boolean succeeded) {
        this.succeeded = succeeded;
    }

    @Nullable
    public String getFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(@Nullable String finalMessage) {
        this.finalMessage = finalMessage;
    }
}
