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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.mail.api.MailMessage;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.Category;
import org.nrg.notify.entities.Channel;
import org.nrg.notify.entities.Definition;
import org.nrg.notify.entities.Subscriber;
import org.nrg.notify.entities.Subscription;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
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

        _service.getCategoryService().refresh(category2);
        List<Definition> definitions = _service.getDefinitionService().getDefinitionsForCategory(category2);
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
    @Ignore("Ignored because requires working SMTP server. Set SMTP address in test.properties to test.")
    public void testSubscribersAndSubscriptions() throws DuplicateDefinitionException, DuplicateSubscriberException, IOException {
        Subscriber subscriber1 = _service.getSubscriberService().createSubscriber("Subscriber 1", "subscriber1@rickherrick.com");
        Subscriber subscriber2 = _service.getSubscriberService().createSubscriber("Subscriber 2", "subscriber2@rickherrick.com");
        Subscriber subscriber3 = _service.getSubscriberService().createSubscriber("Subscriber 3", "subscriber3@rickherrick.com");
        Subscriber subscriber4 = _service.getSubscriberService().createSubscriber("Subscriber 4", "subscriber4@rickherrick.com");
        
        Definition definition1 = _service.createDefinition(CategoryScope.Project, "subevent1", 11L);
        Definition definition2 = _service.createDefinition(CategoryScope.Project, "subevent1", 12L);
        Definition definition3 = _service.createDefinition(CategoryScope.Project, "subevent2", 21L);
        Definition definition4 = _service.createDefinition(CategoryScope.Project, "subevent2", 22L);

        Channel channel1 = _service.getChannelService().createChannel("htmlMail", "text/html");
        Channel channel2 = _service.getChannelService().createChannel("textMail", "text/plain");
        
        Subscription subscription111 = _service.subscribe(subscriber1, SubscriberType.User, definition1, channel1);
        Subscription subscription121 = _service.subscribe(subscriber1, SubscriberType.User, definition2, channel1);
        Subscription subscription132 = _service.subscribe(subscriber1, SubscriberType.User, definition3, channel2);
        Subscription subscription211 = _service.subscribe(subscriber2, SubscriberType.User, definition1, channel1);
        Subscription subscription232 = _service.subscribe(subscriber2, SubscriberType.User, definition3, channel2);
        Subscription subscription242 = _service.subscribe(subscriber2, SubscriberType.User, definition4, channel2);
        Subscription subscription312 = _service.subscribe(subscriber3, SubscriberType.User, definition1, channel2);
        Subscription subscription322 = _service.subscribe(subscriber3, SubscriberType.User, definition2, channel2);
        Subscription subscription341 = _service.subscribe(subscriber3, SubscriberType.User, definition4, channel1);
        Subscription subscription412 = _service.subscribe(subscriber4, SubscriberType.User, definition1, channel2);
        Subscription subscription441 = _service.subscribe(subscriber4, SubscriberType.User, definition4, channel1);

        validateSubscription(subscription111, "Subscriber 1", "subscriber1@rickherrick.com", 11L, CategoryScope.Project, "subevent1", "htmlMail", "text/html");
        validateSubscription(subscription121, "Subscriber 1", "subscriber1@rickherrick.com", 12L, CategoryScope.Project, "subevent1", "htmlMail", "text/html");
        validateSubscription(subscription132, "Subscriber 1", "subscriber1@rickherrick.com", 21L, CategoryScope.Project, "subevent2", "textMail", "text/plain");
        validateSubscription(subscription211, "Subscriber 2", "subscriber2@rickherrick.com", 11L, CategoryScope.Project, "subevent1", "htmlMail", "text/html");
        validateSubscription(subscription232, "Subscriber 2", "subscriber2@rickherrick.com", 21L, CategoryScope.Project, "subevent2", "textMail", "text/plain");
        validateSubscription(subscription242, "Subscriber 2", "subscriber2@rickherrick.com", 22L, CategoryScope.Project, "subevent2", "textMail", "text/plain");
        validateSubscription(subscription312, "Subscriber 3", "subscriber3@rickherrick.com", 11L, CategoryScope.Project, "subevent1", "textMail", "text/plain");
        validateSubscription(subscription322, "Subscriber 3", "subscriber3@rickherrick.com", 12L, CategoryScope.Project, "subevent1", "textMail", "text/plain");
        validateSubscription(subscription341, "Subscriber 3", "subscriber3@rickherrick.com", 22L, CategoryScope.Project, "subevent2", "htmlMail", "text/html");
        validateSubscription(subscription412, "Subscriber 4", "subscriber4@rickherrick.com", 11L, CategoryScope.Project, "subevent1", "textMail", "text/plain");
        validateSubscription(subscription441, "Subscriber 4", "subscriber4@rickherrick.com", 22L, CategoryScope.Project, "subevent2", "htmlMail", "text/html");

        List<Subscription> subscriberSubscriptions1 = subscriber1.getSubscriptions();
        List<Subscription> subscriberSubscriptions2 = subscriber2.getSubscriptions();
        List<Subscription> subscriberSubscriptions3 = subscriber3.getSubscriptions();
        List<Subscription> subscriberSubscriptions4 = subscriber4.getSubscriptions();

        assertNotNull(subscriberSubscriptions1);
        assertEquals(3, subscriberSubscriptions1.size());
        assertNotNull(subscriberSubscriptions2);
        assertEquals(3, subscriberSubscriptions2.size());
        assertNotNull(subscriberSubscriptions3);
        assertEquals(3, subscriberSubscriptions3.size());
        assertNotNull(subscriberSubscriptions4);
        assertEquals(2, subscriberSubscriptions4.size());
        
        List<Subscription> definitionSubscriptions1 = definition1.getSubscriptions();
        List<Subscription> definitionSubscriptions2 = definition2.getSubscriptions();
        List<Subscription> definitionSubscriptions3 = definition3.getSubscriptions();
        List<Subscription> definitionSubscriptions4 = definition4.getSubscriptions();

        assertNotNull(definitionSubscriptions1);
        assertEquals(4, definitionSubscriptions1.size());
        assertNotNull(definitionSubscriptions2);
        assertEquals(2, definitionSubscriptions2.size());
        assertNotNull(definitionSubscriptions3);
        assertEquals(2, definitionSubscriptions3.size());
        assertNotNull(definitionSubscriptions4);
        assertEquals(3, definitionSubscriptions4.size());
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MailMessage.PROP_SUBJECT, "Test notification");
        parameters.put(MailMessage.PROP_HTML, "<html><body>This is a test notification, which includes an <b>HTML</b> message payload.</body></html>");
        parameters.put(MailMessage.PROP_TEXT, "This is a test notification, which includes a text message payload.");

        _service.createNotification(definition1, parameters);
    }

    /**
     * @param subscription
     * @param subscriberName
     * @param subscriberEmail
     * @param entity
     * @param scope
     * @param event
     * @param channelName
     * @param mimeType
     */
    private void validateSubscription(Subscription subscription, String subscriberName, String subscriberEmail, long entity, CategoryScope scope, String event, String channelName, String mimeType) {
        assertNotNull(subscription);
        Subscriber subscriber = subscription.getSubscriber();
        assertNotNull(subscriber);
        assertEquals(subscriberName, subscriber.getName());
        assertEquals(subscriberEmail, subscriber.getEmails());
        Definition definition = subscription.getDefinition();
        assertNotNull(definition);
        assertEquals(entity, definition.getEntity());
        Category category = definition.getCategory();
        assertNotNull(category);
        assertEquals(scope, category.getScope());
        assertEquals(event, category.getEvent());
        List<Channel> channels = subscription.getChannels();
        assertNotNull(channels);
        assertEquals(1, channels.size());
        Channel channel = channels.get(0);
        assertNotNull(channel);
        assertEquals(channelName, channel.getName());
        assertEquals(mimeType, channel.getFormat());
    }

    private static final Log _log = LogFactory.getLog(NotificationServiceTests.class);

    @Inject
    private NotificationService _service;
}
