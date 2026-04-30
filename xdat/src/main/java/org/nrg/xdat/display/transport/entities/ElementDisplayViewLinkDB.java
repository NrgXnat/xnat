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
import org.python.apache.commons.compress.utils.Lists;

import javax.persistence.*;
import java.util.List;


/**
 * @author Tim Olsen
 *
 * ViewLinks are configured within the scope of an ElementDisplay to configure how a view can be joined to data stored in a schema.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "element_display_id", "alias" }))
public class ElementDisplayViewLinkDB extends AbstractHibernateEntity {
    private String view;

    private String alias;

    private List<ElementDisplayViewLinkMappingColumnDB> columns;

    private ElementDisplayDB elementDisplay;

    public String getView() {
        return view;
    }

    public void setView(final String view){
        this.view=view;
    }

    @JoinColumn(nullable=false)
    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

    @OneToMany(mappedBy="viewLink", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<ElementDisplayViewLinkMappingColumnDB> getColumns() {
        return columns;
    }

    public void addColumn(final ElementDisplayViewLinkMappingColumnDB col){
        if(this.columns==null){
            this.columns= Lists.newArrayList();
        }

        this.columns.add(col);
        col.setViewLink(this);
    }

    public void setColumns(final List<ElementDisplayViewLinkMappingColumnDB> columns) {
        this.columns = columns;
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
