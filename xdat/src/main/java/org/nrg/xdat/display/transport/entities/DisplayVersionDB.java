/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;

/**
 * @author Tim Olsen
 *
 * Display Versions are configurable lists of columns to be displayed via XNAT's search engines.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "element_display_id", "versionName" }))
public class DisplayVersionDB extends AbstractHibernateEntity {
    private String versionName = "";
    private String defaultOrderBy = "";
    private String defaultSortOrder = "";
    private String briefDescription = "";
    private String darkColor = "";
    private String lightColor = "";
    private boolean allowDiffs = true;

    private Integer htmlCellWidth = null;
    private Integer htmlCellHeight = null;
    private String htmlCellValign = null;
    private String htmlCellAlign = null;
    private String htmlCellServerLink = null;

    public ElementDisplayDB elementDisplay;

    private List<DisplayFieldRefDB> fields=null;


    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public String getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public void setDefaultOrderBy(final String defaultOrderBy) {
        this.defaultOrderBy = defaultOrderBy;
    }

    public String getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(final String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(final String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getDarkColor() {
        return darkColor;
    }

    public void setDarkColor(final String darkColor) {
        this.darkColor = darkColor;
    }

    public String getLightColor() {
        return lightColor;
    }

    public void setLightColor(final String lightColor) {
        this.lightColor = lightColor;
    }

    public boolean isAllowDiffs() {
        return allowDiffs;
    }

    public void setAllowDiffs(final boolean allowDiffs) {
        this.allowDiffs = allowDiffs;
    }

    public Integer getHtmlCellWidth() {
        return htmlCellWidth;
    }

    public void setHtmlCellWidth(final Integer htmlCellWidth) {
        this.htmlCellWidth = htmlCellWidth;
    }

    public Integer getHtmlCellHeight() {
        return htmlCellHeight;
    }

    public void setHtmlCellHeight(final Integer htmlCellHeight) {
        this.htmlCellHeight = htmlCellHeight;
    }

    public String getHtmlCellValign() {
        return htmlCellValign;
    }

    public void setHtmlCellValign(final String htmlCellValign) {
        this.htmlCellValign = htmlCellValign;
    }

    public String getHtmlCellAlign() {
        return htmlCellAlign;
    }

    public void setHtmlCellAlign(final String htmlCellAlign) {
        this.htmlCellAlign = htmlCellAlign;
    }

    public String getHtmlCellServerLink() {
        return htmlCellServerLink;
    }

    public void setHtmlCellServerLink(final String htmlCellServerLink) {
        this.htmlCellServerLink = htmlCellServerLink;
    }

    @OneToMany(mappedBy="displayVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<DisplayFieldRefDB> getFields() {
        if(fields==null){
            fields=Lists.newArrayList();
        }

        fields.sort(new Comparator<DisplayFieldRefDB>() {
            @Override
            public int compare(final DisplayFieldRefDB o1, final DisplayFieldRefDB o2) {
                final int v1 = (o1.getSortOrder()==null)?0:o1.getSortOrder();
                final int v2 = (o2.getSortOrder()==null)?0:o2.getSortOrder();
                return NumberUtils.compare(v1,v2);
            }
        });

        return fields;
    }

    @Transient
    public DisplayFieldRefDB getFieldById(final String id, final String element, final String v){
        for(final DisplayFieldRefDB df: getFields()){
            if(v==null) {
                if (StringUtils.equals(id, df.getFieldId()) && (StringUtils.equals(element, df.getElementName()))) {
                    return df;
                }
            }else{
                if (StringUtils.equals(id, df.getFieldId()) && (StringUtils.equals(element, df.getElementName())) && StringUtils.equals(df.getValue(),v)) {
                    return df;
                }
            }
        }

        return null;
    }

    public void setFields(final List<DisplayFieldRefDB> fields) {
        this.fields = fields;
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


    /**
     * Copy's the key fields from another DisplayVersion into this object.
     * @param dv
     */
    @Transient
    public void copyFrom(final DisplayVersionDB dv){
        this.setVersionName(dv.getVersionName());
        this.setDarkColor(dv.getDarkColor());
        this.setLightColor(dv.getLightColor());
        this.setBriefDescription(dv.getBriefDescription());
        this.setHtmlCellAlign(dv.getHtmlCellAlign());
        this.setHtmlCellHeight(dv.getHtmlCellHeight());
        this.setHtmlCellValign(dv.getHtmlCellValign());
        this.setHtmlCellWidth(dv.getHtmlCellWidth());
        this.setHtmlCellServerLink(dv.getHtmlCellServerLink());
        this.setAllowDiffs(dv.isAllowDiffs());
        this.setDefaultOrderBy(dv.getDefaultOrderBy());
        this.setDefaultSortOrder(dv.getDefaultSortOrder());

        final List<DisplayFieldRefDB> stillThere= Lists.newArrayList();
        int i=0;
        for(final DisplayFieldRefDB dfr: dv.getFields()){
            DisplayFieldRefDB storedDFR = this.getFieldById(dfr.getFieldId(),dfr.getElementName(),dfr.getValue());
            if(storedDFR!=null){
                storedDFR.setSortOrder(dfr.getSortOrder());
                storedDFR.setHeader(dfr.getHeader());
            }else{
                storedDFR = new DisplayFieldRefDB();
                storedDFR.setElementName(dfr.getElementName());
                storedDFR.setHeader(dfr.getHeader());
                storedDFR.setFieldId(dfr.getFieldId());
                storedDFR.setVisible(dfr.getVisible());
                storedDFR.setSortOrder(dfr.getSortOrder());
                storedDFR.setValue(dfr.getValue());
                this.addField(storedDFR);
            }
            stillThere.add(storedDFR);
        }

        //remove absent fields
        final List<DisplayFieldRefDB> toRemove = Lists.newArrayList();
        for(final DisplayFieldRefDB dfr: this.getFields()){
            if(!stillThere.contains(dfr)){
                toRemove.add(dfr);
            }
        }
        for(final DisplayFieldRefDB dfr : toRemove){
            this.getFields().remove(dfr);
        }
    }

    private void addField(final DisplayFieldRefDB storedDFR) {
        storedDFR.setDisplayVersion(this);

        this.getFields().add(storedDFR);
    }
}
