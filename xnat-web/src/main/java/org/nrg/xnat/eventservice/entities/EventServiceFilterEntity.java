package org.nrg.xnat.eventservice.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.model.EventFilter;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Entity
public class EventServiceFilterEntity {

    public EventServiceFilterEntity() {}

    private long id;
    private String name;
    private String eventType;
    private List<String> projectIds;
    private String status;
    private String nodeFilterString;
    private String jsonPathFilter;

    @Autowired
    private static ObjectMapper objectMapper;
    private static final TypeReference<HashMap<String, JsonPathFilterNode>>
            TYPE_REF_MAP_STRING_FILTER_NODE = new TypeReference<HashMap<String, JsonPathFilterNode>>() {
    };

    @Override
    public String toString() {
        return "EventServiceFilterEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", projectIds=" + projectIds +
                ", status='" + status + '\'' +
                ", nodeFilterString='" + nodeFilterString + '\'' +
                ", jsonPathFilter='" + jsonPathFilter + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventServiceFilterEntity)) return false;
        EventServiceFilterEntity that = (EventServiceFilterEntity) o;
        return id == that.id &&
                Objects.equal(name, that.name) &&
                Objects.equal(eventType, that.eventType) &&
                Objects.equal(projectIds, that.projectIds) &&
                Objects.equal(status, that.status) &&
                Objects.equal(nodeFilterString, that.nodeFilterString) &&
                Objects.equal(jsonPathFilter, that.jsonPathFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, eventType, projectIds, status, nodeFilterString, jsonPathFilter);
    }

    public EventServiceFilterEntity(long id, String name, String eventType, List<String> projectIds, String status,
                                    Map<String, JsonPathFilterNode> nodeFilters, String jsonPathFilter) {
        this.id = id;
        this.name = name;
        this.eventType = eventType;
        this.projectIds = projectIds;
        this.status = status;
        this.jsonPathFilter = jsonPathFilter;
        try {
            this.nodeFilterString = nodeFilters!= null ? objectMapper.writeValueAsString(nodeFilters) : null;
        } catch (JsonProcessingException e) {
            log.error("Exception converting node filters to string.", e.getMessage());
        }
    }

    public EventServiceFilterEntity(long id, String name, String eventType, List<String> projectIds,
                                    String status, String nodeFilterString, String jsonPathFilter) {
        this.id = id;
        this.name = name;
        this.eventType = eventType;
        this.projectIds = projectIds;
        this.status = status;
        this.nodeFilterString = nodeFilterString;
        this.jsonPathFilter = jsonPathFilter;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getNodeFilterString() { return nodeFilterString; }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @ElementCollection
    public List<String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<String> projectIds) {
        this.projectIds = projectIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNodeFilterString(String nodeFilterString) {
        this.nodeFilterString = nodeFilterString;
    }

    public String getJsonPathFilter() {
        return jsonPathFilter;
    }

    public void setJsonPathFilter(String jsonPathFilter) {
        this.jsonPathFilter = jsonPathFilter;
    }

    @Transient
    public EventFilter toPojo() {
        EventFilter eventFilter = EventFilter.builder()
                                             .id(this.id)
                                             .name(this.name)
                                             .eventType(this.eventType)
                                             .projectIds(this.projectIds != null ? new ArrayList<>(this.projectIds) : null)
                                             .status(this.status)
                                             .jsonPathFilter(this.jsonPathFilter)


                                             .build();
        if (!Strings.isNullOrEmpty(this.nodeFilterString)) {
            try {
                Map<String, JsonPathFilterNode> nodeFilter = objectMapper.readValue(this.nodeFilterString, TYPE_REF_MAP_STRING_FILTER_NODE);

                eventFilter = eventFilter.toBuilder().nodeFilters(nodeFilter).build();


            } catch (Throwable e) {
                log.error("Exception attempting to de-serialize NodeFilter Map on eventFilter : " + Long.toString(this.id));
            }

        }
        return eventFilter;
    }


    @Transient
    public static EventServiceFilterEntity fromPojo(@Nonnull EventFilter eventServiceFilter) {
        EventServiceFilterEntity eventServiceFilterEntity = new EventServiceFilterEntity();
        eventServiceFilterEntity.setName(eventServiceFilter.name());
        eventServiceFilterEntity.setEventType(eventServiceFilter.eventType());
        eventServiceFilterEntity.setProjectIds(eventServiceFilter.projectIds());
        eventServiceFilterEntity.setStatus(eventServiceFilter.status());
        eventServiceFilterEntity.setJsonPathFilter(eventServiceFilter.jsonPathFilter());
        try {
            eventServiceFilterEntity.setNodeFilterString(eventServiceFilter.nodeFilters()!= null
                    ? objectMapper.writeValueAsString(eventServiceFilter.nodeFilters())
                    : null);
        } catch (JsonProcessingException e) {
            log.error("Exception converting node filters to string.", e.getMessage());
        }
        if (eventServiceFilter.nodeFilters() != null && !eventServiceFilter.nodeFilters().isEmpty()) {
            try {
                log.debug("Serializing nodeFilter on eventServiceFilter : " + Long.toString(eventServiceFilter.id()));
                String nodeFilterJson = objectMapper.writeValueAsString(eventServiceFilter.nodeFilters());
                eventServiceFilterEntity.setNodeFilterString(nodeFilterJson);
            } catch (JsonProcessingException e) {
                log.error("Exception when attempting to serialize nodeFilter on eventServiceFilter : " + Long.toString(eventServiceFilter.id()));
                log.error(e.getMessage());
            }
        }
        return eventServiceFilterEntity;
    }


}
