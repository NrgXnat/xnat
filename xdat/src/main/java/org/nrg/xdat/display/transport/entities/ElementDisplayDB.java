/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import org.apache.commons.lang.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.python.apache.commons.compress.utils.Lists;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.*;
import java.util.List;

/**
 * @author Tim Olsen
 *
 * Element Displays, modeled after the display.xml files, enable display configurations to be stored in the database.
 *
 * Element displays which correspond to xml files will be overlayed on top of the xml version during server startup.
 */
@Entity
public class ElementDisplayDB extends AbstractHibernateEntity  {
    private String elementName;
    private String valueField;
    private String displayField;
    private String displayLabel;
    private String briefDescription;
    private String fullDescription;
    private String lightColor;
    private String darkColor;
    private Integer sortOrder;

    private List<DisplayFieldDB> displayFields;
    private List<ElementDisplayArc> arcs;
    private List<DisplayVersionDB> displayVersions;
    private List<ElementDisplayViewLinkDB> viewLinks;

    @Column(unique = true, nullable = false)
    public String getElementName() {
        return elementName;
    }

    public void setElementName(final String elementName) {
        this.elementName = elementName;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(final String valueField) {
        this.valueField = valueField;
    }

    public String getDisplayField() {
        return displayField;
    }

    public void setDisplayField(final String displayField) {
        this.displayField = displayField;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(final String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(final String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(final String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getLightColor() {
        return lightColor;
    }

    public void setLightColor(final String lightColor) {
        this.lightColor = lightColor;
    }

    public String getDarkColor() {
        return darkColor;
    }

    public void setDarkColor(final String darkColor) {
        this.darkColor = darkColor;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @OneToMany(mappedBy="elementDisplay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<DisplayFieldDB> getDisplayFields() {
        if(displayFields==null){
            displayFields= Lists.newArrayList();
        }
        return displayFields;
    }

    public void setDisplayFields(final List<DisplayFieldDB> displayFields) {
        this.displayFields = displayFields;
    }

    @OneToMany(mappedBy="elementDisplay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<ElementDisplayArc> getArcs() {
        if(arcs==null){
            arcs=Lists.newArrayList();
        }

        return arcs;
    }

    public void setArcs(final List<ElementDisplayArc> arcs) {
        this.arcs = arcs;
    }

    public void addArc(final ElementDisplayArc arc) {
        arc.setElementDisplay(this);
        this.getArcs().add(arc);
    }

    public ElementDisplayArc getArcByName(final String name){
        for(final ElementDisplayArc arc : getArcs()){
            if(StringUtils.equals(arc.getName(),name)){
                return arc;
            }
        }

        return null;
    }

    @OneToMany(mappedBy="elementDisplay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<DisplayVersionDB> getDisplayVersions() {
        if(displayVersions==null){
            displayVersions=Lists.newArrayList();
        }
        return displayVersions;
    }

    public void setDisplayVersions(final List<DisplayVersionDB> displayVersions) {
        this.displayVersions = displayVersions;
    }

    @OneToMany(mappedBy="elementDisplay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<ElementDisplayViewLinkDB> getViewLinks() {
        if(viewLinks==null){
            viewLinks=Lists.newArrayList();
        }
        return viewLinks;
    }

    @Transient
    public ElementDisplayViewLinkDB getViewLinkByAlias(final String alias){
        for(final ElementDisplayViewLinkDB vl: getViewLinks()){
            if(StringUtils.equals(vl.getAlias(),alias)){
                return vl;
            }
        }

        return null;
    }

    public void setViewLinks(final List<ElementDisplayViewLinkDB> viewLinks) {
        this.viewLinks = viewLinks;
    }

    public void addViewLink(final ElementDisplayViewLinkDB viewLink){
        viewLink.setElementDisplay(this);
        this.getViewLinks().add(viewLink);
    }

    @Transient
    public void addDisplayField(final DisplayFieldDB field){
        if(displayFields==null){
            displayFields=Lists.newArrayList();
        }

        field.setElementDisplay(this);
        displayFields.add(field);
    }

    @Transient
    public DisplayFieldDB getDisplayField(final String fieldId){
        for(final DisplayFieldDB df : this.getDisplayFields()){
            if (StringUtils.equals(df.getFieldId(), fieldId)) {
                return df;
            }
        }

        return null;
    }

    @Transient
    public void addDisplayVersion(final DisplayVersionDB version){
        if(displayVersions==null){
            displayVersions=Lists.newArrayList();
        }

        version.setElementDisplay(this);
        displayVersions.add(version);
    }

    @Transient
    public DisplayVersionDB getDisplayVersion(final String versionId){
        for(final DisplayVersionDB dv : this.getDisplayVersions()){
            if (StringUtils.equals(dv.getVersionName(), versionId)) {
                return dv;
            }
        }

        return null;
    }
}
