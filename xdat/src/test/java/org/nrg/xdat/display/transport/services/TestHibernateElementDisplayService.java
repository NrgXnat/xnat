/*
 * core: org.nrg.xdat.display.transport.services.TestHibernateElementDisplayService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display.transport.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestElementDisplayServiceConfig;
import org.nrg.xdat.display.transport.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for HibernateElementDisplayService, focusing on the renderElementDisplay logic
 * that converts DB entities to in-memory ElementDisplay objects.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestElementDisplayServiceConfig.class)
@Transactional
@Slf4j
public class TestHibernateElementDisplayService {
    private ElementDisplayStorageService _service;

    @Autowired
    public void setService(final ElementDisplayStorageService service) {
        _service = service;
    }

    @Test
    public void testRenderBasicElementDisplay() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:mrSessionData");
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:mrSessionData");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        assertEquals("xnat:mrSessionData", rendered.getElementName());
        assertEquals("SESSION_ID", rendered.getValueField());
        assertEquals("label", rendered.getDisplayField());
        assertEquals("MR Session", rendered.getDisplayLabel());
        assertEquals("Brief", rendered.getBriefDescription());
        assertEquals("Full description", rendered.getFullDescription());
    }

    @Test
    public void testRenderNullReturnsNull() {
        // findByElementName for nonexistent should return null
        assertNull(_service.findByElementName("xnat:nonExistent"));
    }

    @Test
    public void testRenderDisplayFields() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderFields");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("SESSION_ID");
        field.setHeader("Session");
        field.setVisible(true);
        field.setSearchable(true);
        field.setDataType("string");
        field.setDescription("The session ID");
        field.setSortBy("SESSION_ID");
        field.setSortOrder("ASC");
        field.setHtmlCellWidth(150);
        field.setHtmlCellHeight(30);
        field.setHtmlCellAlign("left");
        field.setHtmlCellValign("middle");
        field.setHtmlCellServerLink("/data/sessions");

        final DisplayFieldElementDB element = new DisplayFieldElementDB();
        element.setName("element0");
        element.setSchemaElementName("xnat:mrSessionData/ID");
        element.setViewName("xnat_mrSessionData");
        element.setViewColumn("id");
        element.setXdatType("VARCHAR");
        field.addElement(element);

        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderFields");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("SESSION_ID");
        assertNotNull(renderedField);
        assertEquals("SESSION_ID", renderedField.getId());
        assertEquals("Session", renderedField.getHeader());
        assertTrue(renderedField.isVisible());
        assertTrue(renderedField.isSearchable());
        assertEquals("string", renderedField.getDataType());
        assertEquals("The session ID", renderedField.getDescription());

        // Check element
        assertThat(renderedField.getElements()).hasSize(1);
        final org.nrg.xdat.display.DisplayFieldElement renderedElement = renderedField.getElements().get(0);
        assertEquals("element0", renderedElement.getName());
        assertEquals("xnat:mrSessionData/ID", renderedElement.getSchemaElementName());
        assertEquals("xnat_mrSessionData", renderedElement.getViewName());
        assertEquals("id", renderedElement.getViewColumn());
        assertEquals("VARCHAR", renderedElement.getXdatType());

        // Check HTML cell
        assertEquals("left", renderedField.getHtmlCell().getAlign());
        assertEquals("middle", renderedField.getHtmlCell().getValign());
        assertEquals(Integer.valueOf(150), renderedField.getHtmlCell().getWidth());
        assertEquals(Integer.valueOf(30), renderedField.getHtmlCell().getHeight());
        assertEquals("/data/sessions", renderedField.getHtmlCell().getServerLink());
    }

    @Test
    public void testRenderDisplayFieldWithContent() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderContent");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("LABEL");
        field.setHeader("Label");
        final Map<String, String> content = new HashMap<>();
        content.put("sql", "SELECT label FROM xnat_subjectData");
        field.setContent(content);

        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderContent");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("LABEL");
        assertNotNull(renderedField);
        assertEquals("SELECT label FROM xnat_subjectData", renderedField.getContent().get("sql"));
    }

    @Test
    public void testRenderDisplayFieldWithHtmlLink() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderLink");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("SUBJECT_LABEL");
        field.setHeader("Subject");
        field.setHtmlLinkSecureLinkTo("xnat:subjectData");

        final DisplayFieldHtmlLinkPropertyDB linkProp = new DisplayFieldHtmlLinkPropertyDB();
        linkProp.setName("HREF");
        linkProp.setValue("/data/subjects/@WHERE");
        final Map<String, String> insertedValues = new HashMap<>();
        insertedValues.put("SUBJECT_ID", "xnat:subjectData/ID");
        linkProp.setInsertedValues(insertedValues);
        field.addHtmlLinkProperties(linkProp);

        final Map<String, String> secureProps = new HashMap<>();
        secureProps.put("project", "xnat:subjectData/project");
        field.setHtmlLinkSecureProps(secureProps);

        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderLink");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("SUBJECT_LABEL");
        assertNotNull(renderedField);
        assertNotNull(renderedField.getHtmlLink());
        assertEquals("xnat:subjectData", renderedField.getHtmlLink().getSecureLinkTo());
        assertThat(renderedField.getHtmlLink().getProperties()).hasSize(1);

        final org.nrg.xdat.display.HTMLLinkProperty prop = renderedField.getHtmlLink().getProperties().get(0);
        assertEquals("HREF", prop.getName());
        assertEquals("/data/subjects/@WHERE", prop.getValue());
        assertEquals("xnat:subjectData/ID", prop.getInsertedValues().get("SUBJECT_ID"));

        assertEquals("xnat:subjectData/project", renderedField.getHtmlLink().getSecureProps().get("project"));
    }

    @Test
    public void testRenderSqlQueryField() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderSqlQuery");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("CUSTOM_FIELD");
        field.setHeader("Custom");
        field.setSubQuery("SELECT value FROM params WHERE key=:param");

        final SubQueryMappingColumnDB mappingCol = new SubQueryMappingColumnDB();
        mappingCol.setSchemaField("xnat:mrSessionData/ID");
        mappingCol.setQueryField("param");
        field.addMappingColumn(mappingCol);

        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderSqlQuery");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("CUSTOM_FIELD");
        assertNotNull(renderedField);
        assertTrue(renderedField instanceof org.nrg.xdat.display.SQLQueryField);

        final org.nrg.xdat.display.SQLQueryField sqf = (org.nrg.xdat.display.SQLQueryField) renderedField;
        assertEquals("SELECT value FROM params WHERE key=:param", sqf.getSubQuery());
        assertThat(sqf.getMappingColumns()).hasSize(1);
        assertEquals("xnat:mrSessionData/ID", sqf.getMappingColumns().get(0).getSchemaField());
        assertEquals("param", sqf.getMappingColumns().get(0).getQueryField());
    }

    @Test
    public void testRenderDisplayVersions() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderVersions");

        // Need a display field first for the ref to be valid
        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("SESSION_ID");
        field.setHeader("Session");
        storedED.addDisplayField(field);

        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");
        version.setDefaultSortOrder("ASC");
        version.setBriefDescription("Standard listing");
        version.setDarkColor("#333");
        version.setLightColor("#EEE");
        version.setAllowDiffs(true);
        version.setHtmlCellWidth(200);
        version.setHtmlCellHeight(30);
        version.setHtmlCellAlign("left");
        version.setHtmlCellValign("middle");
        version.setHtmlCellServerLink("/app/list");

        final DisplayFieldRefDB ref = new DisplayFieldRefDB();
        ref.setFieldId("SESSION_ID");
        ref.setElementName("");
        ref.setHeader("Session");
        ref.setVisible(true);
        ref.setSortOrder(0);
        ref.setDisplayVersion(version);
        version.setFields(Lists.newArrayList(ref));

        storedED.addDisplayVersion(version);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderVersions");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        assertThat(rendered.getVersions()).isNotEmpty();

        final org.nrg.xdat.display.DisplayVersion renderedDV = rendered.getVersions().get("listing");
        assertNotNull(renderedDV);
        assertEquals("listing", renderedDV.getVersionName());
        assertEquals("label", renderedDV.getDefaultOrderBy());
        assertEquals("ASC", renderedDV.getDefaultSortOrder());
        assertEquals("Standard listing", renderedDV.getBriefDescription());
        assertTrue(renderedDV.isAllowDiffs());

        // Check HTML cell
        assertEquals("left", renderedDV.getHeaderCell().getAlign());
        assertEquals("middle", renderedDV.getHeaderCell().getValign());
        assertEquals(Integer.valueOf(200), renderedDV.getHeaderCell().getWidth());
    }

    @Test
    public void testRenderArcs() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderArcs");

        final ElementDisplayArc arc = new ElementDisplayArc();
        arc.setName("project-arc");
        final Map<String, String> commonFields = new HashMap<>();
        commonFields.put("project", "xnat:subjectData/project");
        commonFields.put("subject_id", "xnat:subjectData/ID");
        arc.setCommonFields(commonFields);
        storedED.addArc(arc);

        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderArcs");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        assertNotNull(rendered.getArcs());
        final org.nrg.xdat.display.Arc renderedArc = (org.nrg.xdat.display.Arc) rendered.getArcs().get("project-arc");
        assertNotNull(renderedArc);
        assertEquals("project-arc", renderedArc.getName());
        assertEquals("xnat:subjectData/project", renderedArc.getCommonFields().get("project"));
        assertEquals("xnat:subjectData/ID", renderedArc.getCommonFields().get("subject_id"));
    }

    @Test
    public void testRenderViewLinks() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderViewLinks");

        final ElementDisplayViewLinkDB viewLink = new ElementDisplayViewLinkDB();
        viewLink.setAlias("session_view");
        viewLink.setView("xnat_mrSessionData_view");

        final ElementDisplayViewLinkMappingColumnDB col = new ElementDisplayViewLinkMappingColumnDB();
        col.setRootElement("xnat:mrSessionData");
        col.setFieldElementXMLPath("xnat:mrSessionData/ID");
        col.setMapsTo("session_id");
        viewLink.addColumn(col);

        storedED.addViewLink(viewLink);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderViewLinks");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        assertNotNull(rendered.getViewLinks());
        final org.nrg.xdat.display.ViewLink renderedVL = (org.nrg.xdat.display.ViewLink) rendered.getViewLinks().get("session_view");
        assertNotNull(renderedVL);
        assertEquals("session_view", renderedVL.getAlias());
        assertNotNull(renderedVL.getMapping());
        assertEquals("xnat_mrSessionData_view", renderedVL.getMapping().getTableName());
        assertThat(renderedVL.getMapping().getColumns()).hasSize(1);

        final org.nrg.xdat.display.MappingColumn renderedCol = (org.nrg.xdat.display.MappingColumn) renderedVL.getMapping().getColumns().get(0);
        assertEquals("xnat:mrSessionData", renderedCol.getRootElement());
        assertEquals("xnat:mrSessionData/ID", renderedCol.getFieldElementXMLPath());
        assertEquals("session_id", renderedCol.getMapsTo());
    }

    @Test
    public void testRenderCompleteEntityGraph() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderComplete");

        // Display field with element
        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("SESSION_ID");
        field.setHeader("Session");
        field.setSearchable(true);
        field.setVisible(true);
        final DisplayFieldElementDB element = new DisplayFieldElementDB();
        element.setName("element0");
        element.setSchemaElementName("xnat:mrSessionData/ID");
        element.setViewName("xnat_mrSessionData");
        element.setViewColumn("id");
        field.addElement(element);
        storedED.addDisplayField(field);

        // Display version with ref
        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");
        version.setDefaultSortOrder("ASC");
        final DisplayFieldRefDB ref = new DisplayFieldRefDB();
        ref.setFieldId("SESSION_ID");
        ref.setElementName("");
        ref.setSortOrder(0);
        ref.setDisplayVersion(version);
        version.setFields(Lists.newArrayList(ref));
        storedED.addDisplayVersion(version);

        // Arc
        final ElementDisplayArc arc = new ElementDisplayArc();
        arc.setName("project-arc");
        arc.setCommonFields(new HashMap<>());
        storedED.addArc(arc);

        // View link
        final ElementDisplayViewLinkDB viewLink = new ElementDisplayViewLinkDB();
        viewLink.setAlias("test_view");
        viewLink.setView("test_view_table");
        final ElementDisplayViewLinkMappingColumnDB col = new ElementDisplayViewLinkMappingColumnDB();
        col.setRootElement("xnat:mrSessionData");
        col.setFieldElementXMLPath("xnat:mrSessionData/ID");
        col.setMapsTo("id");
        viewLink.addColumn(col);
        storedED.addViewLink(viewLink);

        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderComplete");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        assertNotNull(rendered);
        assertEquals("xnat:testRenderComplete", rendered.getElementName());
        assertNotNull(rendered.getDisplayField("SESSION_ID"));
        assertNotNull(rendered.getVersions().get("listing"));
        assertNotNull(rendered.getArcs().get("project-arc"));
        assertNotNull(rendered.getViewLinks().get("test_view"));
    }

    @Test
    public void testFindByElementNameReturnsNullForDisabled() {
        final ElementDisplayDB ed = createFullElementDisplay("xnat:testDisabled");
        ed.setEnabled(false);
        _service.create(ed);

        // findByElementName filters on enabled=true
        assertNull(_service.findByElementName("xnat:testDisabled"));
    }

    @Test
    public void testRenderFieldWithNoHtmlLinkProduceNoLink() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testNoLink");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("PLAIN_FIELD");
        field.setHeader("Plain");
        // No HTML link properties set
        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testNoLink");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("PLAIN_FIELD");
        assertNotNull(renderedField);
        assertNull(renderedField.getHtmlLink());
    }

    @Test
    public void testRenderFieldWithImageSettings() {
        final ElementDisplayDB storedED = createFullElementDisplay("xnat:testRenderImage");

        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId("ICON");
        field.setHeader("Icon");
        field.setImage(true);
        field.setHtmlImage(true);
        field.setHtmlImageWidth(48);
        field.setHtmlImageHeight(48);
        storedED.addDisplayField(field);
        _service.create(storedED);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testRenderImage");
        final org.nrg.xdat.display.ElementDisplay rendered = _service.renderElementDisplay(retrieved);

        final org.nrg.xdat.display.DisplayField renderedField = rendered.getDisplayField("ICON");
        assertNotNull(renderedField);
        assertTrue(renderedField.isImage());
        assertEquals(Integer.valueOf(48), renderedField.getHtmlImage().getWidth());
        assertEquals(Integer.valueOf(48), renderedField.getHtmlImage().getHeight());
    }

    @Test
    public void testMultipleElementDisplays() {
        final ElementDisplayDB ed1 = createFullElementDisplay("xnat:mrSessionData");
        final ElementDisplayDB ed2 = createFullElementDisplay("xnat:ctSessionData");
        final ElementDisplayDB ed3 = createFullElementDisplay("xnat:petSessionData");

        _service.create(ed1);
        _service.create(ed2);
        _service.create(ed3);

        assertNotNull(_service.findByElementName("xnat:mrSessionData"));
        assertNotNull(_service.findByElementName("xnat:ctSessionData"));
        assertNotNull(_service.findByElementName("xnat:petSessionData"));
    }

    @Test
    public void testUpdateExistingElementDisplay() {
        final ElementDisplayDB ed = createFullElementDisplay("xnat:testUpdate");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testUpdate");
        retrieved.setDisplayLabel("Updated Label");
        retrieved.setBriefDescription("Updated description");
        _service.update(retrieved);

        final ElementDisplayDB updated = _service.findByElementName("xnat:testUpdate");
        assertEquals("Updated Label", updated.getDisplayLabel());
        assertEquals("Updated description", updated.getBriefDescription());
    }

    /**
     * Reproduces the production bug where adding a new DisplayVersion via addOrUpdateDisplayVersion
     * resulted in DisplayFieldRefDB rows with null foreign keys to DisplayVersionDB.
     *
     * The bug was: addOrUpdateDisplayVersion created a new storedDV, called copyFrom(dv) on it,
     * but then called addDisplayVersion(dv) instead of addDisplayVersion(storedDV). The input dv
     * (deserialized from JSON) had DisplayFieldRefDB children with null displayVersion back-refs
     * because Jackson ignores @JsonIgnore fields. The copyFrom path correctly set back-refs via
     * addField(), but that work was discarded because the wrong object was persisted.
     *
     * This test simulates the same flow: creates a DisplayVersionDB with field refs that have
     * null displayVersion (as Jackson would produce), then uses copyFrom + addDisplayVersion
     * (the fixed path) and verifies foreign keys are populated after persistence.
     */
    @Test
    public void testNewDisplayVersionFieldRefForeignKeys() {
        // Create and persist an ElementDisplayDB
        final ElementDisplayDB ed = createFullElementDisplay("xnat:testFKBug");
        _service.create(ed);

        final ElementDisplayDB storedDisplay = _service.findByElementName("xnat:testFKBug");
        assertNotNull(storedDisplay);

        // Simulate a JSON-deserialized DisplayVersionDB (displayVersion back-ref is null on refs)
        final DisplayVersionDB inputDV = new DisplayVersionDB();
        inputDV.setVersionName("listing");
        inputDV.setDefaultOrderBy("label");
        inputDV.setDefaultSortOrder("ASC");

        final DisplayFieldRefDB ref1 = new DisplayFieldRefDB();
        ref1.setFieldId("SESSION_ID");
        ref1.setElementName("xnat:ctSessionData");
        ref1.setHeader("Session");
        ref1.setVisible(true);
        ref1.setSortOrder(0);
        // Deliberately NOT setting ref1.setDisplayVersion() - simulates Jackson deserialization

        final DisplayFieldRefDB ref2 = new DisplayFieldRefDB();
        ref2.setFieldId("SUBJECT_ID");
        ref2.setElementName("xnat:subjectData");
        ref2.setHeader("Subject");
        ref2.setVisible(true);
        ref2.setSortOrder(1);
        // Deliberately NOT setting ref2.setDisplayVersion()

        inputDV.setFields(Lists.newArrayList(ref1, ref2));

        // Replicate the fixed addOrUpdateDisplayVersion logic for new version
        final DisplayVersionDB storedDV = new DisplayVersionDB();
        storedDV.copyFrom(inputDV);
        storedDisplay.addDisplayVersion(storedDV);

        _service.update(storedDisplay);

        // Re-fetch and verify foreign keys are populated
        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testFKBug");
        final DisplayVersionDB retrievedVersion = retrieved.getDisplayVersion("listing");
        assertNotNull(retrievedVersion);
        assertThat(retrievedVersion.getFields()).hasSize(2);

        for (final DisplayFieldRefDB fieldRef : retrievedVersion.getFields()) {
            // This is the key assertion - the foreign key must be populated
            assertNotNull("DisplayFieldRefDB.displayVersion foreign key must not be null", fieldRef.getDisplayVersion());
            assertEquals(retrievedVersion.getId(), fieldRef.getDisplayVersion().getId());
        }

        // Verify field data survived the round-trip
        assertEquals("SESSION_ID", retrievedVersion.getFields().get(0).getFieldId());
        assertEquals("SUBJECT_ID", retrievedVersion.getFields().get(1).getFieldId());
    }

    /**
     * Tests the update path of the same flow - when a DisplayVersion already exists,
     * copyFrom should correctly add new refs and remove absent ones while preserving FK.
     */
    @Test
    public void testUpdateDisplayVersionFieldRefForeignKeys() {
        // Create ED with an existing version
        final ElementDisplayDB ed = createFullElementDisplay("xnat:testFKUpdate");

        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");

        final DisplayFieldRefDB origRef = new DisplayFieldRefDB();
        origRef.setFieldId("SESSION_ID");
        origRef.setElementName("xnat:ctSessionData");
        origRef.setSortOrder(0);
        origRef.setDisplayVersion(version);
        version.setFields(Lists.newArrayList(origRef));

        ed.addDisplayVersion(version);
        _service.create(ed);

        // Simulate an update with a modified version from JSON
        final ElementDisplayDB storedDisplay = _service.findByElementName("xnat:testFKUpdate");
        final DisplayVersionDB storedDV = storedDisplay.getDisplayVersion("listing");
        assertNotNull(storedDV);

        // Input version replaces SESSION_ID with two new fields
        final DisplayVersionDB inputDV = new DisplayVersionDB();
        inputDV.setVersionName("listing");
        inputDV.setDefaultOrderBy("date");
        inputDV.setDefaultSortOrder("DESC");

        final DisplayFieldRefDB newRef1 = new DisplayFieldRefDB();
        newRef1.setFieldId("PROJECT");
        newRef1.setElementName("xnat:ctSessionData");
        newRef1.setHeader("Project");
        newRef1.setSortOrder(0);

        final DisplayFieldRefDB newRef2 = new DisplayFieldRefDB();
        newRef2.setFieldId("DATE");
        newRef2.setElementName("xnat:ctSessionData");
        newRef2.setHeader("Date");
        newRef2.setSortOrder(1);

        inputDV.setFields(Lists.newArrayList(newRef1, newRef2));

        storedDV.copyFrom(inputDV);
        _service.update(storedDisplay);

        // Re-fetch and verify
        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testFKUpdate");
        final DisplayVersionDB retrievedVersion = retrieved.getDisplayVersion("listing");
        assertNotNull(retrievedVersion);
        assertEquals("date", retrievedVersion.getDefaultOrderBy());
        assertEquals("DESC", retrievedVersion.getDefaultSortOrder());
        assertThat(retrievedVersion.getFields()).hasSize(2);

        for (final DisplayFieldRefDB fieldRef : retrievedVersion.getFields()) {
            assertNotNull("DisplayFieldRefDB.displayVersion foreign key must not be null", fieldRef.getDisplayVersion());
            assertEquals(retrievedVersion.getId(), fieldRef.getDisplayVersion().getId());
        }

        assertEquals("PROJECT", retrievedVersion.getFields().get(0).getFieldId());
        assertEquals("DATE", retrievedVersion.getFields().get(1).getFieldId());
    }

    // --- Helper methods ---

    private ElementDisplayDB createFullElementDisplay(final String elementName) {
        final ElementDisplayDB ed = new ElementDisplayDB();
        ed.setElementName(elementName);
        ed.setValueField("SESSION_ID");
        ed.setDisplayField("label");
        ed.setDisplayLabel("MR Session");
        ed.setBriefDescription("Brief");
        ed.setFullDescription("Full description");
        ed.setLightColor("#FFFFFF");
        ed.setDarkColor("#000000");
        ed.setSortOrder(1);
        return ed;
    }
}
