package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.Constraint;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ConstraintEntity extends AbstractHibernateEntity {

    private String key;
    private Set<String> constraintValues; // Different from model, values is a reserved word
    private String operator;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ConstraintConfigEntity constraintConfig;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @ElementCollection
    public Set<String> getConstraintValues() {
        return constraintValues;
    }

    public void setConstraintValues(Set<String> constraintValues) {
        if (constraintValues == null) {
            constraintValues = new HashSet<>();
        }

        this.constraintValues = constraintValues;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @OneToOne
    @MapsId
    public ConstraintConfigEntity getConstraintConfig() {
        return constraintConfig;
    }

    public void setConstraintConfig(ConstraintConfigEntity constraintConfig) {
        this.constraintConfig = constraintConfig;
    }

    public void update(final Constraint constraint) {
        setKey(constraint.getKey());
        setConstraintValues(constraint.getValues());
        setOperator(constraint.getOperator().toString());
        setConstraintConfig(this.constraintConfig);
    }

    public Constraint toPojo() {
        return Constraint.builder()
                .key(getKey())
                .values(getConstraintValues())
                .operator(Constraint.Operator.valueOf(getOperator()))
                .build();
    }

    public static ConstraintEntity fromPojo(final Constraint constraint) {
        final ConstraintEntity entity = new ConstraintEntity();
        entity.update(constraint);
        return entity;
    }

}
