/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.display.*;
import org.nrg.xdat.display.transport.dao.ElementDisplayDAO;
import org.nrg.xdat.display.transport.entities.*;
import org.nrg.xdat.display.transport.entities.DisplayFieldDB;
import org.nrg.xdat.display.transport.entities.DisplayFieldElementDB;
import org.nrg.xdat.display.transport.entities.DisplayFieldRefDB;
import org.nrg.xdat.display.transport.entities.DisplayVersionDB;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * @author Tim Olsen
 *
 * Service implementation for ElementDisplays stored via Hibernate.
 */

@Service
public class HibernateElementDisplayService extends AbstractHibernateEntityService<ElementDisplayDB, ElementDisplayDAO> implements ElementDisplayStorageService {
    @Autowired
    public HibernateElementDisplayService(final ElementDisplayDAO dao) {
        _dao = dao;
    }

    @Override
    protected ElementDisplayDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateElementDisplayService.class);

    private final ElementDisplayDAO _dao;

    @Override
    public ElementDisplayDB findByElementName(final String elementName) {
        return getDao().findByElementName(elementName, false);
    }

    @Override
    @Transactional
    public DisplayVersionDB convertDisplayVersion(final org.nrg.xdat.display.DisplayVersion dv) {
        final DisplayVersionDB storedVersion = new DisplayVersionDB();

        storedVersion.setVersionName(dv.getVersionName());
        storedVersion.setDarkColor(dv.getDarkColor());
        storedVersion.setLightColor(dv.getLightColor());
        storedVersion.setBriefDescription(dv.getBriefDescription());
        storedVersion.setHtmlCellAlign(dv.getHeaderCell().getAlign());
        storedVersion.setHtmlCellHeight(dv.getHeaderCell().getHeight());
        storedVersion.setHtmlCellValign(dv.getHeaderCell().getValign());
        storedVersion.setHtmlCellWidth(dv.getHeaderCell().getWidth());
        storedVersion.setHtmlCellServerLink(dv.getHeaderCell().getServerLink());
        storedVersion.setAllowDiffs(dv.isAllowDiffs());
        storedVersion.setDefaultOrderBy(dv.getDefaultOrderBy());
        storedVersion.setDefaultSortOrder(dv.getDefaultSortOrder());

        final List<DisplayFieldRefDB> dfrs = Lists.newArrayList();

        int i = 0;
        for (final Object dfO : dv.getSortedDisplayFieldRefs()) {
            final org.nrg.xdat.display.DisplayFieldRef dfr = (org.nrg.xdat.display.DisplayFieldRef) dfO;

            DisplayFieldRefDB storedDFR = new DisplayFieldRefDB();
            storedDFR.setElementName(dfr.getElementName());
            storedDFR.setHeader(dfr.getHeader());
            storedDFR.setFieldId(dfr.getId());
            storedDFR.setVisible(dfr.getVisible());
            storedDFR.setValue((String) dfr.getValue());
            storedDFR.setSortOrder(i++);

            dfrs.add(storedDFR);
            storedDFR.setDisplayVersion(storedVersion);
        }

        storedVersion.setFields(dfrs);
        return storedVersion;
    }

    private org.nrg.xdat.display.ElementDisplay renderElementDisplay(final String elementName) {
        final  ElementDisplayDB storedED = findByElementName(elementName);

        if (storedED == null) {
            return null;
        } else {
            return renderElementDisplay(storedED);
        }
    }

    private org.nrg.xdat.display.DisplayField renderDisplayField(final DisplayFieldDB storedDF, final org.nrg.xdat.display.ElementDisplay ed) {
        final org.nrg.xdat.display.DisplayField df;
        if (StringUtils.isNotBlank(storedDF.getSubQuery())) {
            df = new SQLQueryField(ed);
        } else {
            df = new org.nrg.xdat.display.DisplayField(ed);
        }

        df.setId(storedDF.getFieldId());
        df.setHeader(storedDF.getHeader());
        df.setImage(storedDF.isImage());
        df.setVisible(storedDF.isVisible());
        df.setSearchable(storedDF.isSearchable());
        df.setDataType(storedDF.getDataType());
        df.setSortBy(storedDF.getSortBy());
        df.setSortOrder(storedDF.getSortOrder());
        df.setHtmlContent(storedDF.isHtmlContent());
        df.setDescription(storedDF.getDescription());

        for (DisplayFieldElementDB storedDFE : storedDF.getElements()) {
            final org.nrg.xdat.display.DisplayFieldElement dfe = new org.nrg.xdat.display.DisplayFieldElement();

            dfe.setName(storedDFE.getName());
            dfe.setSchemaElementName(storedDFE.getSchemaElementName());
            dfe.setViewColumn(storedDFE.getViewColumn());
            dfe.setViewName(storedDFE.getViewName());
            dfe.setXdatType(storedDFE.getXdatType());

            df.addDisplayFieldElement(dfe);
        }

        for (Map.Entry<String, String> entry : storedDF.getContent().entrySet()) {
            df.getContent().put(entry.getKey(), entry.getValue());
        }

        df.getHtmlCell().setAlign(storedDF.getHtmlCellAlign());
        df.getHtmlCell().setHeight(storedDF.getHtmlCellHeight());
        df.getHtmlCell().setWidth(storedDF.getHtmlCellWidth());
        df.getHtmlCell().setValign(storedDF.getHtmlCellValign());
        df.getHtmlCell().setServerLink(storedDF.getHtmlCellServerLink());

        //configure HTML Link
        final HTMLLink htmlLink = new HTMLLink();
        for (DisplayFieldHtmlLinkPropertyDB linkProp : storedDF.getHtmlLinkProperties()) {
            HTMLLinkProperty prop = new HTMLLinkProperty();
            prop.setName(linkProp.getName());
            prop.setValue(linkProp.getValue());

            for (Map.Entry<String, String> entry : linkProp.getInsertedValues().entrySet()) {
                if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
                    prop.addInsertedValue(entry.getKey(), entry.getValue());
                }
            }

            htmlLink.addProperty(prop);
        }

        for (final Map.Entry<String, String> entry : storedDF.getHtmlLinkSecureProps().entrySet()) {
            if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
                htmlLink.getSecureProps().put(entry.getKey(), entry.getValue());
            }
        }

        htmlLink.setSecureLinkTo(storedDF.getHtmlLinkSecureLinkTo());

        if (htmlLink.getSecureLinkTo() != null || htmlLink.getProperties().size() > 0 || htmlLink.getSecureProps().size() > 0) {
            df.setHtmlLink(htmlLink);
        }

        //html image settings
        df.getHtmlImage().setWidth(storedDF.getHtmlImageWidth());
        df.getHtmlImage().setHeight(storedDF.getHtmlImageHeight());

        //sub-query settings
        if (df instanceof SQLQueryField) {
            final SQLQueryField sqf = (SQLQueryField) df;
            sqf.setSubQuery(storedDF.getSubQuery());
            for (final SubQueryMappingColumnDB col : storedDF.getMappingColumns()) {
                sqf.addMappingColumn(col.getSchemaField(), col.getQueryField());
            }
        }
        return df;
    }

    @Override
    public org.nrg.xdat.display.ElementDisplay renderElementDisplay(ElementDisplayDB storedED) {
        final org.nrg.xdat.display.ElementDisplay ed = new org.nrg.xdat.display.ElementDisplay();
        ed.setElementName(storedED.getElementName());

        ed.setValueField(storedED.getValueField());
        ed.setDisplayField(storedED.getDisplayField());
        ed.setDisplayLabel(storedED.getDisplayLabel());
        ed.setBriefDescription(storedED.getBriefDescription());
        ed.setFullDescription(storedED.getFullDescription());

        for (final DisplayFieldDB storedDF : storedED.getDisplayFields()) {
            final org.nrg.xdat.display.DisplayField df = renderDisplayField(storedDF, ed);

            try {
                ed.addDisplayFieldWException(df);
            } catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
                _log.error(df.getParentDisplay().getElementName() + "." + df.getId());
                _log.error("DisplayField addition failed during stored display field rendering", e);
            }
        }

        for (final DisplayVersionDB storedDV : storedED.getDisplayVersions()) {
            org.nrg.xdat.display.DisplayVersion dv = renderDisplayVersion(storedDV, ed);

            ed.addVersion(dv);
        }

        //arc registration
        for (final ElementDisplayArc storedARC : storedED.getArcs()) {
            Arc arc = new Arc();
            arc.setName(storedARC.getName());
            for (Map.Entry<String, String> entry : storedARC.getCommonFields().entrySet()) {
                arc.addCommonField(entry.getKey(), entry.getValue());
            }
            ed.addArc(arc);
        }

        //view registrations
        for (final ElementDisplayViewLinkDB storedVL : storedED.getViewLinks()) {
            final ViewLink link = new ViewLink();
            link.setAlias(storedVL.getAlias());

            final Mapping m = new Mapping();
            m.setTableName(storedVL.getView());

            for (final ElementDisplayViewLinkMappingColumnDB storedCOL : storedVL.getColumns()) {
                final MappingColumn mc = new MappingColumn();
                mc.setFieldElementXMLPath(storedCOL.getFieldElementXMLPath());
                mc.setMapsTo(storedCOL.getMapsTo());
                mc.setRootElement(storedCOL.getRootElement());
                m.addColumn(mc);
            }
            link.setMapping(m);
            ed.addViewLink(link);
        }

        return ed;
    }

    private org.nrg.xdat.display.DisplayVersion renderDisplayVersion(DisplayVersionDB storedDV, org.nrg.xdat.display.ElementDisplay ed) {
        final org.nrg.xdat.display.DisplayVersion dv = new org.nrg.xdat.display.DisplayVersion();
        dv.setParentElementDisplay(ed);

        dv.setVersionName(storedDV.getVersionName());
        dv.setDefaultOrderBy(storedDV.getDefaultOrderBy());
        dv.setDefaultSortOrder(storedDV.getDefaultSortOrder());
        dv.setBriefDescription(storedDV.getBriefDescription());
        dv.setDarkColor(storedDV.getDarkColor());
        dv.setLightColor(storedDV.getLightColor());
        dv.setAllowDiffs(storedDV.isAllowDiffs());

        for (DisplayFieldRefDB dfr : storedDV.getFields()) {
            final org.nrg.xdat.display.DisplayFieldRef df = new org.nrg.xdat.display.DisplayFieldRef(dv);
            df.setElementName(dfr.getElementName());
            df.setId(dfr.getFieldId());
            df.setType(dfr.getType());
            df.setHeader(dfr.getHeader());
            df.setValue(dfr.getValue());
            if (dfr.getVisible() != null)
                df.setVisible(Boolean.toString(dfr.getVisible()));
            try {
                if (df.getElementName().equals("")) {
                    ed.getDisplayFieldWException(df.getId());
                }
                dv.addDisplayField(df);
            } catch (DisplayFieldRefCollection.DuplicateDisplayFieldRefException e) {
                _log.error("Duplicate display field during stored display field rendering", e);
            } catch (DisplayFieldCollection.DisplayFieldNotFoundException e) {
                _log.error("Display field not found during stored display field rendering", e);
            }
        }

        dv.getHeaderCell().setWidth(storedDV.getHtmlCellWidth());
        dv.getHeaderCell().setHeight(storedDV.getHtmlCellHeight());
        dv.getHeaderCell().setValign(storedDV.getHtmlCellValign());
        dv.getHeaderCell().setAlign(storedDV.getHtmlCellAlign());
        dv.getHeaderCell().setServerLink(storedDV.getHtmlCellServerLink());

        return dv;
    }

    private DisplayFieldDB registerDisplayField(org.nrg.xdat.display.DisplayField df, ElementDisplayDB storedDisplay) {
        DisplayFieldDB storedDF = storedDisplay.getDisplayField(df.getId());
        if (storedDF != null) {
            //don't modify existing fields
        } else {
            storedDF = new DisplayFieldDB();
            storedDF.setDataType(df.getDataType());
            storedDF.setDescription(df.getDescription());
            storedDF.setHeader(df.getHeader());
            storedDF.setFieldId(df.getId());
            storedDF.setSearchable(df.isSearchable());
            storedDF.setVisible(df.isVisible());
            storedDF.setImage(df.isImage());
            storedDF.setSortBy(df.getSortBy());
            storedDF.setSortOrder(df.getSortOrder());
            storedDF.setSortIndex(df.getSortIndex());

            if (df.getHtmlCell() != null) {
                storedDF.setHtmlCellAlign(df.getHtmlCell().getAlign());
                storedDF.setHtmlCellValign(df.getHtmlCell().getValign());
                storedDF.setHtmlCellHeight(df.getHtmlCell().getHeight());
                storedDF.setHtmlCellWidth(df.getHtmlCell().getWidth());
                storedDF.setHtmlCellServerLink(df.getHtmlCell().getServerLink());
            }
            if (df.getHtmlImage() != null) {
                storedDF.setHtmlImage(true);
                storedDF.setHtmlImageHeight(df.getHtmlImage().getHeight());
                storedDF.setHtmlImageWidth(df.getHtmlImage().getWidth());
            }

            for (final org.nrg.xdat.display.DisplayFieldElement dfe : df.getElements()) {
                DisplayFieldElementDB storedDFE = new DisplayFieldElementDB();
                storedDFE.setSchemaElementName(dfe.getSchemaElementName());
                storedDFE.setName(dfe.getName());
                storedDFE.setViewColumn(dfe.getViewColumn());
                storedDFE.setViewName(dfe.getViewName());
                storedDFE.setXdatType(dfe.getXdatType());
                storedDF.addElement(storedDFE);
            }

            if (df instanceof SQLQueryField) {
                storedDF.setSubQuery(((SQLQueryField) df).getSubQuery());

                for (SQLQueryField.QueryMappingColumn qmc : ((SQLQueryField) df).getMappingColumns()) {
                    SubQueryMappingColumnDB storedQMC = new SubQueryMappingColumnDB();
                    storedQMC.setQueryField(qmc.getQueryField());
                    storedQMC.setSchemaField(qmc.getSchemaField());
                    storedDF.addMappingColumn(storedQMC);
                }
            }

            if (df.getHtmlLink() != null) {
                final HTMLLink link = df.getHtmlLink();
                storedDF.setHtmlLinkSecureLinkTo(link.getSecureLinkTo());

                if (link.getProperties() != null) {
                    for (HTMLLinkProperty prop : link.getProperties()) {
                        DisplayFieldHtmlLinkPropertyDB storedProp = new DisplayFieldHtmlLinkPropertyDB();
                        storedProp.setName(prop.getName());
                        storedProp.setValue(prop.getValue());

                        Map<String, String> set = Maps.newHashMap();
                        for (Map.Entry<String, String> entry : prop.getInsertedValues().entrySet()) {
                            set.put(entry.getKey(), entry.getValue());
                        }

                        if (set.size() > 0) {
                            storedProp.setInsertedValues(set);
                        }

                        storedDF.addHtmlLinkProperties(storedProp);
                    }
                }


                if (link.getSecureProps() != null) {
                    final Map<String, String> secureProps = Maps.newHashMap();
                    for (final Map.Entry<String, String> entry : ((Map<String, String>) link.getSecureProps()).entrySet()) {
                        secureProps.put(entry.getKey(), entry.getValue());
                    }

                    if (secureProps.size() > 0) {
                        storedDF.setHtmlLinkSecureProps(secureProps);
                    }
                }
            }

            if (df.getContent().size() > 0) {
                final Map<String, String> content = Maps.newHashMap();
                for (final Map.Entry<String, String> entry : df.getContent().entrySet()) {
                    content.put(entry.getKey(), entry.getValue());
                }

                storedDF.setContent(content);
            }

            storedDisplay.addDisplayField(storedDF);
        }
        return storedDF;
    }

    private void registerElementDisplay(final org.nrg.xdat.display.ElementDisplay ed) {
        ElementDisplayDB storedDisplay = findByElementName(ed.getElementName());

        if(storedDisplay==null){
            storedDisplay = new ElementDisplayDB();
        }

        storedDisplay.setElementName(ed.getElementName());
        storedDisplay.setDisplayField(ed.getDisplayField());
        storedDisplay.setDisplayLabel(ed.getDisplayLabel());
        storedDisplay.setBriefDescription(ed.getBriefDescription());
        storedDisplay.setFullDescription(ed.getFullDescription());
        storedDisplay.setDarkColor(ed.getDarkColor());
        storedDisplay.setLightColor(ed.getLightColor());
        storedDisplay.setSortOrder(ed.getSequence());

        //iterate the actual display fields
        for(final org.nrg.xdat.display.DisplayField df : ed.getSortedFields()){
            this.registerDisplayField(df,storedDisplay);
        }

        //iterate the displayVersions
        for(final Map.Entry<String, org.nrg.xdat.display.DisplayVersion> entry : ed.getVersions().entrySet()){
            final org.nrg.xdat.display.DisplayVersion dv = entry.getValue();
            DisplayVersionDB storedVersion = storedDisplay.getDisplayVersion(entry.getKey());

            if(storedVersion!=null){
                //already exists
            }else{
                storedVersion = convertDisplayVersion(dv);

                storedVersion.setElementDisplay(storedDisplay);
                storedDisplay.addDisplayVersion(storedVersion);
            }
        }

        for(final Object entry : ed.getViewLinks().values()){
            final ViewLink vl = (ViewLink) entry;

            ElementDisplayViewLinkDB storedVL=storedDisplay.getViewLinkByAlias(vl.getAlias());
            if(storedVL !=null){
                //already registered
                continue;
            }

            storedVL=new ElementDisplayViewLinkDB();
            storedVL.setAlias(vl.getAlias());
            storedVL.setView(vl.getMapping().getTableName());

            for(Object obj : vl.getMapping().getColumns()){
                MappingColumn mc = (MappingColumn) obj;

                final ElementDisplayViewLinkMappingColumnDB storedMC = new ElementDisplayViewLinkMappingColumnDB();
                storedMC.setRootElement(mc.getRootElement());
                storedMC.setFieldElementXMLPath(mc.getFieldElementXMLPath());
                storedMC.setMapsTo(mc.getMapsTo());

                storedVL.addColumn(storedMC);
            }

            storedDisplay.addViewLink(storedVL);
        }

        for(final Object obj : ed.getArcs().values()){
            final Arc arc = (Arc) obj;

            ElementDisplayArc storedArc=storedDisplay.getArcByName(arc.getName());
            if(storedArc!=null){
                continue;
            }else{
                storedArc = new ElementDisplayArc();
                storedArc.setName(arc.getName());

                for(final Map.Entry<String,String> entry: arc.getCommonFields().entrySet()){
                    storedArc.getCommonFields().put(entry.getKey(),entry.getValue());
                }
            }

            storedDisplay.addArc(storedArc);

            ArcDefinition arcDefinition = DisplayManager.GetInstance().getArcDefinition(arc.getName());
            if(arcDefinition!=null){
                arcDefinition.addMember(ed.getElementName());
            }
        }

        this.getDao().create(storedDisplay);
    }

    @Override
    @Transactional
    public List<org.nrg.xdat.display.ElementDisplay> findAllActive() {
        final List<ElementDisplayDB> dbVersions = getDao().findAllEnabled();
        final List<org.nrg.xdat.display.ElementDisplay> eds = Lists.newArrayList();
        for(ElementDisplayDB ed: dbVersions){
            eds.add(this.renderElementDisplay(ed));
        }
        return eds;
    }

    @Override
    @Transactional
    public void addDisplayField(final String elementName, final DisplayFieldDB df) throws NoSuchElementException, FieldAlreadyExists {
        final ElementDisplayDB ed =this.findByElementName(elementName);
        if(ed==null){
            throw new NoSuchElementException(elementName);
        }

        if(ed.getDisplayField(df.getFieldId())!=null){
            throw new FieldAlreadyExists(df.getFieldId());
        }

        ed.addDisplayField(df);

        this.getDao().update(ed);

        //add to in-memory cache
        final org.nrg.xdat.display.ElementDisplay elementDisplay = DisplayManager.GetElementDisplay(elementName);
        elementDisplay.addDisplayField(renderDisplayField(df,elementDisplay));
    }



    @Override
    @Transactional
    public org.nrg.xdat.display.DisplayField addSqlQueryValue(final String elementName, final String displayField, final String header, final String value) throws NoFieldFound, NoSuchElementException {
        //get xml loaded ED
        final org.nrg.xdat.display.ElementDisplay ed = DisplayManager.GetElementDisplay(elementName);
        if(ed==null){
            throw new NoSuchElementException(elementName);
        }

        //use the rendered version of the DF so none of the hibernate keys are in there.
        final org.nrg.xdat.display.DisplayField df = ed.getDisplayField(displayField);
        if(df==null){
            throw new NoFieldFound(displayField);
        }

        //get db loaded ED
        ElementDisplayDB storedDisplay =this.findByElementName(elementName);
        if(storedDisplay==null){
            storedDisplay = new ElementDisplayDB();
            storedDisplay.setElementName(elementName);
            this.getDao().create(storedDisplay);
        }

        if(!(df instanceof SQLQueryField)){
            throw new NoFieldFound(displayField);
        }

        //create copy of DF and modify it
        final org.nrg.xdat.display.DisplayField modDF= df.clone();

        modDF.setId(df.getId()+ "=" +value);
        ((SQLQueryField) modDF).setValue(value);

        if(header !=null) {
            modDF.setHeader(header);
        }

        //add to storable ED
        registerDisplayField(modDF,storedDisplay);

        this.getDao().update(storedDisplay);

        //add to in memory copy
        ed.addDisplayField(modDF);

        return modDF;
    }

    @Override
    @Transactional
    public void configureDisplayField(final String elementName, final String displayField, final String header, final Boolean searchable, final String description) throws NoFieldFound, NoSuchElementException {
        //get xml loaded ED
        final org.nrg.xdat.display.ElementDisplay elementDisplay = DisplayManager.GetElementDisplay(elementName);
        if(elementDisplay==null){
            throw new NoSuchElementException(elementName);
        }

        //get xml loaded Field
        final org.nrg.xdat.display.DisplayField displayField1 = elementDisplay.getDisplayField(displayField);
        if(displayField1==null){
            throw new NoFieldFound(displayField);
        }

        //get db loaded ED
        ElementDisplayDB storedDisplay =this.findByElementName(elementName);
        if(storedDisplay==null){
            storedDisplay = new ElementDisplayDB();
            storedDisplay.setElementName(elementName);
            this.getDao().create(storedDisplay);
        }

        //get db loaded field
        DisplayFieldDB storedDF = storedDisplay.getDisplayField(displayField);
        if(storedDF==null){
           storedDF = registerDisplayField(displayField1,storedDisplay);
        }

        //set db version
        if(header !=null) {
            storedDF.setHeader(header);
        }
        if(searchable !=null){
            storedDF.setSearchable(searchable);
        }
        if(description!=null){
            storedDF.setDescription(description);
        }

        this.getDao().update(storedDisplay);

        //set in memory version
        if(header !=null) {
            displayField1.setHeader(header);
        }
        if(searchable !=null){
            displayField1.setSearchable(searchable);
        }
        if(description!=null){
            displayField1.setDescription(description);
        }
    }

    @Override
    @Transactional
    public void addOrUpdateDisplayVersion(String elementName, DisplayVersionDB dv) throws NoFieldFound, NoSuchElementException, DisplayFieldRefCollection.DisplayFieldRefNotFoundException, NoVersionFound {
        //retreive in memory ED
        final org.nrg.xdat.display.ElementDisplay xmlDisplay = DisplayManager.GetElementDisplay(elementName);
        if(xmlDisplay==null){
            throw new NoSuchElementException(elementName);
        }

        //get db loaded ED
        ElementDisplayDB storedDisplay =this.findByElementName(elementName);
        if(storedDisplay==null){
            storedDisplay = new ElementDisplayDB();
            storedDisplay.setElementName(elementName);
            this.getDao().create(storedDisplay);
            storedDisplay =this.findByElementName(elementName);
        }

        DisplayVersionDB storedDV = storedDisplay.getDisplayVersion(dv.getVersionName());
        if(storedDV==null){
            storedDV = new DisplayVersionDB();
            storedDV.copyFrom(dv);
            storedDisplay.addDisplayVersion(storedDV);
        }else{
            storedDV.copyFrom(dv);
        }

        this.getDao().update(storedDisplay);

        //update in memory copy
        org.nrg.xdat.display.DisplayVersion dv2 = renderDisplayVersion(storedDV,xmlDisplay);
        xmlDisplay.getVersions().put(dv.getVersionName(),dv2);
    }

    @Override
    @Transactional
    public void saveElementDisplay(org.nrg.xdat.display.ElementDisplay ed) {
        this.registerElementDisplay(ed);
    }

    public class NoSuchElementException extends Exception{
        public NoSuchElementException(String s){
            super(s,new Exception(s));
        }
    }

    public class FieldAlreadyExists extends Exception{
        public FieldAlreadyExists(String s){
            super(s,new Exception(s));
        }
    }

    public class NoVersionFound extends Exception{
        public NoVersionFound(String s){
            super(s,new Exception(s));
        }
    }

    public class NoFieldFound extends Exception{
        public NoFieldFound(String s){
            super(s,new Exception(s));
        }
    }
}
