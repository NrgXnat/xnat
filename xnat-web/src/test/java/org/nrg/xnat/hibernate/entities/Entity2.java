package org.nrg.xnat.hibernate.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Entity2 extends AbstractHibernateEntity {
    @Serial
    private static final long serialVersionUID = -3105894934187799808L;

    private String name;

    private String description;

    private String twoness;
}
