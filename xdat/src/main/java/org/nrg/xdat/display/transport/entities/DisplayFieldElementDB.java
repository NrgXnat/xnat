/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.*;

/**
 * @author Tim Olsen
 *
 * DisplayFieldElement is a child of DisplayField and is used to reference fields from a schema or a view for use in a configurable column.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(name = "UniqueNamePerField", columnNames = { "display_field_id", "name" }))
public class DisplayFieldElementDB extends AbstractHibernateEntity {
    private long id;
    private String name;
    private String schemaElementName;
    private String viewName;
    private String viewColumn;
    private String xdatType;
    private DisplayFieldDB displayField=null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSchemaElementName() {
        return schemaElementName;
    }

    public void setSchemaElementName(final String schemaElementName) {
        this.schemaElementName = schemaElementName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    public String getViewColumn() {
        return viewColumn;
    }

    public void setViewColumn(final String viewColumn) {
        this.viewColumn = viewColumn;
    }

    public String getXdatType() {
        return xdatType;
    }

    public void setXdatType(final String xdatType) {
        this.xdatType = xdatType;
    }

    @ManyToOne
    @JoinColumn(name = "display_field_id")
    @JsonIgnore
    public DisplayFieldDB getDisplayField() {
        return displayField;
    }

    public void setDisplayField(final DisplayFieldDB displayField) {
        this.displayField = displayField;
    }
}
