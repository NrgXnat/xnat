package org.nrg.xnat.eventservice.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
    private Boolean compressed;
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

    @Column(name = "payload")
    @Lob
    private byte[] getPayloadAsByteArray() { return payload; }

    private void setPayloadAsByteArray(byte[] payload) { this.payload = payload; }

    @Transient
    public Object getPayload() {
            return payload == null ?
                    null :
                    (getCompressed() ?
                            SerializationUtils.deserialize(decompress(payload)) :
                            SerializationUtils.deserialize(payload));
        }

    @Transient
    public void setPayload(Object payload) {
        if (payload != null && payload instanceof Serializable) {
            setCompressed(false);
            byte[] bytes = SerializationUtils.serialize(payload);
            // if serialized payload is larger than 1k, compress before storing
            if(bytes.length > 1024) {
                byte[] compressedBytes = compress(bytes);
                if (compressedBytes != null) {
                    setCompressed(true);
                    this.setPayloadAsByteArray(compressedBytes);
                    return;
                }
            }
            this.setPayloadAsByteArray(bytes);
        }
    }

    @Transient
    public byte[] compress(byte[] bytes){
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(bytes);
        deflater.finish();
        ByteArrayOutputStream output = new ByteArrayOutputStream(bytes.length);
        byte[] buffer = new byte[1024];
        try{
            while(!deflater.finished()){
                int size = deflater.deflate(buffer);
                output.write(buffer, 0, size);
            }
        } catch (Exception ex){
            log.error("Problem compressing serialized payload: " + ex.getMessage());
        } finally {
            try{
                if(output != null) output.close();
            } catch(Exception ex){
                log.error("Problem closing compression stream: " + ex.getMessage());
            }
        }
        byte[] compressedBytes = output.toByteArray();
        if(log.isDebugEnabled()){
            log.debug("Compressing serialized payload object saved %i bytes.", (bytes != null && compressedBytes != null) ? bytes.length - compressedBytes.length : -1);
        }
        return compressedBytes;
    }


    @Transient byte[] decompress(byte[] bytes){
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try{
            while(!inflater.finished()){
                int size = inflater.inflate(buffer);
                output.write(buffer, 0, size);
            }
        } catch (Exception ex){
            log.error("Problem decompressing serialized payload: " + ex.getMessage());
        } finally {
            try{
                if(output != null) output.close();
            } catch(Exception ex){
                log.error("Problem closing decompression stream: " + ex.getMessage());
            }
        }
        return output.toByteArray();
    }

    public Boolean getCompressed() { return compressed == null ? false : compressed; }

    public void setCompressed(Boolean compressedPayload) { this.compressed = compressedPayload; }

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
                .serializablePayload(serializablePayload != null ? serializablePayload : "")
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
