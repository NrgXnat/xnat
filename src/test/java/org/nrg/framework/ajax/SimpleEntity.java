/*
 * framework: org.nrg.framework.ajax.SimpleEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class SimpleEntity extends AbstractHibernateEntity {

    /**
     * for Hibernate
     */
    public SimpleEntity() {
    }

    public SimpleEntity(final String name, final String description, final Integer total) {
        _name = name;
        _description = description;
        _total = total;
    }

    @NotEmpty
    @Size(max = 100)
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    @NotEmpty
    @Size(max = 100)
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String host) {
        _description = host;
    }

    @NotNull
    public Integer getTotal() {
        return _total;
    }

    public void setTotal(final Integer storagePort) {
        _total = storagePort;
    }

    private String  _name;
    private String  _description;
    private Integer _total;
}
