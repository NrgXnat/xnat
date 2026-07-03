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
import org.hibernate.validator.constraints.NotBlank;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

// TODO: Add JSON properties for testing. Support for JSON types across PostgreSQL and H2 doesn't work properly with the
//  current way of configuring json and jsonb columns: see https://github.com/vladmihalcea/hibernate-types/issues/179 for
//  info on possible fixes.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
public class SimpleEntity extends AbstractHibernateEntity {
    private static final long serialVersionUID = 3584333785184537428L;

    @Builder
    public SimpleEntity(final String name, final String username, final String description, final Long value, final Integer total, final Float price, final Double warpSpeed, final Date disabled, final Date timestamp, final Date created, final Boolean enabled, final Long id) {
        setName(name);
        setUsername(username);
        setDescription(description);
        setValue(value);
        setTotal(total);
        setPrice(price);
        setWarpSpeed(warpSpeed);
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

    @NotBlank
    @Size(max = 100)
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    @NotBlank
    @Size(max = 100)
    public String getUsername() {
        return _username;
    }

    public void setUsername(final String username) {
        _username = username;
    }

    @NotBlank
    @Size(max = 100)
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    @SuppressWarnings("unused")
    @NotNull
    public Long getValue() {
        return _value;
    }

    public void setValue(final Long value) {
        _value = value;
    }

    @SuppressWarnings("unused")
    @NotNull
    public Integer getTotal() {
        return _total;
    }

    public void setTotal(final Integer total) {
        _total = total;
    }

    @SuppressWarnings("unused")
    @NotNull
    public Float getPrice() {
        return _price;
    }

    public void setPrice(final Float price) {
        _price = price;
    }

    @SuppressWarnings("unused")
    @NotNull
    public Double getWarpSpeed() {
        return _warpSpeed;
    }

    public void setWarpSpeed(final Double warpSpeed) {
        _warpSpeed = warpSpeed;
    }

    private String  _name;
    private String  _username;
    private String  _description;
    private Long    _value;
    private Integer _total;
    private Float   _price;
    private Double  _warpSpeed;
}
