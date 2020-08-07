package org.nrg.xnat.eventservice.entities;

import com.google.common.base.MoreObjects;
import org.nrg.xnat.eventservice.model.TriggeringEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class TriggeringEventEntity implements Serializable{

    private long id;
    private String eventName;
    private Boolean isXsiType;
    private String xnatType;
    private String xsiUri;
    private String objectLabel;

    public TriggeringEventEntity() {

    }

    public TriggeringEventEntity(String eventName, Boolean isXsiType, String xnatType, String xsiUri, String objectLabel) {

        this.eventName = eventName;
        this.isXsiType = isXsiType;
        this.xnatType = xnatType;
        this.xsiUri = xsiUri;
        this.objectLabel = objectLabel;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Column(name = "event_name")
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Column(name = "is_xsi_type")
    public Boolean getXsiType() {
        return isXsiType;
    }

    public void setXsiType(Boolean xsiType) {
        isXsiType = xsiType;
    }

    @Column(name = "xnat_type")
    public String getXnatType() {
        return xnatType;
    }

    public void setXnatType(String xnatType) {
        this.xnatType = xnatType;
    }

    @Column(name = "xsi_uri")
    public String getXsiUri() {
        return xsiUri;
    }

    public void setXsiUri(String xsiUri) {
        this.xsiUri = xsiUri;
    }

    @Column(name = "object_label")
    public String getObjectLabel() {
        return objectLabel;
    }

    public void setObjectLabel(String objectLabel) {
        this.objectLabel = objectLabel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("eventName", eventName)
                .add("isXsiType", isXsiType)
                .add("xnatType", xnatType)
                .add("xsiUri", xsiUri)
                .add("objectLabel", objectLabel)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggeringEventEntity that = (TriggeringEventEntity) o;
        return Objects.equals(eventName, that.eventName) &&
                Objects.equals(isXsiType, that.isXsiType) &&
                Objects.equals(xnatType, that.xnatType) &&
                Objects.equals(xsiUri, that.xsiUri) &&
                Objects.equals(objectLabel, that.objectLabel);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eventName, isXsiType, xnatType, xsiUri, objectLabel);
    }

    public TriggeringEvent toPojo(){
        return TriggeringEvent.builder()
                .eventName(this.eventName != null ? this.eventName : "")
                .isXsiType(this.isXsiType != null ? this.isXsiType : false)
                .xnatType(this.xnatType != null ? this.xnatType : "")
                .xsiUri(this.xsiUri)
                .objectLabel(this.objectLabel)
                .build();
    }

}
