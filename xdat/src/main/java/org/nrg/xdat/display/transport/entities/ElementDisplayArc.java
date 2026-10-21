/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.*;
import java.util.Map;


/**
 * @author Tim Olsen
 *
 * Arcs are used in the search engine to configure join logic.  The Arc exists within an ElementDisplay and subscribes it to an ArcDefinition.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "element_display_id", "name" }))
public class ElementDisplayArc extends AbstractHibernateEntity{
    private String name;
    private Map<String,String> commonFields;

    private ElementDisplayDB elementDisplay;

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "common_field")
    @Column(name="field_id")
    public Map<String,String> getCommonFields() {
        if(commonFields==null){
            commonFields= Maps.newHashMap();
        }
        return commonFields;
    }

    public void setCommonFields(final Map<String,String> cf){
        commonFields=cf;
    }


    @ManyToOne
    @JoinColumn(name = "element_display_id")
    @JsonIgnore
    public ElementDisplayDB getElementDisplay() {
        return elementDisplay;
    }

    public void setElementDisplay(final ElementDisplayDB elementDisplay) {
        this.elementDisplay = elementDisplay;
    }
}
