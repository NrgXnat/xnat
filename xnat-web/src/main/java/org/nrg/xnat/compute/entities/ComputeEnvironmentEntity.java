package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.ComputeEnvironment;

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
public class ComputeEnvironmentEntity extends AbstractHibernateEntity {

    private String name;
    private String image;
    private String command;

    private List<EnvironmentVariableEntity> environmentVariables;
    private List<MountEntity> mounts;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ComputeEnvironmentConfigEntity computeEnvironmentConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
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
    public List<MountEntity> getMounts() {
        if (mounts == null) {
            mounts = new ArrayList<>();
        }

        return mounts;
    }

    public void setMounts(List<MountEntity> mounts) {
        this.mounts = mounts;
    }

    @OneToOne
    public ComputeEnvironmentConfigEntity getComputeEnvironmentConfig() {
        return computeEnvironmentConfig;
    }

    public void setComputeEnvironmentConfig(ComputeEnvironmentConfigEntity computeEnvironmentConfig) {
        this.computeEnvironmentConfig = computeEnvironmentConfig;
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo to convert.
     * @return The entity created from the pojo.
     */
    public static ComputeEnvironmentEntity fromPojo(ComputeEnvironment pojo) {
        final ComputeEnvironmentEntity entity = new ComputeEnvironmentEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Converts this entity to a pojo.
     * @return The pojo created from this entity.
     */
    public ComputeEnvironment toPojo() {
        return ComputeEnvironment.builder()
                .name(getName())
                .image(getImage())
                .command(getCommand())
                .environmentVariables(getEnvironmentVariables().stream().map(EnvironmentVariableEntity::toPojo).collect(Collectors.toList()))
                .mounts(getMounts().stream().map(MountEntity::toPojo).collect(Collectors.toList()))
                .build();
    }

    /**
     * Updates this entity from the given pojo.
     * @param pojo The pojo to update from.
     */
    public void update(ComputeEnvironment pojo) {
        setName(pojo.getName());
        setImage(pojo.getImage());
        setCommand(pojo.getCommand());

        // Clear the existing environment variables
        getEnvironmentVariables().clear();

        // Add the new environment variables
        if (pojo.getEnvironmentVariables() != null) {
            getEnvironmentVariables().addAll(pojo.getEnvironmentVariables()
                                                     .stream()
                                                     .map(EnvironmentVariableEntity::fromPojo)
                                                     .collect(Collectors.toList()));
        }

        // Clear the existing mounts
        getMounts().clear();

        // Add the new mounts
        if (pojo.getMounts() != null) {
            getMounts().addAll(pojo.getMounts()
                                          .stream()
                                          .map(MountEntity::fromPojo)
                                          .collect(Collectors.toList()));
        }
    }

}
