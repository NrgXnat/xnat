/*
 * core: org.nrg.xdat.display.transport.entities.TestElementDisplayDB
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display.transport.entities;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestElementDisplayServiceConfig;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestElementDisplayServiceConfig.class)
@Transactional
@Slf4j
public class TestElementDisplayDB {
    private ElementDisplayStorageService _service;

    @Autowired
    public void setService(final ElementDisplayStorageService service) {
        _service = service;
    }

    @Test
    public void testCreateAndRetrieveElementDisplay() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:subjectData");

        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:subjectData");
        assertNotNull(retrieved);
        assertEquals("xnat:subjectData", retrieved.getElementName());
        assertEquals("SUBJECT_ID", retrieved.getValueField());
        assertEquals("label", retrieved.getDisplayField());
        assertEquals("Subject", retrieved.getDisplayLabel());
        assertEquals("Brief desc", retrieved.getBriefDescription());
        assertEquals("Full description", retrieved.getFullDescription());
        assertEquals("#FFFFFF", retrieved.getLightColor());
        assertEquals("#000000", retrieved.getDarkColor());
        assertEquals(Integer.valueOf(1), retrieved.getSortOrder());
    }

    @Test
    public void testElementNameUniqueness() {
        final ElementDisplayDB ed1 = createBasicElementDisplay("xnat:mrSessionData");
        _service.create(ed1);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:mrSessionData");
        assertNotNull(retrieved);

        assertNull(_service.findByElementName("xnat:nonExistent"));
    }

    @Test
    public void testAddDisplayField() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:ctSessionData");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:ctSessionData");
        assertNotNull(retrieved);

        final DisplayFieldDB field = createDisplayField("SESSION_ID", "Session ID");
        retrieved.addDisplayField(field);
        _service.update(retrieved);

        final ElementDisplayDB updated = _service.findByElementName("xnat:ctSessionData");
        assertThat(updated.getDisplayFields()).hasSize(1);

        final DisplayFieldDB retrievedField = updated.getDisplayField("SESSION_ID");
        assertNotNull(retrievedField);
        assertEquals("Session ID", retrievedField.getHeader());
        assertTrue(retrievedField.isSearchable());
        assertTrue(retrievedField.isVisible());
        assertEquals("string", retrievedField.getDataType());
    }

    @Test
    public void testGetDisplayFieldReturnsNullForMissing() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:petSessionData");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:petSessionData");
        assertNull(retrieved.getDisplayField("nonexistent_field"));
    }

    @Test
    public void testMultipleDisplayFields() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:imageSessionData");

        final DisplayFieldDB field1 = createDisplayField("SESSION_ID", "Session");
        final DisplayFieldDB field2 = createDisplayField("SUBJECT_ID", "Subject");
        final DisplayFieldDB field3 = createDisplayField("PROJECT", "Project");
        ed.addDisplayField(field1);
        ed.addDisplayField(field2);
        ed.addDisplayField(field3);

        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:imageSessionData");
        assertThat(retrieved.getDisplayFields()).hasSize(3);
        assertNotNull(retrieved.getDisplayField("SESSION_ID"));
        assertNotNull(retrieved.getDisplayField("SUBJECT_ID"));
        assertNotNull(retrieved.getDisplayField("PROJECT"));
    }

    @Test
    public void testDisplayFieldWithElements() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testElements");

        final DisplayFieldDB field = createDisplayField("SESSION_ID", "Session");

        final DisplayFieldElementDB element = new DisplayFieldElementDB();
        element.setName("element0");
        element.setSchemaElementName("xnat:mrSessionData/ID");
        element.setViewName("xnat_mrSessionData");
        element.setViewColumn("id");
        element.setXdatType("VARCHAR");
        field.addElement(element);

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testElements");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("SESSION_ID");
        assertNotNull(retrievedField);
        assertThat(retrievedField.getElements()).hasSize(1);

        final DisplayFieldElementDB retrievedElement = retrievedField.getElements().get(0);
        assertEquals("element0", retrievedElement.getName());
        assertEquals("xnat:mrSessionData/ID", retrievedElement.getSchemaElementName());
        assertEquals("xnat_mrSessionData", retrievedElement.getViewName());
        assertEquals("id", retrievedElement.getViewColumn());
        assertEquals("VARCHAR", retrievedElement.getXdatType());
    }

    @Test
    public void testDisplayFieldWithContent() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testContent");

        final DisplayFieldDB field = createDisplayField("LABEL", "Label");
        final Map<String, String> content = new HashMap<>();
        content.put("sql", "SELECT label FROM xnat_subjectData");
        content.put("xml", "<value>label</value>");
        field.setContent(content);

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testContent");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("LABEL");
        assertNotNull(retrievedField.getContent());
        assertEquals("SELECT label FROM xnat_subjectData", retrievedField.getContent().get("sql"));
        assertEquals("<value>label</value>", retrievedField.getContent().get("xml"));
    }

    @Test
    public void testDisplayFieldWithSubQuery() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testSubQuery");

        final DisplayFieldDB field = createDisplayField("CUSTOM", "Custom");
        field.setSubQuery("SELECT value FROM params WHERE key=:param");

        final SubQueryMappingColumnDB mappingCol = new SubQueryMappingColumnDB();
        mappingCol.setSchemaField("xnat:mrSessionData/ID");
        mappingCol.setQueryField("param");
        field.addMappingColumn(mappingCol);

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testSubQuery");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("CUSTOM");
        assertEquals("SELECT value FROM params WHERE key=:param", retrievedField.getSubQuery());
        assertThat(retrievedField.getMappingColumns()).hasSize(1);
        assertEquals("xnat:mrSessionData/ID", retrievedField.getMappingColumns().get(0).getSchemaField());
        assertEquals("param", retrievedField.getMappingColumns().get(0).getQueryField());
    }

    @Test
    public void testDisplayFieldWithHtmlLinkProperties() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testHtmlLink");

        final DisplayFieldDB field = createDisplayField("LABEL", "Label");
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

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testHtmlLink");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("LABEL");
        assertEquals("xnat:subjectData", retrievedField.getHtmlLinkSecureLinkTo());
        assertThat(retrievedField.getHtmlLinkProperties()).hasSize(1);

        final DisplayFieldHtmlLinkPropertyDB retrievedProp = retrievedField.getHtmlLinkProperties().get(0);
        assertEquals("HREF", retrievedProp.getName());
        assertEquals("/data/subjects/@WHERE", retrievedProp.getValue());
        assertEquals("xnat:subjectData/ID", retrievedProp.getInsertedValues().get("SUBJECT_ID"));

        assertNotNull(retrievedField.getHtmlLinkSecureProps());
        assertEquals("xnat:subjectData/project", retrievedField.getHtmlLinkSecureProps().get("project"));
    }

    @Test
    public void testDisplayFieldHtmlCellSettings() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testHtmlCell");

        final DisplayFieldDB field = createDisplayField("LABEL", "Label");
        field.setHtmlCellWidth(100);
        field.setHtmlCellHeight(50);
        field.setHtmlCellAlign("center");
        field.setHtmlCellValign("top");
        field.setHtmlCellServerLink("/data/link");

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testHtmlCell");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("LABEL");
        assertEquals(Integer.valueOf(100), retrievedField.getHtmlCellWidth());
        assertEquals(Integer.valueOf(50), retrievedField.getHtmlCellHeight());
        assertEquals("center", retrievedField.getHtmlCellAlign());
        assertEquals("top", retrievedField.getHtmlCellValign());
        assertEquals("/data/link", retrievedField.getHtmlCellServerLink());
    }

    @Test
    public void testDisplayFieldHtmlImageSettings() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testHtmlImage");

        final DisplayFieldDB field = createDisplayField("ICON", "Icon");
        field.setImage(true);
        field.setHtmlImage(true);
        field.setHtmlImageWidth(32);
        field.setHtmlImageHeight(32);

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testHtmlImage");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("ICON");
        assertTrue(retrievedField.isImage());
        assertTrue(retrievedField.isHtmlImage());
        assertEquals(Integer.valueOf(32), retrievedField.getHtmlImageWidth());
        assertEquals(Integer.valueOf(32), retrievedField.getHtmlImageHeight());
    }

    @Test
    public void testAddArc() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testArcs");

        final ElementDisplayArc arc = new ElementDisplayArc();
        arc.setName("test-arc");
        final Map<String, String> commonFields = new HashMap<>();
        commonFields.put("project", "xnat:subjectData/project");
        commonFields.put("subject_id", "xnat:subjectData/ID");
        arc.setCommonFields(commonFields);
        ed.addArc(arc);

        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testArcs");
        assertThat(retrieved.getArcs()).hasSize(1);

        final ElementDisplayArc retrievedArc = retrieved.getArcByName("test-arc");
        assertNotNull(retrievedArc);
        assertEquals("test-arc", retrievedArc.getName());
        assertEquals("xnat:subjectData/project", retrievedArc.getCommonFields().get("project"));
        assertEquals("xnat:subjectData/ID", retrievedArc.getCommonFields().get("subject_id"));
    }

    @Test
    public void testGetArcByNameReturnsNullForMissing() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testArcNull");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testArcNull");
        assertNull(retrieved.getArcByName("nonexistent"));
    }

    @Test
    public void testAddDisplayVersion() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testVersions");

        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");
        version.setDefaultSortOrder("ASC");
        version.setBriefDescription("Standard listing");
        version.setDarkColor("#333333");
        version.setLightColor("#EEEEEE");
        version.setAllowDiffs(true);
        version.setHtmlCellWidth(200);
        version.setHtmlCellHeight(30);
        version.setHtmlCellAlign("left");
        version.setHtmlCellValign("middle");
        version.setHtmlCellServerLink("/app/list");

        ed.addDisplayVersion(version);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testVersions");
        assertThat(retrieved.getDisplayVersions()).hasSize(1);

        final DisplayVersionDB retrievedVersion = retrieved.getDisplayVersion("listing");
        assertNotNull(retrievedVersion);
        assertEquals("listing", retrievedVersion.getVersionName());
        assertEquals("label", retrievedVersion.getDefaultOrderBy());
        assertEquals("ASC", retrievedVersion.getDefaultSortOrder());
        assertEquals("Standard listing", retrievedVersion.getBriefDescription());
        assertTrue(retrievedVersion.isAllowDiffs());
        assertEquals(Integer.valueOf(200), retrievedVersion.getHtmlCellWidth());
    }

    @Test
    public void testGetDisplayVersionReturnsNullForMissing() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testVersionNull");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testVersionNull");
        assertNull(retrieved.getDisplayVersion("nonexistent"));
    }

    @Test
    public void testDisplayVersionWithFieldRefs() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testVersionRefs");

        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");
        version.setDefaultSortOrder("ASC");

        final DisplayFieldRefDB ref1 = new DisplayFieldRefDB();
        ref1.setFieldId("SESSION_ID");
        ref1.setElementName("xnat:mrSessionData");
        ref1.setHeader("Session");
        ref1.setVisible(true);
        ref1.setSortOrder(0);
        ref1.setDisplayVersion(version);

        final DisplayFieldRefDB ref2 = new DisplayFieldRefDB();
        ref2.setFieldId("SUBJECT_ID");
        ref2.setElementName("xnat:subjectData");
        ref2.setHeader("Subject");
        ref2.setVisible(true);
        ref2.setSortOrder(1);
        ref2.setDisplayVersion(version);

        version.setFields(com.google.common.collect.Lists.newArrayList(ref1, ref2));

        ed.addDisplayVersion(version);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testVersionRefs");
        final DisplayVersionDB retrievedVersion = retrieved.getDisplayVersion("listing");
        assertThat(retrievedVersion.getFields()).hasSize(2);

        // Fields should be sorted by sortOrder
        assertEquals("SESSION_ID", retrievedVersion.getFields().get(0).getFieldId());
        assertEquals("SUBJECT_ID", retrievedVersion.getFields().get(1).getFieldId());
    }

    @Test
    public void testDisplayVersionFieldRefByIdLookup() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testFieldRefById");

        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");

        final DisplayFieldRefDB ref = new DisplayFieldRefDB();
        ref.setFieldId("SESSION_ID");
        ref.setElementName("xnat:mrSessionData");
        ref.setHeader("Session");
        ref.setVisible(true);
        ref.setSortOrder(0);
        ref.setDisplayVersion(version);
        version.setFields(com.google.common.collect.Lists.newArrayList(ref));

        ed.addDisplayVersion(version);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testFieldRefById");
        final DisplayVersionDB retrievedVersion = retrieved.getDisplayVersion("listing");

        assertNotNull(retrievedVersion.getFieldById("SESSION_ID", "xnat:mrSessionData", null));
        assertNull(retrievedVersion.getFieldById("NONEXISTENT", "xnat:mrSessionData", null));
        assertNull(retrievedVersion.getFieldById("SESSION_ID", "xnat:otherType", null));
    }

    @Test
    public void testDisplayVersionCopyFrom() {
        final DisplayVersionDB source = new DisplayVersionDB();
        source.setVersionName("v1");
        source.setDefaultOrderBy("date");
        source.setDefaultSortOrder("DESC");
        source.setBriefDescription("Source version");
        source.setDarkColor("#111");
        source.setLightColor("#EEE");
        source.setAllowDiffs(false);
        source.setHtmlCellWidth(150);
        source.setHtmlCellHeight(25);
        source.setHtmlCellAlign("right");
        source.setHtmlCellValign("bottom");
        source.setHtmlCellServerLink("/test");

        final DisplayFieldRefDB ref = new DisplayFieldRefDB();
        ref.setFieldId("F1");
        ref.setElementName("xnat:test");
        ref.setSortOrder(0);
        ref.setDisplayVersion(source);
        source.setFields(com.google.common.collect.Lists.newArrayList(ref));

        final DisplayVersionDB target = new DisplayVersionDB();
        target.copyFrom(source);

        assertEquals("v1", target.getVersionName());
        assertEquals("date", target.getDefaultOrderBy());
        assertEquals("DESC", target.getDefaultSortOrder());
        assertEquals("Source version", target.getBriefDescription());
        assertFalse(target.isAllowDiffs());
        assertEquals(Integer.valueOf(150), target.getHtmlCellWidth());
        assertThat(target.getFields()).hasSize(1);
        assertEquals("F1", target.getFields().get(0).getFieldId());
    }

    @Test
    public void testAddViewLink() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testViewLinks");

        final ElementDisplayViewLinkDB viewLink = new ElementDisplayViewLinkDB();
        viewLink.setAlias("session_view");
        viewLink.setView("xnat_mrSessionData_view");

        final ElementDisplayViewLinkMappingColumnDB col1 = new ElementDisplayViewLinkMappingColumnDB();
        col1.setRootElement("xnat:mrSessionData");
        col1.setFieldElementXMLPath("xnat:mrSessionData/ID");
        col1.setMapsTo("session_id");
        viewLink.addColumn(col1);

        final ElementDisplayViewLinkMappingColumnDB col2 = new ElementDisplayViewLinkMappingColumnDB();
        col2.setRootElement("xnat:mrSessionData");
        col2.setFieldElementXMLPath("xnat:mrSessionData/project");
        col2.setMapsTo("project");
        viewLink.addColumn(col2);

        ed.addViewLink(viewLink);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testViewLinks");
        assertThat(retrieved.getViewLinks()).hasSize(1);

        final ElementDisplayViewLinkDB retrievedVL = retrieved.getViewLinkByAlias("session_view");
        assertNotNull(retrievedVL);
        assertEquals("xnat_mrSessionData_view", retrievedVL.getView());
        assertThat(retrievedVL.getColumns()).hasSize(2);
    }

    @Test
    public void testGetViewLinkByAliasReturnsNullForMissing() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testViewLinkNull");
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testViewLinkNull");
        assertNull(retrieved.getViewLinkByAlias("nonexistent"));
    }

    @Test
    public void testOrphanRemovalOnDisplayFields() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testOrphan");

        final DisplayFieldDB field1 = createDisplayField("FIELD1", "Field 1");
        final DisplayFieldDB field2 = createDisplayField("FIELD2", "Field 2");
        ed.addDisplayField(field1);
        ed.addDisplayField(field2);
        _service.create(ed);

        ElementDisplayDB retrieved = _service.findByElementName("xnat:testOrphan");
        assertThat(retrieved.getDisplayFields()).hasSize(2);

        // Clear and replace with single field
        retrieved.getDisplayFields().clear();
        final DisplayFieldDB field3 = createDisplayField("FIELD3", "Field 3");
        retrieved.addDisplayField(field3);
        _service.update(retrieved);

        retrieved = _service.findByElementName("xnat:testOrphan");
        assertThat(retrieved.getDisplayFields()).hasSize(1);
        assertEquals("FIELD3", retrieved.getDisplayFields().get(0).getFieldId());
    }

    @Test
    public void testCompleteEntityGraph() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testComplete");

        // Add display fields
        final DisplayFieldDB field = createDisplayField("SESSION_ID", "Session");
        final DisplayFieldElementDB element = new DisplayFieldElementDB();
        element.setName("element0");
        element.setSchemaElementName("xnat:mrSessionData/ID");
        element.setViewName("xnat_mrSessionData");
        element.setViewColumn("id");
        field.addElement(element);
        ed.addDisplayField(field);

        // Add display version
        final DisplayVersionDB version = new DisplayVersionDB();
        version.setVersionName("listing");
        version.setDefaultOrderBy("label");
        version.setDefaultSortOrder("ASC");
        final DisplayFieldRefDB ref = new DisplayFieldRefDB();
        ref.setFieldId("SESSION_ID");
        ref.setElementName("");
        ref.setSortOrder(0);
        ref.setDisplayVersion(version);
        version.setFields(com.google.common.collect.Lists.newArrayList(ref));
        ed.addDisplayVersion(version);

        // Add arc
        final ElementDisplayArc arc = new ElementDisplayArc();
        arc.setName("project-arc");
        final Map<String, String> commonFields = new HashMap<>();
        commonFields.put("project", "xnat:subjectData/project");
        arc.setCommonFields(commonFields);
        ed.addArc(arc);

        // Add view link
        final ElementDisplayViewLinkDB viewLink = new ElementDisplayViewLinkDB();
        viewLink.setAlias("session_view");
        viewLink.setView("xnat_mrSessionData_view");
        final ElementDisplayViewLinkMappingColumnDB col = new ElementDisplayViewLinkMappingColumnDB();
        col.setRootElement("xnat:mrSessionData");
        col.setFieldElementXMLPath("xnat:mrSessionData/ID");
        col.setMapsTo("session_id");
        viewLink.addColumn(col);
        ed.addViewLink(viewLink);

        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testComplete");
        assertNotNull(retrieved);
        assertThat(retrieved.getDisplayFields()).hasSize(1);
        assertThat(retrieved.getDisplayVersions()).hasSize(1);
        assertThat(retrieved.getArcs()).hasSize(1);
        assertThat(retrieved.getViewLinks()).hasSize(1);
        assertNotNull(retrieved.getDisplayField("SESSION_ID"));
        assertNotNull(retrieved.getDisplayVersion("listing"));
        assertNotNull(retrieved.getArcByName("project-arc"));
        assertNotNull(retrieved.getViewLinkByAlias("session_view"));
    }

    @Test
    public void testLazyInitCollectionsNotNull() {
        final ElementDisplayDB ed = new ElementDisplayDB();
        assertNotNull(ed.getDisplayFields());
        assertNotNull(ed.getArcs());
        assertNotNull(ed.getDisplayVersions());
        assertNotNull(ed.getViewLinks());
        assertTrue(ed.getDisplayFields().isEmpty());
        assertTrue(ed.getArcs().isEmpty());
        assertTrue(ed.getDisplayVersions().isEmpty());
        assertTrue(ed.getViewLinks().isEmpty());
    }

    @Test
    public void testDisplayFieldDefaults() {
        final DisplayFieldDB field = new DisplayFieldDB();
        assertEquals("", field.getFieldId());
        assertEquals("", field.getHeader());
        assertFalse(field.isImage());
        assertFalse(field.isSearchable());
        assertTrue(field.isVisible());
        assertEquals("", field.getSortBy());
        assertEquals("ASC", field.getSortOrder());
        assertEquals(0, field.getSortIndex());
        assertFalse(field.isHtmlContent());
        assertFalse(field.isHtmlImage());
    }

    @Test
    public void testDisplayVersionDefaults() {
        final DisplayVersionDB version = new DisplayVersionDB();
        assertEquals("", version.getVersionName());
        assertEquals("", version.getDefaultOrderBy());
        assertEquals("", version.getDefaultSortOrder());
        assertEquals("", version.getBriefDescription());
        assertEquals("", version.getDarkColor());
        assertEquals("", version.getLightColor());
        assertTrue(version.isAllowDiffs());
    }

    @Test
    public void testDisplayFieldPossibleValues() {
        final ElementDisplayDB ed = createBasicElementDisplay("xnat:testPossibleValues");

        final DisplayFieldDB field = createDisplayField("STATUS", "Status");
        field.getPossibleValues().add("active");
        field.getPossibleValues().add("inactive");
        field.getPossibleValues().add("archived");

        ed.addDisplayField(field);
        _service.create(ed);

        final ElementDisplayDB retrieved = _service.findByElementName("xnat:testPossibleValues");
        final DisplayFieldDB retrievedField = retrieved.getDisplayField("STATUS");
        assertThat(retrievedField.getPossibleValues()).hasSize(3);
        assertThat(retrievedField.getPossibleValues()).containsExactlyInAnyOrder("active", "inactive", "archived");
    }

    // --- Helper methods ---

    private ElementDisplayDB createBasicElementDisplay(final String elementName) {
        final ElementDisplayDB ed = new ElementDisplayDB();
        ed.setElementName(elementName);
        ed.setValueField("SUBJECT_ID");
        ed.setDisplayField("label");
        ed.setDisplayLabel("Subject");
        ed.setBriefDescription("Brief desc");
        ed.setFullDescription("Full description");
        ed.setLightColor("#FFFFFF");
        ed.setDarkColor("#000000");
        ed.setSortOrder(1);
        return ed;
    }

    private DisplayFieldDB createDisplayField(final String fieldId, final String header) {
        final DisplayFieldDB field = new DisplayFieldDB();
        field.setFieldId(fieldId);
        field.setHeader(header);
        field.setSearchable(true);
        field.setVisible(true);
        field.setDataType("string");
        field.setSortBy(fieldId);
        field.setSortOrder("ASC");
        return field;
    }
}
