package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.xnat.compute.models.GenericResource;

import javax.persistence.Embeddable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class GenericResourceEntity {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void update(GenericResource genericResource) {
        setName(genericResource.getName());
        setValue(genericResource.getValue());
    }

    public GenericResource toPojo() {
        return GenericResource.builder()
                .name(name)
                .value(value)
                .build();
    }

    public static GenericResourceEntity fromPojo(GenericResource genericResource) {
        final GenericResourceEntity entity = new GenericResourceEntity();
        entity.update(genericResource);
        return entity;
    }

}
