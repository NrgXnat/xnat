package org.nrg.xnat.eventservice.entities;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.nrg.xnat.eventservice.model.EventFilter;

import javax.persistence.*;
import java.util.List;


@Entity
public class EventServiceFilterEntity {

    public EventServiceFilterEntity() {}

    private long id;
    private String name;
    private List<String> projectIds;
    private String jsonPathFilter;


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                .add("name", name)
                .add("projectIds", projectIds)
                .toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventServiceFilterEntity that = (EventServiceFilterEntity) o;
        return id == that.id &&
                Objects.equal(name, that.name) &&
                Objects.equal(jsonPathFilter, that.jsonPathFilter) &&
                Objects.equal(projectIds, that.projectIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, jsonPathFilter, projectIds);
    }

    public EventServiceFilterEntity(String name, List<String> projectIds, String jsonPathFilter) {
        this.name = name;
        this.projectIds = projectIds;
        this.jsonPathFilter = jsonPathFilter;
    }

    public static EventServiceFilterEntity fromPojo(EventFilter eventServiceFilter) {

        return eventServiceFilter != null ?
                new EventServiceFilterEntity(
                        eventServiceFilter.name(),
                        eventServiceFilter.projectIds(),
                        eventServiceFilter.jsonPathFilter())
                : null;

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

    @ElementCollection
    public List<String> getProjectIds() { return projectIds; }

    public void setProjectIds(List<String> projectIds) { this.projectIds = projectIds; }

    public EventFilter toPojo() {
        return EventFilter.builder()
                          .id(this.id)
                          .name(this.name)
                          .projectIds(this.projectIds)
                          .jsonPathFilter(this.jsonPathFilter)
                          .build();
    }

    public String getJsonPathFilter() {
        return jsonPathFilter;
    }

    public void setJsonPathFilter(String jsonPathFilter) {
        this.jsonPathFilter = jsonPathFilter;
    }
}
