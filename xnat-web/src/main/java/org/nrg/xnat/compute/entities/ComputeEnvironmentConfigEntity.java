package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.models.ComputeEnvironmentScope;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ComputeEnvironmentConfigEntity extends AbstractHibernateEntity {

    private Set<String> configTypes;

    private ComputeEnvironmentEntity computeEnvironment;
    private Map<Scope, ComputeEnvironmentScopeEntity> scopes;
    private ComputeEnvironmentHardwareOptionsEntity hardwareOptions;

    @ElementCollection
    public Set<String> getConfigTypes() {
        if (configTypes == null) {
            configTypes = new HashSet<>();
        }

        return configTypes;
    }

    public void setConfigTypes(Set<String> configTypes) {
        this.configTypes = configTypes;
    }

    @OneToOne(mappedBy = "computeEnvironmentConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public ComputeEnvironmentEntity getComputeEnvironment() {
        return computeEnvironment;
    }

    public void setComputeEnvironment(ComputeEnvironmentEntity computeEnvironment) {
        computeEnvironment.setComputeEnvironmentConfig(this);
        this.computeEnvironment = computeEnvironment;
    }

    @OneToMany(mappedBy = "computeEnvironmentConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public Map<Scope, ComputeEnvironmentScopeEntity> getScopes() {
        return scopes;
    }

    public void setScopes(Map<Scope, ComputeEnvironmentScopeEntity> scopes) {
        this.scopes = scopes;
    }

    @OneToOne(mappedBy = "computeEnvironmentConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public ComputeEnvironmentHardwareOptionsEntity getHardwareOptions() {
        return hardwareOptions;
    }

    public void setHardwareOptions(ComputeEnvironmentHardwareOptionsEntity hardwareOptions) {
        this.hardwareOptions = hardwareOptions;
    }

    /**
     * Creates a new entity from the pojo.
     * @param pojo The pojo to create the entity from
     * @return The newly created entity
     */
    public static ComputeEnvironmentConfigEntity fromPojo(final ComputeEnvironmentConfig pojo) {
        final ComputeEnvironmentConfigEntity entity = new ComputeEnvironmentConfigEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Creates a new pojo from the entity.
     * @return The pojo created from the entity
     */
    public ComputeEnvironmentConfig toPojo() {
        return ComputeEnvironmentConfig.builder()
                .id(getId())
                .configTypes(getConfigTypes()
                                     .stream()
                                     .map(ComputeEnvironmentConfig.ConfigType::valueOf)
                                     .collect(Collectors.toSet()))
                .computeEnvironment(getComputeEnvironment().toPojo())
                .scopes(getScopes()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toPojo())))
                .hardwareOptions(getHardwareOptions().toPojo())
                .build();
    }

    /**
     * Updates the entity with the values from the pojo. Does not update the hardwareConfigs, since that is a
     * many-to-many relationship and needs to be handled separately.
     * @param pojo The pojo to update the entity with
     */
    public void update(final ComputeEnvironmentConfig pojo) {
        setConfigTypes(pojo.getConfigTypes()
                              .stream()
                              .map(Enum::name)
                              .collect(Collectors.toSet()));

        if (getComputeEnvironment() == null) {
            // This is a new entity, so we need to create the computeEnvironment entity
            setComputeEnvironment(ComputeEnvironmentEntity.fromPojo(pojo.getComputeEnvironment()));
        } else {
            // This is an existing entity, so we need to update the computeEnvironment entity
            getComputeEnvironment().update(pojo.getComputeEnvironment());
        }

        if (getScopes() == null) {
            // This is a new entity, so we need to create the scopes entity
            setScopes(pojo.getScopes()
                              .entrySet()
                              .stream()
                              .collect(Collectors.toMap(Map.Entry::getKey, e -> ComputeEnvironmentScopeEntity.fromPojo(e.getValue()))));
        } else {
            // This is an existing entity, so we need to update the scopes entities
            Map<Scope, ComputeEnvironmentScopeEntity> existingEntityScopes = getScopes();
            Map<Scope, ComputeEnvironmentScope> updatedPojoScopes = pojo.getScopes();

            // Collect and remove scopes that are no longer in the updated pojo but are in the existing entity
            Set<Scope> scopesToRemove = existingEntityScopes.keySet().stream()
                    .filter(scope -> !updatedPojoScopes.containsKey(scope))
                    .collect(Collectors.toSet());

            scopesToRemove.forEach(scope -> {
                ComputeEnvironmentScopeEntity scopeToRemove = existingEntityScopes.remove(scope);
                scopeToRemove.setComputeEnvironmentConfig(null);
            });

            // Update existing and add new scopes
            updatedPojoScopes.forEach((scope, computeEnvironmentScope) -> {
                ComputeEnvironmentScopeEntity existingScope = existingEntityScopes.get(scope);
                if (existingScope != null) {
                    // Update the existing scope
                    existingScope.update(computeEnvironmentScope);
                } else {
                    // Add new scope
                    existingEntityScopes.put(scope, ComputeEnvironmentScopeEntity.fromPojo(computeEnvironmentScope));
                }
            });
        }

        // Set the computeEnvironmentConfig on the scopes
        getScopes().values().forEach(s -> s.setComputeEnvironmentConfig(this));

        if (getHardwareOptions() == null) {
            // This is a new entity, so we need to create the hardwareOptions entity
            ComputeEnvironmentHardwareOptionsEntity computeEnvironmentHardwareOptionsEntity = ComputeEnvironmentHardwareOptionsEntity.fromPojo(pojo.getHardwareOptions());
            setHardwareOptions(computeEnvironmentHardwareOptionsEntity);
            computeEnvironmentHardwareOptionsEntity.setComputeEnvironmentConfig(this);
        } else {
            // This is an existing entity, so we need to update the hardwareOptions entity
            getHardwareOptions().update(pojo.getHardwareOptions());
        }
    }

}
