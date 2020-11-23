package org.nrg.xnat.eventservice.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.eventservice.model.TimedEventStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Entity
public class TimedEventStatusEntity implements Serializable {

    public TimedEventStatusEntity() {
    }

    public TimedEventStatusEntity(Status status, Date statusTimestamp, String message, SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        setStatus(status);
        setStatusTimestamp(statusTimestamp);
        setMessage(message);
        setSubscriptionDeliveryEntity(subscriptionDeliveryEntity);
    }

    private long id;
    private Status status;
    private Date statusTimestamp;
    private String message;
    private SubscriptionDeliveryEntity subscriptionDeliveryEntity;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Integer MAX_TEXT_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Column(name = "status_timestamp")
    public Date getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(Date statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    @Column
    @Lob
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message != null ? StringUtils.left(message, MAX_TEXT_LENGTH) : null;
    }

    @ManyToOne
    public SubscriptionDeliveryEntity getSubscriptionDeliveryEntity() {
        return subscriptionDeliveryEntity;
    }

    public void setSubscriptionDeliveryEntity(SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        this.subscriptionDeliveryEntity = subscriptionDeliveryEntity;
    }

    @Override
    public String toString() {
        return "TimedEventStatus{" +
                "status=" + status +
                ", statusTimestamp=" + statusTimestamp +
                ", message='" + message + '\'' +
                ", subscriptionDeliveryEntity=" + subscriptionDeliveryEntity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TimedEventStatusEntity that = (TimedEventStatusEntity) o;
        return status == that.status &&
                Objects.equal(statusTimestamp, that.statusTimestamp) &&
                Objects.equal(message, that.message) &&
                Objects.equal(subscriptionDeliveryEntity, that.subscriptionDeliveryEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), status, statusTimestamp, message, subscriptionDeliveryEntity);
    }

    public TimedEventStatus toPojo(){

        return TimedEventStatus.builder()
                .status(this.status.toString())
                .timestamp(this.statusTimestamp)
                .message(this.message)
                .build();

    }

    public static List<TimedEventStatus> toPojo(Set<TimedEventStatusEntity> statusEntities){
        List<TimedEventStatus> statuses = new ArrayList<>();
        for(TimedEventStatusEntity entity : statusEntities) {
            statuses.add(entity.toPojo());
        }
        return statuses;
    }


    public enum Status {
        EVENT_TRIGGERED,
        EVENT_DETECTED,
        SUBSCRIPTION_TRIGGERED,
        SUBSCRIPTION_DISABLED_HALT,
        OBJECT_SERIALIZED,
        OBJECT_SERIALIZATION_FAULT,
        OBJECT_FILTERED,
        OBJECT_FILTERING_FAULT,
        OBJECT_FILTER_MISMATCH_HALT,
        RESOLVING_ATTRIBUTES,
        ACTION_CALLED,
        ACTION_STEP,
        ACTION_ERROR,
        ACTION_FAILED,
        ACTION_COMPLETE,
        FAILED
    }
}
