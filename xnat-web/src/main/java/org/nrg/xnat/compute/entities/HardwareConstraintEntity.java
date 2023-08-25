package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.Constraint;

import javax.persistence.*;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HardwareConstraintEntity extends AbstractHibernateEntity {

    private String key;
    private Set<String> constraintValues; // Different from model, values is a reserved word
    private String operator;

    @ToString.Exclude @EqualsAndHashCode.Exclude private HardwareEntity hardware;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    public Set<String> getConstraintValues() {
        return constraintValues;
    }

    public void setConstraintValues(Set<String> constraintValues) {
        this.constraintValues = constraintValues;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @ManyToOne
    @JoinColumn(name = "hardware_entity_id")
    public HardwareEntity getHardware() {
        return hardware;
    }

    public void setHardware(HardwareEntity hardware) {
        this.hardware = hardware;
    }

    public void update(final Constraint constraint) {
        setKey(constraint.getKey());
        setConstraintValues(constraint.getValues());
        setOperator(constraint.getOperator().toString());
        setHardware(this.hardware);
    }

    public Constraint toPojo() {
        return Constraint.builder()
                .key(key)
                .values(constraintValues)
                .operator(Constraint.Operator.valueOf(operator))
                .build();
    }

    public static HardwareConstraintEntity fromPojo(final Constraint constraint) {
        final HardwareConstraintEntity entity = new HardwareConstraintEntity();
        entity.update(constraint);
        return entity;
    }

}
