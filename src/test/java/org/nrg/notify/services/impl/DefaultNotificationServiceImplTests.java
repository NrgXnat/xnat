/**
 * DefaultNotificationServiceImplTests
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.notify.api.Category;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.Definition;
import org.nrg.notify.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Rick Herrick <rick.herrick@wustl.edu>

 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DefaultNotificationServiceImplTests {
    public DefaultNotificationServiceImplTests() {
        _log.info("Creating test class");
    }
    
    @Before
    public void initialize() {
        _log.info("Initializing test class");
    }

    @After
    public void teardown() {
        _log.info("Tearing down test class");
    }
    
    @Test
    public void testCreateCategory() throws SQLException {
        // TODO: I added this to test H2 issues with auto-creation of tables.
        // Connection connection = _dataSource.getConnection();
        // Statement statement = connection.createStatement();
        // statement.execute("CREATE TABLE category(id int PRIMARY KEY, event varchar(255), scope int)");

        // Create a category.
        assertNotNull(_service);
        Category category = _service.getCategoryService().newEntity();
        assertNotNull(category);
        assertEquals(CategoryScope.Default, category.getScope());
        assertNull(category.getEvent());
        category.setEvent("event1");
        category.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category);
        Category retrieved = _service.getCategoryService().retrieve(category.getId());
        assertEquals(category, retrieved);

        // Create a couple of definitions based on the category.
        Definition definition1 = _service.getDefinitionService().newEntity();
        definition1.setCategory(category);
        definition1.setEntity(1111L);
        _service.getDefinitionService().create(definition1);
        Definition definition2 = _service.getDefinitionService().newEntity();
        definition2.setCategory(category);
        definition2.setEntity(2222L);
        _service.getDefinitionService().create(definition2);
        Definition retrievedDef1 = _service.getDefinitionService().retrieve(definition1.getId());
        Definition retrievedDef2 = _service.getDefinitionService().retrieve(definition2.getId());
        assertNotNull(retrievedDef1);
        assertNotNull(retrievedDef2);
        assertEquals(definition1, retrievedDef1);
        assertEquals(definition2, retrievedDef2);
    }

    private static final Log _log = LogFactory.getLog(DefaultNotificationServiceImplTests.class);

    // TODO: I added these to enable the connection.createStatement() test above to debug H2 issues.
    // @Autowired
    // @Qualifier("dataSource")
    // private DataSource _dataSource;
    
    @Autowired
    private NotificationService _service;
}
