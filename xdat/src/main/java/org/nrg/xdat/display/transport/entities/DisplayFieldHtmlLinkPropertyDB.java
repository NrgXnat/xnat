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
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Tim Olsen
 *
 * HTML Link Property used to configure html links in display fields
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "display_field_id", "name" }))
public class DisplayFieldHtmlLinkPropertyDB extends AbstractHibernateEntity {
    private long id;
    private String name = "";
    private String value = "";
    private DisplayFieldDB displayField=null;

    private Map<String,String> insertedValues= new Hashtable<>();


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

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "insert_key")
    @Column(name = "insert_value")
    public Map<String, String> getInsertedValues() {
        return insertedValues;
    }

    public void setInsertedValues(final Map<String, String> insertedValues) {
        this.insertedValues = insertedValues;
    }
}
