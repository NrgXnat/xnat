/*
 * notify: org.nrg.notify.services.NotificationServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.services;

import org.hibernate.Hibernate;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.mail.api.MailMessage;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.configuration.NotificationServiceTestConfiguration;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Many of these tests are annotated with {@link Transactional} to support session persistence through the execution
 * of the test. In the Spring Web context, this is usually handled with the OpenSessionInView interceptor configuration.
 * Basically the issue is that a Hibernate session is instantiated at the boundary of the Transactional annotation. Any
 * objects that have lazily-fetched data members that aren't accessed within the transaction context will become
 * unresolvable later once the session has expired. Maintaining the Transactional layer at the test method level allows
 * these data members to be properly resolved without having to resort to eager fetches or collection initialization
 * (e.g. calling {@link Hibernate#initialize(Object)}).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NotificationServiceTestConfiguration.class)
@ComponentScan("org.nrg.mail.services")
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
     *
     * @throws InterruptedException This is required for the {@link Thread#sleep(long)} call.
     */
    @Test
    public void testManageCategory() throws InterruptedException {

        // Verify the service.
        assertNotNull(_service);

        int allWithDisabledOffset = _service.getCategoryService().getAllWithDisabled().size();
        int allOffset             = _service.getCategoryService().getAll().size();

        if (allWithDisabledOffset > 0 || allOffset > 0) {
            _log.warn("There is an issue with query caching where categories are properly deleted when execution is relatively slow, but persist when test execution is fast, e.g. when running from Maven Surefire. These offsets compensate for that, but generally speaking are super lame. The appropriate thing to do would be to have the cache cleared and/or die between executions, but using a non-singleton cache also breaks unit test execution.");
            _log.warn("Offsets are set to " + allOffset + " existing categories and " + allWithDisabledOffset + " categories including disabled categories.");
        }

        // Create a category.
        Category category1 = _service.getCategoryService().newEntity();
        assertNotNull(category1);
        assertEquals(CategoryScope.Default, category1.getScope());
        assertNull(category1.getEvent());

        category1.setEvent("event1");
        category1.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category1);

        final long entityId1 = category1.getId();

        final Category cat1Retrieve1 = _service.getCategoryService().retrieve(entityId1);
        assertEquals(category1, cat1Retrieve1);
        final Date created   = cat1Retrieve1.getCreated();
        final Date timestamp = cat1Retrieve1.getTimestamp();
        assertEquals(category1.getCreated(), created);
        assertEquals(category1.getTimestamp(), timestamp);

        category1.setEvent("event1updated");
        Thread.sleep(500);
        _service.getCategoryService().update(category1);

        final Category cat1Retrieve2 = _service.getCategoryService().retrieve(entityId1);
        assertEquals(category1, cat1Retrieve2);
        assertEquals("event1updated", cat1Retrieve2.getEvent());
        assertEquals(cat1Retrieve2.getCreated(), created);
        assertNotSame(cat1Retrieve2.getTimestamp(), created);
        assertNotSame(cat1Retrieve2.getTimestamp(), timestamp);

        _service.getCategoryService().delete(entityId1);
        final Category cat1Retrieve3 = _service.getCategoryService().retrieve(entityId1);
        assertNull(cat1Retrieve3);

        final Category category2 = _service.getCategoryService().newEntity();
        category2.setEvent("event2");
        category2.setScope(CategoryScope.Project);
        _service.getCategoryService().create(category2);

        final long     entityId2     = category2.getId();
        final Category cat2Retrieve1 = _service.getCategoryService().retrieve(entityId2);
        assertEquals(category2, cat2Retrieve1);

        final List<Category> allCategories1 = _service.getCategoryService().getAllWithDisabled();
        final List<Category> categories1    = _service.getCategoryService().getAll();
        assertNotNull(allCategories1);
        assertNotNull(categories1);

        assertEquals(2 + allWithDisabledOffset, allCategories1.size());
        assertEquals(1 + allOffset, categories1.size());

        _service.getCategoryService().delete(category2);

        final Category cat2Retrieve2 = _service.getCategoryService().retrieve(entityId2);
        assertNull(cat2Retrieve2);

        final List<Category> allCategories2 = _service.getCategoryService().getAllWithDisabled();
        final List<Category> categories2    = _service.getCategoryService().getAll();
        assertNotNull(allCategories2);
        assertNotNull(categories2);
        assertEquals(2 + allWithDisabledOffset, allCategories2.size());
        assertEquals(allOffset, categories2.size());
    }

    @Test(expected = ConstraintViolationException.class)
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
        category1.setEvent("eventA");
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
        category2.setEvent("eventB");
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

    @Test(expected = DuplicateDefinitionException.class)
    @Transactional
    public void testDuplicateDefinitions() throws NrgServiceException {
        _service.createDefinition(CategoryScope.Project, "dupevent1", 11L);
        _service.createDefinition(CategoryScope.Project, "dupevent1", 11L);
    }

    @Test
    @Ignore("Ignored because requires working SMTP server. Set SMTP address in test.properties to test.")
    public void testSubscribersAndSubscriptions() throws NrgServiceException, IOException {
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

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MailMessage.PROP_SUBJECT, "Test notification");
        parameters.put(MailMessage.PROP_HTML, "<html><body>This is a test notification, which includes an <b>HTML</b> message payload.</body></html>");
        parameters.put(MailMessage.PROP_TEXT, "This is a test notification, which includes a text message payload.");

        _service.createNotification(definition1, parameters);
    }

    /**
     * @param subscription    The subscription to validate.
     * @param subscriberName  The subscriber login name.
     * @param subscriberEmail The subscriber email.
     * @param entityId        The ID of the entity with which the subscription is associated.
     * @param scope           The {@link org.nrg.notify.api.CategoryScope scope} of the subscription category.
     * @param event           The event associated with the subscription.
     * @param channelName     The channel name.
     * @param mimeType        The requested MIME type for the subscription.
     */
    @SuppressWarnings("SameParameterValue")
    private void validateSubscription(final Subscription subscription, final String subscriberName, final String subscriberEmail, final long entityId, final CategoryScope scope, final String event, final String channelName, final String mimeType) {
        assertNotNull(subscription);
        Subscriber subscriber = subscription.getSubscriber();
        assertNotNull(subscriber);
        assertEquals(subscriberName, subscriber.getName());
        assertEquals(subscriberEmail, subscriber.getEmails());
        Definition definition = subscription.getDefinition();
        assertNotNull(definition);
        assertEquals(entityId, definition.getEntity());
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

    private static final Logger _log = LoggerFactory.getLogger(NotificationServiceTests.class);

    @Inject
    private NotificationService _service;
}
