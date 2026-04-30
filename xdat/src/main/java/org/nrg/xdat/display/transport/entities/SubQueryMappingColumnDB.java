/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;


/**
 * @author Tim Olsen
 *
 * SubQueryMappingColumns are used by DisplayFields which include SubQuery's to configure the values injected into the query.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "display_field_id", "queryField" }))
public class SubQueryMappingColumnDB {
    private long id;
    private String schemaField = "";
    private String queryField  = "";
    private DisplayFieldDB displayField=null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
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

    public String getSchemaField() {
        return schemaField;
    }

    public void setSchemaField(final String schemaField) {
        this.schemaField = schemaField;
    }

    public String getQueryField() {
        return queryField;
    }

    public void setQueryField(final String queryField) {
        this.queryField = queryField;
    }
}
