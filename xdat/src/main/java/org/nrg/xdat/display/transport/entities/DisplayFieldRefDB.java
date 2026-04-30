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
 * DisplayFieldRef used by DisplayVersions to reference DisplayField Objects and configure how they are displayed.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "display_version_id", "fieldId","elementName","value" }))
public class DisplayFieldRefDB {
    private long id;
    private String header = null;
    private String fieldId;
    private String elementName = null;
    private Boolean visible = true;
    private String type = null;
    private Integer sortOrder;
    private String value = null;

    private DisplayVersionDB displayVersion=null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    @Column(nullable = false)
    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(final String fieldId) {
        this.fieldId = fieldId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(final String elementName) {
        this.elementName = elementName;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @ManyToOne
    @JoinColumn(name = "display_version_id")
    @JsonIgnore
    public DisplayVersionDB getDisplayVersion() {
        return displayVersion;
    }

    public void setDisplayVersion(final DisplayVersionDB displayField) {
        this.displayVersion = displayField;
    }
}
