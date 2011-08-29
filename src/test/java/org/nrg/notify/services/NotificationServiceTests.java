/**
 * NotificationServiceTests
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * Many of these tests are annotated with {@link Transactional} to support session persistence through the execution
 * of the test. In the Spring Web context, this is usually handled with the OpenSessionInView intercepter configuration.
 * Basically the issue is that a Hibernate session is instantiated at the boundary of the Transactional annotation. Any
 * objects that have lazily-fetched data members that aren't accessed within the transaction context will become
 * unresolvable later once the session has expired. Maintaining the Transactional layer at the test method level allows
 * these data members to be properly resolved without having to resort to eager fetches or collection initialization
 * (e.g. calling {@link Hibernate#initialize(Object)}).
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class NotificationServiceTests {
    public NotificationServiceTests() {
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
    
    /**
     * Tests creating, retrieving, updating, and deleting categories.
     * @throws InterruptedException This is required for the {@link Thread#sleep(long)} call.
     */
    @Test
    public void testManageCategory() throws InterruptedException {
        // TODO: I added this to test H2 issues with auto-creation of tables.
        // Connection connection = _dataSource.getConnection();
        // Statement statement = connection.createStatement();
        // statement.execute("CREATE TABLE category(id int PRIMARY KEY, event varchar(255), scope int)");

        // Create a category.
        assertNotNull(_service);
        Category category1 = _service.getCategoryService().newEntity();
        assertNotNull(category1);
        assertEquals(CategoryScope.Default, category1.getScope());
        assertNull(category1.getEvent());

        category1.setEvent("event1");
        category1.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category1);

        long entityId1 = category1.getId();
        Category retrievedCat1 = _service.getCategoryService().retrieve(entityId1);
        assertEquals(category1, retrievedCat1);
        Date created = retrievedCat1.getCreated();
        Date timestamp = retrievedCat1.getTimestamp();
        assertEquals(category1.getCreated(), created);
        assertEquals(created, timestamp);
        
        category1.setEvent("event1updated");
        Thread.sleep(500);
        _service.getCategoryService().update(category1);
        retrievedCat1 = _service.getCategoryService().retrieve(entityId1);
        assertEquals(category1, retrievedCat1);
        assertEquals("event1updated", retrievedCat1.getEvent());
        assertEquals(retrievedCat1.getCreated(), created);
        assertFalse(retrievedCat1.getTimestamp() == created);
        assertFalse(retrievedCat1.getTimestamp() == timestamp);

        _service.getCategoryService().delete(entityId1);
        retrievedCat1 = _service.getCategoryService().retrieve(entityId1);
        assertNull(retrievedCat1);
        
        Category category2 = _service.getCategoryService().newEntity();
        category2.setEvent("event2");
        category2.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category2);

        long entityId2 = category2.getId();
        Category retrievedCat2 = _service.getCategoryService().retrieve(entityId2);
        assertEquals(category2, retrievedCat2);

        List<Category> allCategories = _service.getCategoryService().getAllWithDisabled();
        List<Category> categories = _service.getCategoryService().getAll();
        assertNotNull(allCategories);
        assertNotNull(categories);
        assertEquals(2, allCategories.size());
        assertEquals(1, categories.size());

        _service.getCategoryService().delete(category2);
        retrievedCat2 = _service.getCategoryService().retrieve(entityId2);
        assertNull(retrievedCat2);
        
        allCategories = _service.getCategoryService().getAllWithDisabled();
        categories = _service.getCategoryService().getAll();
        assertNotNull(allCategories);
        assertNotNull(categories);
        assertEquals(2, allCategories.size());
        assertEquals(0, categories.size());
    }
    
    @Test
    @ExpectedException(DataIntegrityViolationException.class)
    public void testCategoryConstraints() {
        Category category1 = _service.getCategoryService().newEntity();
        category1.setScope(CategoryScope.Site);
        category1.setEvent("duplicate");
        _service.getCategoryService().create(category1);
        Category category2 = _service.getCategoryService().newEntity();
        category2.setScope(CategoryScope.Site);
        category2.setEvent("duplicate");
        _service.getCategoryService().create(category2);
    }

    /**
     * Creates multiple categories and definitions.
     */
    @Test
    @Transactional
    public void testCategoryAndDefinitions() {
        // Create a category.
        Category category1 = _service.getCategoryService().newEntity();
        assertNotNull(category1);
        assertEquals(CategoryScope.Default, category1.getScope());
        assertNull(category1.getEvent());
        category1.setEvent("event1");
        category1.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category1);
        Category retrievedCat1 = _service.getCategoryService().retrieve(category1.getId());
        assertEquals(category1, retrievedCat1);

        // Create a couple of definitions based on the category.
        Definition definition11 = _service.getDefinitionService().newEntity();
        definition11.setCategory(category1);
        definition11.setEntity(11L);
        _service.getDefinitionService().create(definition11);
        Definition definition12 = _service.getDefinitionService().newEntity();
        definition12.setCategory(category1);
        definition12.setEntity(12L);
        _service.getDefinitionService().create(definition12);
        Definition retrievedDef1 = _service.getDefinitionService().retrieve(definition11.getId());
        Definition retrievedDef2 = _service.getDefinitionService().retrieve(definition12.getId());
        assertNotNull(retrievedDef1);
        assertNotNull(retrievedDef2);
        assertEquals(definition11, retrievedDef1);
        assertEquals(definition12, retrievedDef2);
        
        Category category2 = _service.getCategoryService().newEntity();
        category2.setEvent("event2");
        category2.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category2);
        Definition definition21 = _service.getDefinitionService().newEntity();
        definition21.setCategory(category2);
        definition21.setEntity(21L);
        _service.getDefinitionService().create(definition21);
        Definition definition22 = _service.getDefinitionService().newEntity();
        definition22.setCategory(category2);
        definition22.setEntity(22L);
        _service.getDefinitionService().create(definition22);

        List<Definition> definitions = _service.getDefinitionsForCategory(category2);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
    }
    
    @Test
    @ExpectedException(DuplicateDefinitionException.class)
    @Transactional
    public void testDuplicateDefinitions() throws DuplicateDefinitionException {
        _service.createDefinition(CategoryScope.Project, "dupevent1", 11L);
        _service.createDefinition(CategoryScope.Project, "dupevent1", 11L);
    }

    @Test
    @Transactional
    public void testSubscribersAndSubscriptions() throws DuplicateDefinitionException, DuplicateSubscriberException {
        Definition definition1 = _service.createDefinition(CategoryScope.Project, "subevent1", 11L);
        Definition definition2 = _service.createDefinition(CategoryScope.Project, "subevent1", 12L);
        Definition definition3 = _service.createDefinition(CategoryScope.Project, "subevent2", 21L);
        Definition definition4 = _service.createDefinition(CategoryScope.Project, "subevent2", 22L);

        Subscriber subscriber1 = _service.getSubscriberService().createSubscriber("Subscriber 1", "subscriber1@rickherrick.com");
        Subscriber subscriber2 = _service.getSubscriberService().createSubscriber("Subscriber 2", "subscriber2@rickherrick.com");
        Subscriber subscriber3 = _service.getSubscriberService().createSubscriber("Subscriber 3", "subscriber3@rickherrick.com");
        Subscriber subscriber4 = _service.getSubscriberService().createSubscriber("Subscriber 4", "subscriber4@rickherrick.com");
        
        Channel channel1 = _service.getChannelService().createChannel("htmlMail", "text/html");
        Channel channel2 = _service.getChannelService().createChannel("textMail", "text/plain");
        
        _service.subscribe(subscriber1, SubscriberType.User, definition1, channel1);
        _service.subscribe(subscriber1, SubscriberType.User, definition2, channel1);
        _service.subscribe(subscriber1, SubscriberType.User, definition3, channel2);
        _service.subscribe(subscriber2, SubscriberType.User, definition1, channel1);
        _service.subscribe(subscriber2, SubscriberType.User, definition3, channel2);
        _service.subscribe(subscriber2, SubscriberType.User, definition4, channel2);
        _service.subscribe(subscriber3, SubscriberType.User, definition1, channel2);
        _service.subscribe(subscriber3, SubscriberType.User, definition2, channel2);
        _service.subscribe(subscriber3, SubscriberType.User, definition4, channel1);
        _service.subscribe(subscriber4, SubscriberType.User, definition1, channel2);
        _service.subscribe(subscriber4, SubscriberType.User, definition4, channel1);
        
    }

    private static final Log _log = LogFactory.getLog(NotificationServiceTests.class);

    // TODO: I added these to enable the connection.createStatement() test above to debug H2 issues.
    // @Autowired
    // @Qualifier("dataSource")
    // private DataSource _dataSource;
    
    @Autowired
    private NotificationService _service;
}
