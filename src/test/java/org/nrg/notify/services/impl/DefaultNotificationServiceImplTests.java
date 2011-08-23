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
    public void testCreateCategory() {
        assertNotNull(_service);
        Category category = _service.newCategory();
        assertNotNull(category);
        assertEquals(CategoryScope.Default, category.getScope());
        assertNull(category.getEvent());
    }

    private static final Log _log = LogFactory.getLog(DefaultNotificationServiceImplTests.class);

    @Autowired
    private NotificationService _service;
}
