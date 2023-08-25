package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.ComputeEnvironmentScope;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ComputeEnvironmentScopeEntity {

    private long id;
    private String scope;
    private boolean enabled;
    private Set<String> ids;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ComputeEnvironmentConfigEntity computeEnvironmentConfig;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ElementCollection
    public Set<String> getIds() {
        if (ids == null) {
            ids = new HashSet<>();
        }

        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    @ManyToOne
    public ComputeEnvironmentConfigEntity getComputeEnvironmentConfig() {
        return computeEnvironmentConfig;
    }

    public void setComputeEnvironmentConfig(ComputeEnvironmentConfigEntity computeEnvironmentConfig) {
        this.computeEnvironmentConfig = computeEnvironmentConfig;
    }

    /**
     * Updates this entity with the values from the given pojo.
     * @param pojo The pojo to update from
     */
    public void update(ComputeEnvironmentScope pojo) {
        setScope(pojo.getScope().name());
        setEnabled(pojo.isEnabled());

        getIds().clear();

        // add new ids
        if (pojo.getIds() != null && !pojo.getIds().isEmpty()) {
            getIds().addAll(pojo.getIds());
        }
    }

    /**
     * Converts this entity to a pojo.
     * @return The pojo
     */
    public ComputeEnvironmentScope toPojo() {
        return ComputeEnvironmentScope.builder()
                .scope(Scope.valueOf(getScope()))
                .enabled(isEnabled())
                .ids(getIds())
                .build();
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo to create from
     * @return The new entity
     */
    public static ComputeEnvironmentScopeEntity fromPojo(ComputeEnvironmentScope pojo) {
        final ComputeEnvironmentScopeEntity entity = new ComputeEnvironmentScopeEntity();
        entity.update(pojo);
        return entity;
    }

}
