/*
 * framework: org.nrg.framework.ajax.SimpleEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@AllArgsConstructor
@NoArgsConstructor
public class SimpleEntity extends AbstractHibernateEntity {
    @Builder
    public SimpleEntity(final Integer total, final String description, final String name, final Date disabled, final Date timestamp, final Date created, final Boolean enabled, final Long id) {
        setName(name);
        setDescription(description);
        setTotal(total);
        if (id != null) {
            setId(id);
        }
        if (created != null) {
            setCreated(created);
        }
        if (enabled != null) {
            setEnabled(enabled);
        }
        if (timestamp != null) {
            setTimestamp(timestamp);
        }
        if (disabled != null) {
            setDisabled(disabled);
        }
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

    public void setDescription(final String description) {
        _description = description;
    }

    @NotNull
    public Integer getTotal() {
        return _total;
    }

    public void setTotal(final Integer total) {
        _total = total;
    }

    /*
    Support for JSON types across PostgreSQL and H2 doesn't work properly with the current way of configuring json and jsonb columns: see https://github.com/vladmihalcea/hibernate-types/issues/179 for info on possible fixes

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public JsonNode getAttributes() {
        return _attributes;
    }

    public void setAttributes(final JsonNode attributes) {
        _attributes = attributes;
    }
    */

    private String  _name;
    private String  _description;
    private Integer _total;
    // private JsonNode _attributes;
}
