package org.nrg.xnat.eventservice.entities;

import com.google.common.base.Objects;
import org.nrg.xnat.eventservice.model.TimedEventStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class TimedEventStatusEntity implements Serializable {

    public TimedEventStatusEntity() {
    }

    public TimedEventStatusEntity(Status status, Date statusTimestamp, String message, SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        this.status = status;
        this.statusTimestamp = statusTimestamp;
        this.message = message;
        this.subscriptionDeliveryEntity = subscriptionDeliveryEntity;
    }

    private long id;
    private Status status;
    private Date statusTimestamp;
    private String message;
    private SubscriptionDeliveryEntity subscriptionDeliveryEntity;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public static List<TimedEventStatus> toPojo(List<TimedEventStatusEntity> statusEntities){
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
        OBJECT_FILTER_MISMATCH_HALT,
        ACTION_CALLED,
        ACTION_STEP,
        ACTION_FAILED,
        ACTION_COMPLETE,
        FAILED
    }
}
