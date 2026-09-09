/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * @author Tim Olsen
 *
 * Stored Views/Functions used by the search engine.
 */
@Entity
public class DisplayStoredViewDB extends AbstractHibernateEntity {
    String name;
    String sql;
    Integer sortOrder;

    boolean isFunction = false;
    String sourceType;


    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(columnDefinition="TEXT")
    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(final boolean function) {
        isFunction = function;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(final String sourceType) {
        this.sourceType = sourceType;
    }
}
