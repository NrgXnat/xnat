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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.orm.utils.TestDBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests handling of auditable classes, checking for persistence of "deleted" classes, handling
 * of unique constraints, etc.
 *
 * @author Rick Herrick
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuditableEntityTestsConfiguration.class)
public class AuditableEntityTests {
    public static final String FIELD_1 = "Field 1";
    public static final int    FIELD_2 = 321;
    public static final Date   FIELD_3 = new Timestamp(new Date().getTime());

    private final AuditableEntityService _service;
    private final TestDBUtils            _dbUtils;
    private final DataSource             _dataSource;

    @Autowired
    public AuditableEntityTests(final AuditableEntityService service, final TestDBUtils dbUtils, final DataSource dataSource) {
        _service    = service;
        _dbUtils    = dbUtils;
        _dataSource = dataSource;
    }

    @BeforeEach
    public void clearEntities() throws SQLException {
        _dbUtils.cleanDb("XHBM_AUDITABLE_ENTITY");
    }

    @Test
    public void testBasicOperations() {
        final AuditableEntity created = _service.newEntity();
        created.setField1(FIELD_1);
        created.setField2(FIELD_2);
        created.setField3(FIELD_3);
        _service.create(created);

        final List<AuditableEntity> items1 = _service.getAll();
        assertThat(items1).isNotNull()
                          .isNotEmpty()
                          .hasSize(1);

        final AuditableEntity retrieved = items1.get(0);
        assertThat(retrieved).isNotNull()
                             .hasFieldOrPropertyWithValue("field1", FIELD_1)
                             .hasFieldOrPropertyWithValue("field2", FIELD_2)
                             .hasFieldOrPropertyWithValue("field3", FIELD_3);

        _service.delete(retrieved);
        final List<AuditableEntity> items2 = _service.getAll();
        assertThat(items2).isNotNull().isEmpty();
    }

    @Test
    public void testUniqueConstraints() {
        assertThrows(ConstraintViolationException.class, () -> {
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
        });
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Test
    public void testAuditableUniqueConstraints() {
        final JdbcTemplate template = new JdbcTemplate(_dataSource);
        AuditableEntity    created1 = _service.create(FIELD_1, FIELD_2, FIELD_3);
        final long         id1      = created1.getId();
        displayResults(template.queryForMap("SELECT ID, FIELD1, FIELD2, FIELD3, ENABLED, DISABLED FROM XHBM_AUDITABLE_ENTITY"));
        _service.delete(created1);
        displayResults(created1);
        displayResults(template.queryForMap("SELECT ID, FIELD1, FIELD2, FIELD3, ENABLED, DISABLED FROM XHBM_AUDITABLE_ENTITY"));
        AuditableEntity created2 = _service.create(FIELD_1, FIELD_2, FIELD_3);
        final long      id2      = created2.getId();
        assertThat(id1).isNotEqualTo(id2);
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
}
