package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.xnat.compute.models.EnvironmentVariable;

import javax.persistence.Embeddable;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class EnvironmentVariableEntity {

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void update(EnvironmentVariable environmentVariable) {
        setKey(environmentVariable.getKey());
        setValue(environmentVariable.getValue());
    }

    public EnvironmentVariable toPojo() {
        return EnvironmentVariable.builder()
                .key(key)
                .value(value)
                .build();
    }

    public static EnvironmentVariableEntity fromPojo(EnvironmentVariable environmentVariable) {
        final EnvironmentVariableEntity entity = new EnvironmentVariableEntity();
        entity.update(environmentVariable);
        return entity;
    }

}
