package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.xnat.compute.models.ComputeEnvironmentHardwareOptions;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ComputeEnvironmentHardwareOptionsEntity {

    private long id;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ComputeEnvironmentConfigEntity computeEnvironmentConfig;

    private boolean allowAllHardware;
    private Set<HardwareConfigEntity> hardwareConfigs;

    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToOne
    @MapsId
    public ComputeEnvironmentConfigEntity getComputeEnvironmentConfig() {
        return computeEnvironmentConfig;
    }

    public void setComputeEnvironmentConfig(ComputeEnvironmentConfigEntity computeEnvironmentConfig) {
        this.computeEnvironmentConfig = computeEnvironmentConfig;
    }

    public boolean isAllowAllHardware() {
        return allowAllHardware;
    }

    public void setAllowAllHardware(boolean allowAllHardware) {
        this.allowAllHardware = allowAllHardware;
    }

    @ManyToMany
    @JoinTable(name = "compute_environment_hardware_options_hardware_config",
               joinColumns = @JoinColumn(name = "compute_environment_hardware_options_id"),
               inverseJoinColumns = @JoinColumn(name = "hardware_config_id"))
    public Set<HardwareConfigEntity> getHardwareConfigs() {
        return hardwareConfigs;
    }

    public void setHardwareConfigs(Set<HardwareConfigEntity> hardwareConfigs) {
        this.hardwareConfigs = hardwareConfigs;
    }

    public void addHardwareConfig(HardwareConfigEntity hardwareConfig) {
        if (hardwareConfigs == null) {
            hardwareConfigs = new HashSet<>();
        }

        hardwareConfigs.add(hardwareConfig);
    }

    public void removeHardwareConfig(HardwareConfigEntity hardwareConfig) {
        hardwareConfigs.remove(hardwareConfig);
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo to create the entity from.
     * @return The newly created entity.
     */
    public static ComputeEnvironmentHardwareOptionsEntity fromPojo(final ComputeEnvironmentHardwareOptions pojo) {
        final ComputeEnvironmentHardwareOptionsEntity entity = new ComputeEnvironmentHardwareOptionsEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Creates a new pojo from the given entity.
     * @return The newly created pojo.
     */
    public ComputeEnvironmentHardwareOptions toPojo() {
        return ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(allowAllHardware)
                .hardwareConfigs(hardwareConfigs.stream().map(HardwareConfigEntity::toPojo).collect(Collectors.toSet()))
                .build();
    }

    /**
     * Updates the entity with the values from the given pojo.
     * @param pojo The pojo to update the entity with.
     */
    public void update(final ComputeEnvironmentHardwareOptions pojo) {
        setAllowAllHardware(pojo.isAllowAllHardware());

        if (hardwareConfigs == null) {
            hardwareConfigs = new HashSet<>();
        }

        // Updating the hardware configs is handled separately by the add/remove hardware config methods
    }
}
