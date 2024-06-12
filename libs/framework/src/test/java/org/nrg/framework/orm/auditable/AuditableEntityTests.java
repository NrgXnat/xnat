/*
 * framework: org.nrg.framework.orm.auditable.AuditableEntityTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.auditable;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.orm.utils.TestDBUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests handling of auditable classes, checking for persistence of "deleted" classes, handling
 * of unique constraints, etc.
 *
 * @author Rick Herrick
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AuditableEntityTestsConfiguration.class)
public class AuditableEntityTests {

    public static final String FIELD_1 = "Field 1";
    public static final int FIELD_2 = 321;
    public static final Date FIELD_3 = new Date();

    @Before
    public void clearEntities() throws SQLException {
        _dbUtils.cleanDb("XHBM_AUDITABLE_ENTITY");
    }

    @Test
    public void testBasicOperations() {
        AuditableEntity created = _service.newEntity();
        created.setField1(FIELD_1);
        created.setField2(FIELD_2);
        created.setField3(FIELD_3);
        _service.create(created);

        List<AuditableEntity> items = _service.getAll();
        assertNotNull(items);
        assertEquals(1, items.size());
        AuditableEntity retrieved = items.get(0);
        assertEquals(FIELD_1, retrieved.getField1());
        assertEquals(FIELD_2, retrieved.getField2());
        assertEquals(FIELD_3, retrieved.getField3());

        _service.delete(retrieved);
        items = _service.getAll();
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUniqueConstraints() {
        AuditableEntity created1 = _service.newEntity();
        created1.setField1(FIELD_1);
        created1.setField2(FIELD_2);
        created1.setField3(FIELD_3);
        _service.create(created1);
        AuditableEntity created2 = _service.newEntity();
        created2.setField1(FIELD_1);
        created2.setField2(FIELD_2);
        created2.setField3(FIELD_3);
        _service.create(created2);
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Test
    public void testAuditableUniqueConstraints() {
        final JdbcTemplate template = new JdbcTemplate(_dataSource);
        AuditableEntity created1 = _service.create(FIELD_1, FIELD_2, FIELD_3);
        final long id1 = created1.getId();
        displayResults(template.queryForMap("SELECT ID, FIELD1, FIELD2, FIELD3, ENABLED, DISABLED FROM XHBM_AUDITABLE_ENTITY"));
        _service.delete(created1);
        displayResults(created1);
        displayResults(template.queryForMap("SELECT ID, FIELD1, FIELD2, FIELD3, ENABLED, DISABLED FROM XHBM_AUDITABLE_ENTITY"));
        AuditableEntity created2 = _service.create(FIELD_1, FIELD_2, FIELD_3);
        final long id2 = created2.getId();
        assertNotEquals(id1, id2);
    }

    private void displayResults(final Map<String, Object> results) {
        for (final String key : results.keySet()) {
            System.out.println(key + ": " + results.get(key).toString());
        }
    }

    private void displayResults(final AuditableEntity created1) {
        final Map<String, Object> results = new LinkedHashMap<>();
        results.put("ID", created1.getId());
        results.put("FIELD1", created1.getField1());
        results.put("FIELD2", created1.getField2());
        results.put("FIELD3", created1.getField3());
        results.put("ENABLED", created1.isEnabled());
        results.put("DISABLED", created1.getDisabled());
        displayResults(results);
    }

    @Inject
    private TestDBUtils _dbUtils;
    @Inject
    private AuditableEntityService _service;
    @Inject
    private DataSource _dataSource;
}
