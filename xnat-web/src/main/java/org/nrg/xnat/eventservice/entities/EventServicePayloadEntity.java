package org.nrg.xnat.eventservice.entities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Slf4j
@Entity
public class EventServicePayloadEntity implements Serializable {

    private long id;
    private byte[] payload;
    private Boolean compressed;
    private SubscriptionDeliveryEntity subscriptionDeliveryEntity;


    public EventServicePayloadEntity() {
    }

    public EventServicePayloadEntity(Object payload, SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        setPayload(payload);
        setSubscriptionDeliveryEntity(subscriptionDeliveryEntity);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public long getId() { return id; }

    public void setId(final long id) { this.id = id; }

    @OneToOne(mappedBy = "payload")
    public SubscriptionDeliveryEntity getSubscriptionDeliveryEntity() {
        return subscriptionDeliveryEntity;
    }

    public void setSubscriptionDeliveryEntity(SubscriptionDeliveryEntity subscriptionDeliveryEntity) {
        this.subscriptionDeliveryEntity = subscriptionDeliveryEntity;
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


    @Transient
    byte[] decompress(byte[] bytes){
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

}
