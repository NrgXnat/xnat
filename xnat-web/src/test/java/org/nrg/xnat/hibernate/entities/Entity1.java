package org.nrg.xnat.hibernate.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serial;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Entity1 extends AbstractHibernateEntity {
    @Serial
    private static final long serialVersionUID = -1021552722097817021L;

    private String name;

    private String description;

    private String oneness;
}
