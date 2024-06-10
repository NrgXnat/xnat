package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.ConstraintScope;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ConstraintScopeEntity {

    private long id;
    private String scope;
    private boolean enabled;
    private Set<String> ids;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ConstraintConfigEntity constraintConfig;

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
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    @ManyToOne
    public ConstraintConfigEntity getConstraintConfig() {
        return constraintConfig;
    }

    public void setConstraintConfig(ConstraintConfigEntity constraintConfig) {
        this.constraintConfig = constraintConfig;
    }

    /**
     * Converts this entity to a pojo.
     * @return The pojo.
     */
    public ConstraintScope toPojo() {
        return ConstraintScope.builder()
                .scope(Scope.valueOf(getScope()))
                .enabled(isEnabled())
                .ids(getIds())
                .build();
    }

    /**
     * Updates this entity from the given pojo.
     * @param pojo The pojo.
     */
    public void update(final ConstraintScope pojo) {
        setScope(pojo.getScope().name());
        setEnabled(pojo.isEnabled());

        if (getIds() == null) {
            // This is a new entity, so we need to initialize the collection
            setIds(new HashSet<>());
        } else {
            // This is an existing entity, so we need to clear the collection
            getIds().clear();
        }

        // add new ids
        if (pojo.getIds() != null) {
            getIds().addAll(pojo.getIds());
        }
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo.
     * @return The entity.
     */
    public static ConstraintScopeEntity fromPojo(final ConstraintScope pojo) {
        final ConstraintScopeEntity entity = new ConstraintScopeEntity();
        entity.update(pojo);
        return entity;
    }

}
