package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.Hardware;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class HardwareEntity extends AbstractHibernateEntity {

    private String name;

    private Double cpuReservation;
    private Double cpuLimit;
    private String memoryReservation;
    private String memoryLimit;

    private List<HardwareConstraintEntity> constraints;
    private List<EnvironmentVariableEntity> environmentVariables;
    private List<GenericResourceEntity> genericResources;

    @ToString.Exclude @EqualsAndHashCode.Exclude private HardwareConfigEntity hardwareConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCpuReservation() {
        return cpuReservation;
    }

    public void setCpuReservation(Double cpuReservation) {
        this.cpuReservation = cpuReservation;
    }

    public Double getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(Double cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public String getMemoryReservation() {
        return memoryReservation;
    }

    public void setMemoryReservation(String memoryReservation) {
        this.memoryReservation = memoryReservation;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    @OneToMany(mappedBy = "hardware", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<HardwareConstraintEntity> getConstraints() {
        if (constraints == null) {
            constraints = new ArrayList<>();
        }

        return constraints;
    }

    public void setConstraints(List<HardwareConstraintEntity> constraints) {
        this.constraints = constraints;
    }

    @ElementCollection
    public List<EnvironmentVariableEntity> getEnvironmentVariables() {
        if (environmentVariables == null) {
            environmentVariables = new ArrayList<>();
        }

        return environmentVariables;
    }

    public void setEnvironmentVariables(List<EnvironmentVariableEntity> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @ElementCollection
    public List<GenericResourceEntity> getGenericResources() {
        if (genericResources == null) {
            genericResources = new ArrayList<>();
        }

        return genericResources;
    }

    public void setGenericResources(List<GenericResourceEntity> genericResources) {
        this.genericResources = genericResources;
    }

    @OneToOne
    public HardwareConfigEntity getHardwareConfig() {
        return hardwareConfig;
    }

    public void setHardwareConfig(HardwareConfigEntity hardwareConfig) {
        this.hardwareConfig = hardwareConfig;
    }

    public static HardwareEntity fromPojo(Hardware pojo) {
        HardwareEntity entity = new HardwareEntity();
        entity.update(pojo);
        return entity;
    }

    public Hardware toPojo() {
        return Hardware.builder()
                .name(getName())
                .cpuReservation(getCpuReservation())
                .cpuLimit(getCpuLimit())
                .memoryReservation(getMemoryReservation())
                .memoryLimit(getMemoryLimit())
                .constraints(getConstraints().stream().map(HardwareConstraintEntity::toPojo).collect(Collectors.toList()))
                .environmentVariables(getEnvironmentVariables().stream().map(EnvironmentVariableEntity::toPojo).collect(Collectors.toList()))
                .genericResources(getGenericResources().stream().map(GenericResourceEntity::toPojo).collect(Collectors.toList()))
                .build();
    }

    public void update(Hardware pojo) {
        setName(pojo.getName());
        setCpuReservation(pojo.getCpuReservation());
        setCpuLimit(pojo.getCpuLimit());
        setMemoryReservation(pojo.getMemoryReservation());
        setMemoryLimit(pojo.getMemoryLimit());

        // remove old constraints, need to remove hardware reference from old constraints before clearing
        getConstraints().forEach(c -> c.setHardware(null));
        getConstraints().clear();

        // add new constraints
        if (pojo.getConstraints() != null) {
            getConstraints().addAll(pojo.getConstraints()
                                            .stream()
                                            .map(HardwareConstraintEntity::fromPojo)
                                            .collect(Collectors.toList()));
            getConstraints().forEach(c -> c.setHardware(this));
        }

        // remove old environment variables
        getEnvironmentVariables().clear();

        // add new environment variables
        if (pojo.getEnvironmentVariables() != null) {
            getEnvironmentVariables().addAll(pojo.getEnvironmentVariables()
                                                 .stream()
                                                 .map(EnvironmentVariableEntity::fromPojo)
                                                 .collect(Collectors.toList()));
        }

        // remove old resources
        getGenericResources().clear();

        // add new resources
        if (pojo.getGenericResources() != null) {
            getGenericResources().addAll(pojo.getGenericResources()
                                      .stream()
                                      .map(GenericResourceEntity::fromPojo)
                                      .collect(Collectors.toList()));
        }
    }

}
