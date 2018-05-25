package org.nrg.xnat.eventservice.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.nrg.xnat.eventservice.model.EventFilter;
import
org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import
java.util.HashMap;
import java.util.Map;


@Entity
public class EventServiceFilterEntity {

    public EventServiceFilterEntity() {}

    private long id;
    private String name;
    private String jsonPathFilter;


  private String nodeFilterJson;

    @Autowired
    private static ObjectMapper objectMapper;
    private static final TypeReference<HashMap<String, JsonPathFilterNode>>
TYPE_REF_MAP_STRING_FILTER_NODE      = new TypeReference<HashMap<String, JsonPathFilterNode>>() {};

    private static final Logger log = LoggerFactory.getLogger(EventServiceFilterEntity.class);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                .add("name", name)
                .toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventServiceFilterEntity that = (EventServiceFilterEntity) o;
        return id == that.id &&
                Objects.equal(name, that.name) &&
                Objects.equal(jsonPathFilter, that.jsonPathFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, jsonPathFilter);
    }

    public EventServiceFilterEntity(String name, String jsonPathFilter) {
        this.name = name;
        this.jsonPathFilter = jsonPathFilter;
    }

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;     }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getNodeFilterJson() {

       return nodeFilterJson;
    }

    public void setNodeFilterJson(String nodeFilterJson) {
        this.nodeFilterJson = nodeFilterJson; 
   }      public EventFilter toPojo() {         EventFilter eventFilter = EventFilter.builder()
                                       .id(this.id)
                                       .name(this.name)
                                       .jsonPathFilter(this.jsonPathFilter)
                 

    

              .build();
        if(!Strings.isNullOrEmpty(this.nodeFilterJson)){
            try {
                Map<String, JsonPathFilterNode> nodeFilter = objectMapper.readValue(this.nodeFilterJson, TYPE_REF_MAP_STRING_FILTER_NODE);
       
        eventFilter = eventFilter.toBuilder().nodeFilters(nodeFilter).build();
    

      } catch (Throwable e) {
                log.error("Exception attempting to de-serialize NodeFilter Map on eventFilter : " + Long.toString(this.id)); 
           }

        }
        return eventFilter;
    }

    public static EventServiceFilterEntity fromPojo(EventFilter eventServiceFilter)
{
         EventServiceFilterEntity eventServiceFilterEntity = new EventServiceFilterEntity(eventServiceFilter.name(), eventServiceFilter.jsonPathFilter());   

    if (eventServiceFilter != null){
            if(eventServiceFilter.nodeFilters() != null && !eventServiceFilter.nodeFilters().isEmpty()){
                try {                     log.debug("Serializing nodeFilter on eventServiceFilter : " + Long.toString(eventServiceFilter.id()));
                    String nodeFilterJson = objectMapper.writeValueAsString(eventServiceFilter.nodeFilters());
                    eventServiceFilterEntity.setNodeFilterJson(nodeFilterJson);                  } catch (JsonProcessingException e) {
                    log.error("Exception when attempting to serialize nodeFilter on eventServiceFilter : " + Long.toString(eventServiceFilter.id()));
                    log.error(e.getMessage());                 }
            }
        }
        return eventServiceFilterEntity;
    }

    public String getJsonPathFilter() {
        return jsonPathFilter;
    }

    public void setJsonPathFilter(String jsonPathFilter) {
        this.jsonPathFilter = jsonPathFilter;
    }
}
