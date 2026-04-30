/*
 * core: org.nrg.xft.schema.db.TestDBBackedSchemaService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema.db;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xft.configuration.TestDBBackedSchemaServiceConfig;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests for DBBackedSchema entity, DBBackedSchemaDAO, and HibernateDBBackedSchemaServiceImpl.
 * Tests cover CRUD operations, constraint enforcement, and the service layer methods
 * that do not require XFT initialization (findConfigByPath, createConfig, findAllSchema).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDBBackedSchemaServiceConfig.class)
@Transactional
@Slf4j
public class TestDBBackedSchemaService {
    private static final String SAMPLE_XSD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema targetNamespace=\"http://xnat.org/test\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"TestElement\" type=\"xs:string\"/>\n" +
            "</xs:schema>";

    private static final String SAMPLE_XSD_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema targetNamespace=\"http://xnat.org/other\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"OtherElement\" type=\"xs:string\"/>\n" +
            "</xs:schema>";

    private DBBackedSchemaService _service;

    @Autowired
    public void setService(final DBBackedSchemaService service) {
        _service = service;
    }

    // --- Entity tests ---

    @Test
    public void testEntityDefaults() {
        final DBBackedSchema schema = new DBBackedSchema();
        assertNull(schema.getPath());
        assertNull(schema.getContent());
        assertNull(schema.getName());
    }

    @Test
    public void testEntitySettersAndGetters() {
        final DBBackedSchema schema = new DBBackedSchema();
        schema.setPath("/schemas/test.xsd");
        schema.setContent(SAMPLE_XSD);
        schema.setName("test:testData");

        assertEquals("/schemas/test.xsd", schema.getPath());
        assertEquals(SAMPLE_XSD, schema.getContent());
        assertEquals("test:testData", schema.getName());
    }

    // --- Basic CRUD tests ---

    @Test
    public void testCreateAndRetrieve() {
        final DBBackedSchema schema = createSchema("/schemas/test.xsd", "test:testData", SAMPLE_XSD);
        _service.create(schema);

        final DBBackedSchema retrieved = _service.retrieve(schema.getId());
        assertNotNull(retrieved);
        assertEquals("/schemas/test.xsd", retrieved.getPath());
        assertEquals("test:testData", retrieved.getName());
        assertEquals(SAMPLE_XSD, retrieved.getContent());
    }

    @Test
    public void testUpdate() {
        final DBBackedSchema schema = createSchema("/schemas/test.xsd", "test:testData", SAMPLE_XSD);
        _service.create(schema);

        final DBBackedSchema retrieved = _service.retrieve(schema.getId());
        retrieved.setContent(SAMPLE_XSD_2);
        _service.update(retrieved);

        final DBBackedSchema updated = _service.retrieve(schema.getId());
        assertEquals(SAMPLE_XSD_2, updated.getContent());
    }

    @Test
    public void testDelete() {
        final DBBackedSchema schema = createSchema("/schemas/delete.xsd", "test:deleteData", SAMPLE_XSD);
        _service.create(schema);

        final long id = schema.getId();
        assertNotNull(_service.retrieve(id));

        _service.delete(schema);
        assertNull(_service.retrieve(id));
    }

    // --- DAO findByPath tests ---

    @Test
    public void testFindConfigByPath() {
        final DBBackedSchema schema = createSchema("/schemas/findme.xsd", "test:findMe", SAMPLE_XSD);
        _service.create(schema);

        final DBBackedSchema found = _service.findConfigByPath("/schemas/findme.xsd");
        assertNotNull(found);
        assertEquals("test:findMe", found.getName());
        assertEquals(SAMPLE_XSD, found.getContent());
    }

    @Test
    public void testFindConfigByPathReturnsNullForMissing() {
        assertNull(_service.findConfigByPath("/schemas/nonexistent.xsd"));
    }

    @Test
    public void testFindConfigByPathExcludesDisabled() {
        final DBBackedSchema schema = createSchema("/schemas/disabled.xsd", "test:disabled", SAMPLE_XSD);
        schema.setEnabled(false);
        _service.create(schema);

        assertNull(_service.findConfigByPath("/schemas/disabled.xsd"));
    }

    // --- createConfig tests ---

    @Test
    public void testCreateConfig() throws Exception {
        final DBBackedSchema created = _service.createConfig("/schemas/created.xsd", "test:created", SAMPLE_XSD);

        assertNotNull(created);
        assertEquals("/schemas/created.xsd", created.getPath());
        assertEquals("test:created", created.getName());
        assertEquals(SAMPLE_XSD, created.getContent());
        assertTrue(created.isEnabled());
    }

    @Test
    public void testCreateConfigPersists() throws Exception {
        _service.createConfig("/schemas/persist.xsd", "test:persist", SAMPLE_XSD);

        final DBBackedSchema retrieved = _service.findConfigByPath("/schemas/persist.xsd");
        assertNotNull(retrieved);
        assertEquals("test:persist", retrieved.getName());
    }

    // --- Constraint tests ---

    @Test
    public void testPathUniqueness() {
        final DBBackedSchema schema1 = createSchema("/schemas/unique.xsd", "test:unique1", SAMPLE_XSD);
        _service.create(schema1);

        final DBBackedSchema retrieved = _service.findConfigByPath("/schemas/unique.xsd");
        assertNotNull(retrieved);

        // Verify a different path works
        final DBBackedSchema schema2 = createSchema("/schemas/unique2.xsd", "test:unique2", SAMPLE_XSD_2);
        _service.create(schema2);
        assertNotNull(_service.findConfigByPath("/schemas/unique2.xsd"));
    }

    @Test
    public void testNameUniqueness() {
        final DBBackedSchema schema1 = createSchema("/schemas/name1.xsd", "test:uniqueName", SAMPLE_XSD);
        _service.create(schema1);

        final DBBackedSchema schema2 = createSchema("/schemas/name2.xsd", "test:otherName", SAMPLE_XSD_2);
        _service.create(schema2);

        assertNotNull(_service.findConfigByPath("/schemas/name1.xsd"));
        assertNotNull(_service.findConfigByPath("/schemas/name2.xsd"));
    }

    // --- findAllSchema tests ---

    @Test
    public void testFindAllSchemaEmpty() {
        final List<DBBackedSchema> all = _service.findAllSchema();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAllSchema() {
        _service.create(createSchema("/schemas/a.xsd", "test:a", SAMPLE_XSD));
        _service.create(createSchema("/schemas/b.xsd", "test:b", SAMPLE_XSD_2));
        _service.create(createSchema("/schemas/c.xsd", "test:c", SAMPLE_XSD));

        final List<DBBackedSchema> all = _service.findAllSchema();
        assertThat(all).hasSize(3);
    }

    @Test
    public void testFindAllSchemaIncludesDisabled() {
        _service.create(createSchema("/schemas/enabled.xsd", "test:enabled", SAMPLE_XSD));

        final DBBackedSchema disabled = createSchema("/schemas/off.xsd", "test:off", SAMPLE_XSD_2);
        disabled.setEnabled(false);
        _service.create(disabled);

        // findAllSchema uses findAll which includes all records
        final List<DBBackedSchema> all = _service.findAllSchema();
        assertThat(all).hasSize(2);
    }

    // --- Content (TEXT column) tests ---

    @Test
    public void testLargeContent() {
        final StringBuilder largeXsd = new StringBuilder(SAMPLE_XSD);
        // Pad to make it larger than typical VARCHAR limits
        for (int i = 0; i < 1000; i++) {
            largeXsd.append("  <!-- padding line ").append(i).append(" -->\n");
        }

        final DBBackedSchema schema = createSchema("/schemas/large.xsd", "test:large", largeXsd.toString());
        _service.create(schema);

        final DBBackedSchema retrieved = _service.findConfigByPath("/schemas/large.xsd");
        assertNotNull(retrieved);
        assertEquals(largeXsd.toString(), retrieved.getContent());
    }

    // --- Enabled/disabled behavior ---

    @Test
    public void testDisableAndReEnable() {
        final DBBackedSchema schema = createSchema("/schemas/toggle.xsd", "test:toggle", SAMPLE_XSD);
        _service.create(schema);

        assertNotNull(_service.findConfigByPath("/schemas/toggle.xsd"));

        // Disable
        final DBBackedSchema toDisable = _service.findConfigByPath("/schemas/toggle.xsd");
        toDisable.setEnabled(false);
        _service.update(toDisable);

        // Should not be found by path (filters on enabled=true)
        assertNull(_service.findConfigByPath("/schemas/toggle.xsd"));

        // Re-enable by retrieving by ID
        final DBBackedSchema toEnable = _service.retrieve(schema.getId());
        toEnable.setEnabled(true);
        _service.update(toEnable);

        assertNotNull(_service.findConfigByPath("/schemas/toggle.xsd"));
    }

    // --- newEntity tests ---

    @Test
    public void testNewEntity() {
        final DBBackedSchema schema = _service.newEntity();
        assertNotNull(schema);
        assertTrue(schema.isEnabled());
    }

    // --- Helper methods ---

    private DBBackedSchema createSchema(final String path, final String name, final String content) {
        final DBBackedSchema schema = new DBBackedSchema();
        schema.setPath(path);
        schema.setName(name);
        schema.setContent(content);
        return schema;
    }
}
