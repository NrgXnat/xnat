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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.*;
import java.util.*;

/**
 * @author Tim Olsen
 *
 * Display Field object.  Used to configure the settings on a column for use in the search engine.
 */
@Entity
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = { "element_display_id", "fieldId" }))
public class DisplayFieldDB extends AbstractHibernateEntity {
    private String fieldId = "";
    private String header = "";
    private boolean image = false;
    private boolean searchable = false;
    private String dataType = null;
    private boolean visible = true;

    private String description = null;

    private List<DisplayFieldElementDB> elements;
    private Map<String, String> content;

    private boolean htmlContent;
    private String htmlLinkSecureLinkTo = null;
    private List<DisplayFieldHtmlLinkPropertyDB> htmlLinkProperties;

    private Map<String,String> htmlLinkSecureProps = null;
    private String sortBy = "";
    private String sortOrder = "ASC";

    private Integer htmlCellWidth = null;
    private Integer htmlCellHeight = null;
    private String htmlCellValign = null;
    private String htmlCellAlign = null;
    private String htmlCellServerLink = null;

    private boolean htmlImage;
    private Integer htmlImageWidth = null;
    private Integer htmlImageHeight = null;

    private int sortIndex = 0;

    private List<String> possibleValues = new ArrayList<>();


    private String subQuery       = "";
    private String subQueryValue  = "";
    private List<SubQueryMappingColumnDB> mappingColumns;

    public ElementDisplayDB elementDisplay;

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(mappedBy="displayField", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy
    public List<DisplayFieldElementDB> getElements() {
        if(elements==null){
            elements=Lists.newArrayList();
        }
        return elements;
    }

    public void addElement(DisplayFieldElementDB element){
        element.setDisplayField(this);

        if(elements==null){
            elements=Lists.newArrayList();
        }

        elements.add(element);
    }

    public void setElements(List<DisplayFieldElementDB> elements) {
        this.elements = elements;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DisplayFieldContent",
            joinColumns = {@JoinColumn(name = "df_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "format")
    @Column(name = "content",columnDefinition="TEXT")
    public Map<String, String> getContent() {
        if(content==null){
            content=new HashMap<>();
        }
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public boolean isHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(boolean htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getHtmlLinkSecureLinkTo() {
        return htmlLinkSecureLinkTo;
    }

    public void setHtmlLinkSecureLinkTo(String htmlLinkSecureLinkTo) {
        this.htmlLinkSecureLinkTo = htmlLinkSecureLinkTo;
    }

    @OneToMany(mappedBy="displayField", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<DisplayFieldHtmlLinkPropertyDB> getHtmlLinkProperties() {
        if(htmlLinkProperties==null){
            htmlLinkProperties=Lists.newArrayList();
        }
        return htmlLinkProperties;
    }

    public void setHtmlLinkProperties(List<DisplayFieldHtmlLinkPropertyDB> htmlLinkProperties) {
        this.htmlLinkProperties = htmlLinkProperties;
    }

    public void addHtmlLinkProperties(DisplayFieldHtmlLinkPropertyDB storedProp){
        storedProp.setDisplayField(this);
        if(this.htmlLinkProperties==null){
            this.htmlLinkProperties= Lists.newArrayList();
        }

        this.htmlLinkProperties.add(storedProp);
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DisplayFieldSecureProps",
            joinColumns = {@JoinColumn(name = "df_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "field_id")
    @Column(name = "schema_element_map",columnDefinition="TEXT")
    public Map<String, String> getHtmlLinkSecureProps() {
        if(htmlLinkSecureProps==null){
            htmlLinkSecureProps=new HashMap<>();
        }
        return htmlLinkSecureProps;
    }

    public void setHtmlLinkSecureProps(Map<String, String> htmlLinkSecureProps) {
        this.htmlLinkSecureProps = htmlLinkSecureProps;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getHtmlCellWidth() {
        return htmlCellWidth;
    }

    public void setHtmlCellWidth(Integer htmlCellWidth) {
        this.htmlCellWidth = htmlCellWidth;
    }

    public Integer getHtmlCellHeight() {
        return htmlCellHeight;
    }

    public void setHtmlCellHeight(Integer htmlCellHeight) {
        this.htmlCellHeight = htmlCellHeight;
    }

    public String getHtmlCellValign() {
        return htmlCellValign;
    }

    public void setHtmlCellValign(String htmlCellValign) {
        this.htmlCellValign = htmlCellValign;
    }

    public String getHtmlCellAlign() {
        return htmlCellAlign;
    }

    public void setHtmlCellAlign(String htmlCellAlign) {
        this.htmlCellAlign = htmlCellAlign;
    }

    public String getHtmlCellServerLink() {
        return htmlCellServerLink;
    }

    public void setHtmlCellServerLink(String htmlCellServerLink) {
        this.htmlCellServerLink = htmlCellServerLink;
    }

    public boolean isHtmlImage() {
        return htmlImage;
    }

    public void setHtmlImage(boolean htmlImage) {
        this.htmlImage = htmlImage;
    }

    public Integer getHtmlImageWidth() {
        return htmlImageWidth;
    }

    public void setHtmlImageWidth(Integer htmlImageWidth) {
        this.htmlImageWidth = htmlImageWidth;
    }

    public Integer getHtmlImageHeight() {
        return htmlImageHeight;
    }

    public void setHtmlImageHeight(Integer htmlImageHeight) {
        this.htmlImageHeight = htmlImageHeight;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getSubQueryValue() {
        return subQueryValue;
    }

    public void setSubQueryValue(String subQueryValue) {
        this.subQueryValue = subQueryValue;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(joinColumns=@JoinColumn(name="df_id"))
    @Column(name="possibleValue")
    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @ManyToOne
    @JoinColumn(name = "element_display_id")
    @JsonIgnore
    public ElementDisplayDB getElementDisplay() {
        return elementDisplay;
    }

    public void setElementDisplay(ElementDisplayDB elementDisplay) {
        this.elementDisplay = elementDisplay;
    }

    @Column(columnDefinition="TEXT")
    public String getSubQuery() {
        return subQuery;
    }

    public void setSubQuery(String subQuery) {
        this.subQuery = subQuery;
    }

    @OneToMany(mappedBy="displayField", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<SubQueryMappingColumnDB> getMappingColumns() {
        if(mappingColumns==null){
            mappingColumns=Lists.newArrayList();
        }
        return mappingColumns;
    }

    public void addMappingColumn(SubQueryMappingColumnDB mappingCol){
        mappingCol.setDisplayField(this);

        if(mappingColumns==null){
            mappingColumns=Lists.newArrayList();
        }

        mappingColumns.add(mappingCol);
    }

    public void setMappingColumns(List<SubQueryMappingColumnDB> mappingColumns) {
        this.mappingColumns = mappingColumns;
    }

}
