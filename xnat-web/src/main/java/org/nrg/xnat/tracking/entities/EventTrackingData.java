package org.nrg.xnat.tracking.entities;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nullable;
import javax.persistence.*;

@Slf4j
@Entity
public class EventTrackingData extends AbstractHibernateEntity {
    private String key;
    @Nullable private String payload;
    @Nullable private Boolean succeeded;
    @Nullable private String finalMessage;
    private Integer userId;

    public EventTrackingData() {}

    public EventTrackingData(String key, Integer userId) {
        this.key = key;
        this.userId = userId;
    }

    @Column(columnDefinition = "TEXT")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Nullable
    @Column(columnDefinition = "TEXT")
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
    @Column(columnDefinition = "TEXT")
    public String getFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(@Nullable String finalMessage) {
        this.finalMessage = finalMessage;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public EventTrackingDataPojo toPojo() {
        return new EventTrackingDataPojo(key, payload, succeeded, finalMessage);
    }
}
