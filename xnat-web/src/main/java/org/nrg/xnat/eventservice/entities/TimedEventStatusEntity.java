package org.nrg.xnat.eventservice.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.model.TimedEventStatus;
import org.springframework.util.SerializationUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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

    public TimedEventStatusEntity(Status status, Date statusTimestamp, String message, Object payload, SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        setStatus(status);
        setStatusTimestamp(statusTimestamp);
        setMessage(message);
        setPayload(payload);
        setSubscriptionDeliveryEntity(subscriptionDeliveryEntity);
    }

    private long id;
    private Status status;
    private Date statusTimestamp;
    private String message;
    private byte[] payload;
    private SubscriptionDeliveryEntity subscriptionDeliveryEntity;
    private static final ObjectMapper mapper = new ObjectMapper();

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
        this.message = message;
    }

    @Column(name = "payload")
    @Lob
    private byte[] getPayloadAsByteArray() { return payload; }

    private void setPayloadAsByteArray(byte[] payload) { this.payload = payload; }

    @Transient
    public Object getPayload() {
            return payload == null ? null : SerializationUtils.deserialize(payload);
        }

    public void setPayload(Object payload) {
        if (payload != null && payload instanceof Serializable) {
            this.payload = SerializationUtils.serialize(payload);
        } else {
            this.payload = null;
        }
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
                Objects.equal(payload, that.payload) &&
                Objects.equal(subscriptionDeliveryEntity, that.subscriptionDeliveryEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), status, statusTimestamp, message, payload, subscriptionDeliveryEntity);
    }

    public TimedEventStatus toPojo(){
        Object payload = getPayload();
        Object serializablePayload = null;
        // Attempt to write payload to string
        // Skip adding to pojo if it fails, since we want it to fail here rather then on display
        if(payload != null && payload instanceof Serializable && mapper.canSerialize(payload.getClass())) {
            try{
                String jsonString = mapper.writeValueAsString(payload);
                serializablePayload = payload;
            } catch (Throwable e) {
                log.error("Could not serialize " + payload.getClass().getName(), e.getMessage());
            }
            if(serializablePayload == null){
                try{
                    serializablePayload = payload.toString();
                } catch (Throwable e) {
                    log.error("Exception during string conversion for: " + payload.getClass().getName(), e.getMessage());
                }
            }
        }

        return TimedEventStatus.builder()
                .status(this.status.toString())
                .timestamp(this.statusTimestamp)
                .message(this.message)
                .serializablePayload(serializablePayload)
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
