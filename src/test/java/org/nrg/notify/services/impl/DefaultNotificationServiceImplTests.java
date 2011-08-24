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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.notify.api.Category;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author rherrick
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
        // Connection connection = _dataSource.getConnection();
        // Statement statement = connection.createStatement();
        // statement.execute("CREATE TABLE category(id int PRIMARY KEY, event varchar(255), scope int)");

        assertNotNull(_service);
        Category category = _service.getCategoryService().newCategory();
        assertNotNull(category);
        assertEquals(CategoryScope.Default, category.getScope());
        assertNull(category.getEvent());
        category.setEvent("event1");
        category.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category);
        Category retrieved = _service.getCategoryService().retrieveCategory(category.getId());
        assertEquals(category, retrieved);
    }

    private static final Log _log = LogFactory.getLog(DefaultNotificationServiceImplTests.class);

    @Autowired
    @Qualifier("h2DataSource")
    private DataSource _dataSource;
    
    @Autowired
    private NotificationService _service;
}
