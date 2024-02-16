package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.models.ConstraintScope;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ConstraintConfigEntity extends AbstractHibernateEntity {

    private ConstraintEntity constraint;
    private Map<Scope, ConstraintScopeEntity> scopes;

    @OneToOne(mappedBy = "constraintConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public ConstraintEntity getConstraint() {
        return constraint;
    }

    public void setConstraint(ConstraintEntity constraint) {
        constraint.setConstraintConfig(this);
        this.constraint = constraint;
    }

    @OneToMany(mappedBy = "constraintConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public Map<Scope, ConstraintScopeEntity> getScopes() {
        return scopes;
    }

    public void setScopes(Map<Scope, ConstraintScopeEntity> scopes) {
        this.scopes = scopes;
    }

    /**
     * This method is used to update the entity from the pojo.
     * @param pojo The pojo to update from.
     */
    public void update(final ConstraintConfig pojo) {
        if (getConstraint() == null) {
            // New entity
            setConstraint(ConstraintEntity.fromPojo(pojo.getConstraint()));
        } else {
            // Existing entity
            getConstraint().update(pojo.getConstraint());
        }

        getConstraint().setConstraintConfig(this);

        if (getScopes() == null) {
            // This is a new entity, so we need to create the scope entities
            setScopes(pojo.getScopes()
                          .entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, e -> ConstraintScopeEntity.fromPojo(e.getValue()))));
        } else {
            // This is an existing entity, so we need to update the scope entities
            Map<Scope, ConstraintScopeEntity> existingEntityScopes = getScopes();
            Map<Scope, ConstraintScope> updatedPojoScopes = pojo.getScopes();

            // Collect and remove scopes that are no longer in the updated pojo but are in the existing entity
            Set<Scope> scopesToRemove = existingEntityScopes.keySet().stream()
                    .filter(scope -> !updatedPojoScopes.containsKey(scope))
                    .collect(Collectors.toSet());

            scopesToRemove.forEach(scope -> {
                ConstraintScopeEntity scopeToRemove = existingEntityScopes.remove(scope);
                scopeToRemove.setConstraintConfig(null);
            });

            // Update existing and add new scopes
            updatedPojoScopes.forEach((scope, constraintScope) -> {
                ConstraintScopeEntity existingScope = existingEntityScopes.get(scope);
                if (existingScope != null) {
                    // Update the existing scope
                    existingScope.update(constraintScope);
                } else {
                    // Add new scope
                    existingEntityScopes.put(scope, ConstraintScopeEntity.fromPojo(constraintScope));
                }
            });
        }

        // Set the constraint config on the scope entities
        getScopes().forEach((key, value) -> value.setConstraintConfig(this));
    }

    /**
     * This method is used to convert the entity to a pojo.
     * @return The pojo.
     */
    public ConstraintConfig toPojo() {
        return ConstraintConfig.builder()
                .id(getId())
                .constraint(getConstraint().toPojo())
                .scopes(getScopes()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toPojo())))
                .build();
    }

    /**
     * This method is used to create a new entity from the pojo.
     * @param pojo The pojo to create from.
     * @return The new entity.
     */
    public static ConstraintConfigEntity fromPojo(final ConstraintConfig pojo) {
        final ConstraintConfigEntity entity = new ConstraintConfigEntity();
        entity.update(pojo);
        return entity;
    }

}
