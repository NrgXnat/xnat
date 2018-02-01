package org.nrg.xnat.eventservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.actions.EventServiceLoggingAction;
import org.nrg.xnat.eventservice.actions.SingleActionProvider;
import org.nrg.xnat.eventservice.actions.TestAction;
import org.nrg.xnat.eventservice.config.EventServiceTestConfig;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.events.SampleEvent;
import org.nrg.xnat.eventservice.events.TestCombinedEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.listeners.TestListener;
import org.nrg.xnat.eventservice.model.*;
import org.nrg.xnat.eventservice.model.xnat.Scan;
import org.nrg.xnat.eventservice.model.xnat.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selector;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static reactor.bus.selector.Selectors.matchAll;
import static reactor.bus.selector.Selectors.type;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class EventServiceIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(EventServiceIntegrationTest.class);

    private static final String EVENT_RESOURCE_PATTERN ="classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";

    private UserI mockUser;

    private final String FAKE_USER = "mockUser";
    private final Integer FAKE_USER_ID = 1234;

    @Autowired private EventBus eventBus;
    @Autowired private TestListener testListener;
    @Autowired private EventServiceActionProvider testAction;
    @Autowired @Lazy private EventService eventService;
    @Autowired private EventService mockEventService;
    @Autowired private EventSubscriptionEntityService eventSubscriptionEntityService;
    @Autowired private ContextService contextService;
    @Autowired private EventServiceComponentManager componentManager;
    @Autowired private EventServiceComponentManager mockComponentManager;
    @Autowired private ActionManager actionManager;
    @Autowired private ActionManager mockActionManager;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EventServiceLoggingAction mockEventServiceLoggingAction;
    @Autowired private UserManagementServiceI mockUserManagementServiceI;
    @Autowired private SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService;


    private SubscriptionCreator project1CreatedSubscription;
    private SubscriptionCreator project2CreatedSubscription;
    private Scan mrScan1 = new Scan();
    private Scan mrScan2 = new Scan();
    private Scan ctScan1 = new Scan();

    private Session mrSession = new Session();
    private Session ctSession = new Session();


    //private Project project1 = new Project("PROJECTID-1", mockUser);
    //private Project project2 = new Project("PROJECTID-2", mockUser);

    //private Subject subject1 = new Subject("SUBJECTID-1", mockUser);
    //private Subject subject2 = new Subject("SUBJECTID-2", mockUser);



    @Before
    public void setUp() throws Exception {

        EventFilter eventServiceFilter = EventFilter.builder().build();

        project1CreatedSubscription = SubscriptionCreator.builder()
                                                         .name("TestSubscription")
                                                         .active(true)
                                                         .projectId("PROJECTID-1")
                                                         .eventId("org.nrg.xnat.eventservice.events.ProjectCreatedEvent")
                                                         .customListenerId("org.nrg.xnat.eventservice.listeners.TestListener")
                                                         .actionKey("org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction")
                                                         .eventFilter(eventServiceFilter)
                                                         .actAsEventUser(false)
                                                         .build();

        project2CreatedSubscription = SubscriptionCreator.builder()
                                                        .name("TestSubscription2")
                                                        .active(true)
                                                        .projectId("PROJECTID-2")
                                                        .eventId("org.nrg.xnat.eventservice.events.ProjectCreatedEvent")
                                                        .customListenerId("org.nrg.xnat.eventservice.listeners.TestListener")
                                                        .actionKey("org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction")
                                                        .eventFilter(eventServiceFilter)
                                                        .actAsEventUser(false)
                                                        .build();

        mrScan1.setId("1111");
        mrScan1.setLabel("TestLabel");
        mrScan1.setXsiType("xnat:Scan");
        mrScan1.setNote("Test note.");
        mrScan1.setModality("MR");
        mrScan1.setIntegerId(1111);
        mrScan1.setProjectId("PROJECTID-1");
        mrScan1.setSeriesDescription("This is the description of a series which is this one.");

        mrScan2.setId("2222");
        mrScan2.setLabel("TestLabel");
        mrScan2.setXsiType("xnat:Scan");
        mrScan2.setNote("Test note.");
        mrScan2.setModality("MR");
        mrScan2.setIntegerId(2222);
        mrScan2.setProjectId("PROJECTID-1");
        mrScan2.setSeriesDescription("This is the description of a series which is this one.");

        ctScan1.setId("3333");
        ctScan1.setLabel("TestLabel");
        ctScan1.setXsiType("xnat:Scan");
        ctScan1.setNote("Test note.");
        ctScan1.setModality("CT");
        ctScan1.setIntegerId(3333);
        ctScan1.setProjectId("PROJECTID-1");
        ctScan1.setSeriesDescription("This is the description of a series which is this one.");


        // Mock the userI
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn(FAKE_USER);
        when(mockUser.getID()).thenReturn(FAKE_USER_ID);

        // Mock the user management service
        when(mockUserManagementServiceI.getUser(FAKE_USER)).thenReturn(mockUser);
        when(mockUserManagementServiceI.getUser(FAKE_USER_ID)).thenReturn(mockUser);

        when(mockComponentManager.getActionProviders()).thenReturn(new ArrayList<>(Arrays.asList(new MockSingleActionProvider())));

        when(mockComponentManager.getInstalledEvents()).thenReturn(new ArrayList<>(Arrays.asList(new SampleEvent())));

        when(mockComponentManager.getInstalledListeners()).thenReturn(new ArrayList<>(Arrays.asList(new TestListener())));

        // Mock action
        when(mockEventServiceLoggingAction.getName()).thenReturn("org.nrg.xnat.eventservice.actions.EventServiceLoggingAction");
        when(mockEventServiceLoggingAction.getDisplayName()).thenReturn("MockEventServiceLoggingAction");
        when(mockEventServiceLoggingAction.getDescription()).thenReturn("MockEventServiceLoggingAction");
        when(mockEventServiceLoggingAction.getActions(Matchers.any(UserI.class))).thenReturn(null);


    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void checkContext() throws Exception {
        assertThat(contextService.getBean("eventService"), not(nullValue()));
    }

    @Test
    public void checkDatabaseConnection() throws Exception {
        List<SubscriptionEntity> entities = eventSubscriptionEntityService.getAll();
        assertThat(entities, is(not(nullValue())));
    }

    @Test
    public void filterSerializedModelObjects() throws Exception {
        String mrFilter = "$[?(@.modality == \"MR\")]";
        String ctFilter = "$[?(@.modality == \"CT\")]";
        String mrCtProj1Filter = "$[?(@.project-id == \"PROJECTID-1\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";
        String mrCtProj2Filter = "$[?(@.project-id == \"PROJECTID-2\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";
        String proj2Filter = "$[?(@.project-id == \"PROJECTID-2\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";

        assertThat(objectMapper.canSerialize(Scan.class), is(true));
        String jsonMrScan1 = objectMapper.writeValueAsString(mrScan1);
        String jsonMrScan2 = objectMapper.writeValueAsString(mrScan2);
        String jsonCtScan1 = objectMapper.writeValueAsString(ctScan1);


        List<String> match = JsonPath.parse(jsonMrScan1).read(mrFilter);
        assertThat("JsonPath result should not be null", match, notNullValue());
        assertThat("JsonPath match result should not be empty", match, is(not(empty())));

        List<String> mismatch = JsonPath.parse(jsonCtScan1).read(mrFilter);
        assertThat("JsonPath result should not be null", mismatch, notNullValue());
        assertThat("JsonPath mismatch result should be empty" + mismatch, mismatch, is(empty()));

        match = JsonPath.parse(jsonCtScan1).read(mrCtProj1Filter);
        assertThat("JsonPath result should not be null", match, notNullValue());
        assertThat("JsonPath match result should not be empty", match, is(not(empty())));

        mismatch = JsonPath.parse(jsonMrScan1).read(mrCtProj2Filter);
        assertThat("JsonPath result should not be null", mismatch, notNullValue());
        assertThat("JsonPath mismatch result should be empty: " + mismatch, mismatch, is(empty()));


    }

    @Test
    public void listenForEverything() throws Exception {

        // Detect all EventServiceEvent type events
        Selector selector = matchAll();
        eventBus.on(selector, testListener);
    }

    @Test
    public void getInstalledEvents() throws Exception {
        List<SimpleEvent> events = eventService.getEvents();
        Integer eventPropertyFileCount = BasicXnatResourceLocator.getResources(EVENT_RESOURCE_PATTERN).size();

        assert(events != null && events.size() == eventPropertyFileCount);
    }

    @Test
    public void getInstalledActionProviders() throws Exception {
        assertThat("componentManager.getActionProviders() should not be null.", componentManager.getActionProviders(), notNullValue());
        assertThat("componentManager.getActionProviders() should not be empty.", componentManager.getActionProviders().size(), not(equalTo(0)));
    }

    @Test
    public void getInstalledActions() throws Exception {
        List<Action> actions = eventService.getAllActions();
        assert(actions != null && actions.size() > 0);
    }

    @Test
    public void getInstalledListeners() throws Exception {
        List<EventServiceListener> listeners = componentManager.getInstalledListeners();
        assertThat(listeners, notNullValue());
        assertThat(listeners, not(is(empty())));
    }

    @Test
    @DirtiesContext
    public void createSubscription() throws Exception {
        List<SimpleEvent> events = eventService.getEvents();
        assertThat("eventService.getEvents() should not return a null list", events, notNullValue());
        assertThat("eventService.getEvents() should not return an empty list", events, is(not(empty())));

        List<Action> actions = eventService.getAllActions();
        assertThat("eventService.getAllActions() should not return a null list", actions, notNullValue());
        assertThat("eventService.getAllActions() should not return an empty list", actions, is(not(empty())));

        List<EventServiceListener> listeners = componentManager.getInstalledListeners();
        assertThat("componentManager.getInstalledListeners() should not return a null list", listeners, notNullValue());
        assertThat("componentManager.getInstalledListeners() should not return an empty list", listeners, is(not(empty())));

        String eventId = events.get(0).id();
        String actionId = actions.get(0).id();
        String actionKey = actions.get(0).actionKey();
        String listenerType = listeners.get(0).getClass().getCanonicalName();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder().name("Test Subscription")
                                                                     .active(true)
                                                                     .eventId(eventId)
                                                                     .customListenerId(listenerType)
                                                                     .actionKey(actionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Created subscription should not be null", subscription, notNullValue());

        eventService.validateSubscription(subscription);

        Subscription savedSubscription = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() should not return null", savedSubscription, notNullValue());
        assertThat("subscription id should not be null", savedSubscription.id(), notNullValue());
        assertThat("subscription id should not be zero", savedSubscription.id(), not(0));
        assertThat("subscription registration key should not be null", savedSubscription.listenerRegistrationKey(), notNullValue());
        assertThat("subscription registration key should not be empty", savedSubscription.listenerRegistrationKey(), not(""));

        subscriptionCreator = SubscriptionCreator.builder().name("Test 2 Subscription")
                                                                     .active(true)
                                                                     .eventId(eventId)
                                                                     .customListenerId(listenerType)
                                                                     .actionKey(actionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();

        subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());

        eventService.validateSubscription(subscription);

        Subscription secondSavedSubscription = eventService.createSubscription(subscription);

        assertThat("Subscriptions should have unique listener IDs", savedSubscription.listenerRegistrationKey(), not(secondSavedSubscription.listenerRegistrationKey()));
        assertThat("Subscriptions should have unique IDs", savedSubscription.id(), not(secondSavedSubscription.id()));
    }

    @Test
    public void listSubscriptions() throws Exception {
        createSubscription();
        assertThat("No subscriptions found.", eventService.getSubscriptions(), is(not(empty())));
        assertThat("Two subscriptions expected.", eventService.getSubscriptions().size(), is(2));
        for (Subscription subscription:eventService.getSubscriptions()) {
            assertThat("subscription id is null for " + subscription.name(), subscription.id(), notNullValue());
            assertThat("subscription id is zero (0) for " + subscription.name(), subscription.id(), not(0));
        }
    }

    @Test
    @DirtiesContext
    public void saveSubscriptionEntity() throws Exception {
        Subscription subscription = eventSubscriptionEntityService.save(Subscription.create(project1CreatedSubscription, mockUser.getLogin()));
        assertThat("EventSubscriptionEntityService.save should not create a null entity.", subscription, not(nullValue()));
        assertThat("Saved subscription entity should have been assigned a database ID.", subscription.id(), not(nullValue()));
        assertThat("Pojo name mis-match", subscription.name(), containsString(project1CreatedSubscription.name()));
        assertThat("Pojo actionService mis-match", subscription.actionKey(), containsString(project1CreatedSubscription.actionKey()));
        assertThat("Pojo active-status mis-match", subscription.active(), is(project1CreatedSubscription.active()));
        assertThat("Pojo eventListenerFilter should have been assigned an ID", subscription.eventFilter().id(), notNullValue());
        assertThat("Pojo eventListenerFilter should have been assigned a non-zero ID", subscription.eventFilter().id(), not(0));
        assertThat("Pojo eventListenerFilter.name mis-match", subscription.eventFilter().name(), equalTo(project1CreatedSubscription.eventFilter().name()));
        assertThat("Pojo eventListenerFilter.jsonPathFilter mis-match", subscription.eventFilter().jsonPathFilter(), equalTo(project1CreatedSubscription.eventFilter().jsonPathFilter()));

        SubscriptionEntity entity = eventSubscriptionEntityService.get(subscription.id());
        assertThat(entity, not(nullValue()));
    }

    @Test
    public void validateSubscription() throws Exception {
        Subscription subscription = Subscription.create(project1CreatedSubscription, mockUser.getLogin());
        Subscription validatedSubscription = eventSubscriptionEntityService.validate(subscription);
        assertThat("Sample subscription validation failed.", validatedSubscription, notNullValue());
    }

    @Test
    @DirtiesContext
    public void activateAndSaveSubscriptions() throws Exception {
        Subscription subscription1 = eventSubscriptionEntityService.createSubscription(Subscription.create(project1CreatedSubscription, mockUser.getLogin()));
        assertThat(subscription1, not(nullValue()));
        assertThat(subscription1.listenerRegistrationKey(), not(nullValue()));

        Subscription subscription2 = eventSubscriptionEntityService.createSubscription(Subscription.create(project2CreatedSubscription, mockUser.getLogin()));
        assertThat("Subscription 2 needs a non-null ID", subscription2.id(), not(nullValue()));
        assertThat("Subscription 1 and 2 need unique IDs", subscription2.id(), not(is(subscription1.id())));
        assertThat("Subscription 1 and 2 should have unique registration keys.", subscription2.listenerRegistrationKey().toString(), not(containsString(subscription1.listenerRegistrationKey().toString())));
    }

    @Test
    @DirtiesContext
    public void deleteSubscriptionEntity() throws Exception {
        Subscription subscription1 = eventSubscriptionEntityService.createSubscription(Subscription.create(project1CreatedSubscription, mockUser.getLogin()));
        Subscription subscription2 = eventSubscriptionEntityService.createSubscription(Subscription.create(project2CreatedSubscription, mockUser.getLogin()));
        assertThat("Expected two subscriptions in database.", eventSubscriptionEntityService.getAll().size(), equalTo(2));

        eventSubscriptionEntityService.delete(subscription1.id());
        assertThat("Expected one subscription in database after deleting one.", eventSubscriptionEntityService.getAll().size(), equalTo(1));
        assertThat("Expected remaining subscription ID to match entity not deleted.", eventSubscriptionEntityService.get(subscription2.id()).getId(), equalTo(subscription2.id()));
    }

    @Test
    public void updateSubscriptionEntity() throws Exception {

    }

    @Test
    public void testGetComponents() throws Exception {
        List<EventServiceEvent> installedEvents = componentManager.getInstalledEvents();
        assertThat("componentManager.getInstalledEvents should not return a null list", installedEvents, notNullValue());
        assertThat("componentManager.getInstalledEvents should not return an empty list", installedEvents, is(not(empty())));

        List<EventServiceActionProvider> actionProviders = componentManager.getActionProviders();
        assertThat("componentManger.getActionProviders() should not return null list of action providers", actionProviders, notNullValue());
        assertThat("componentManger.getActionProviders() should not return empty list of action providers", actionProviders, is(not(empty())));

        actionProviders = actionManager.getActionProviders();
        assertThat("actionManager.getActionProviders() should not return null list of action providers", actionProviders, notNullValue());
        assertThat("actionManager.getActionProviders() should not return empty list of action providers", actionProviders, is(not(empty())));

        List<SimpleEvent> events = eventService.getEvents();
        assertThat("eventService.getEvents() should not return a null list", events, notNullValue());
        assertThat("eventService.getEvents() should not return an empty list", events, is(not(empty())));

        List<ActionProvider> providers = eventService.getActionProviders();
        assertThat("eventService.getActionProviders() should not return a null list", providers, notNullValue());
        assertThat("eventService.getActionProviders() should not return an empty list", providers, is(not(empty())));

        List<Action> allActions = eventService.getAllActions();
        assertThat("eventService.getAllActions() should not return a null list", allActions, notNullValue());
        assertThat("eventService.getAllActions() should not return an empty list", allActions, is(not(empty())));


    }

    // ** Async Tests ** //

    @Test
    @DirtiesContext
    public void testSampleEvent() throws InterruptedException {
        MockConsumer consumer = new MockConsumer();

        Selector selector = type(SampleEvent.class);
        // Register with Reactor
        eventBus.on(selector, consumer);

        // Trigger event
        EventServiceEvent event = new SampleEvent();

        eventBus.notify(event, Event.wrap(event));

        // wait for consumer (max 1 sec.)
        synchronized (consumer) {
            consumer.wait(1000);
        }

        assertThat("Time-out waiting for eventId", consumer.getEvent(), is(notNullValue()));
    }

    @Test
    @DirtiesContext
    public void catchSubscribedEvent() throws Exception {
        EventServiceEvent event = new SampleEvent();
        String testActionKey = testAction.getAllActions().get(0).actionKey();

        eventService.getAllActions();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder().name("Test Subscription")
                                                                     .active(true)
                                                                     .eventId(event.getId())
                                                                     .customListenerId(testListener.getId())
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        eventService.validateSubscription(subscription);
        Subscription savedSubscription = eventService.createSubscription(subscription);

        // Trigger event
        eventService.triggerEvent(event);

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("List of detected events should not be null.",action.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.",action.getDetectedEvents().size(), not(0));

    }

    @Test
    @DirtiesContext
    public void registerMrSessionSubscription() throws Exception {
        EventServiceEvent testCombinedEvent = componentManager.getEvent("org.nrg.xnat.eventservice.events.TestCombinedEvent");
        assertThat("Could not load TestCombinedEvent from componentManager", testCombinedEvent, notNullValue());

        EventFilter eventServiceFilterWithJson = EventFilter.builder().jsonPathFilter("$[?(@.modality == \"MR\")]")
                                                            .build();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("FilterTestSubscription")
                                                                     .active(true)
                                                                     .projectId("PROJECTID-1")
                                                                     .eventId("org.nrg.xnat.eventservice.events.TestCombinedEvent")
                                                                     .actionKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction")
                                                                     .eventFilter(eventServiceFilterWithJson)
                                                                     .actAsEventUser(false)
                                                                     .build();
        assertThat("Json Filtered SubscriptionCreator builder failed :(", subscriptionCreator, notNullValue());

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Json Filtered Subscription creation failed :(", subscription, notNullValue());

        Subscription createdSubsciption = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() returned a null value", createdSubsciption, not(nullValue()));
        assertThat("Created subscription is missing listener registration key.", createdSubsciption.listenerRegistrationKey(), not(nullValue()));
        assertThat("Created subscription is missing DB id.", createdSubsciption.id(), not(nullValue()));

    }

    @Test
    @DirtiesContext
    public void matchMrSubscriptionToMrSession() throws Exception {
        registerMrSessionSubscription();

        // Test MR Project 1 session - match
        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());


        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("MR");
        session.setProject("PROJECTID-1");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin());

        eventService.triggerEvent(combinedEvent, "PROJECTID-1");

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.",actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.",actionProvider.getDetectedEvents().size(), not(0));
    }

    @Test
    @DirtiesContext
    public void mismatchProjectIdMrSubscriptionToMrSession() throws Exception {
        registerMrSessionSubscription();

        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);

        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("MR");
        session.setProject("PROJECTID-2");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin());

        eventService.triggerEvent(combinedEvent, "PROJECTID-2");

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should be empty (Mis-matched Project IDs.", actionProvider.getDetectedEvents(), is(empty()));
    }

    @Test
    @DirtiesContext
    public void testReactivateAllActive() throws Exception {
        // Create a working subscription
        matchMrSubscriptionToMrSession();

        List<Subscription> allSubscriptions1 = eventSubscriptionEntityService.getAllSubscriptions();
        assertThat("Expected one subscription to be created.", allSubscriptions1.size(), is(1));

        final Subscription subscription1 = allSubscriptions1.get(0);
        String regKey1 = subscription1.listenerRegistrationKey();

        eventService.reactivateAllSubscriptions();

        List<Subscription> allSubscriptions2 = eventSubscriptionEntityService.getAllSubscriptions();
        assertThat("Expected only a single subscription after reactivation", allSubscriptions2.size(), is(1));
        final Subscription subscription2 = allSubscriptions2.get(0);
        String regKey2 = subscription2.listenerRegistrationKey();

        assertThat("Expected reactivated subscription to have unique registration key.", regKey1, is(not(regKey2)));


    }

    @Test
    @DirtiesContext
    public void mismatchMrSubscriptionToCtSession() throws Exception {
        registerMrSessionSubscription();

        // Test CT Project 1 session - match
        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());


        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("CT");
        session.setProject("PROJECTID-1");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin());
        eventService.triggerEvent(combinedEvent, session.getProject());

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.",actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should be empty.",actionProvider.getDetectedEvents().size(), is(0));
    }

    class MockSingleActionProvider extends SingleActionProvider {


        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user,
                                 Long deliveryId) {

        }

        @Override
        public Map<String, ActionAttributeConfiguration> getAttributes() {
            return null;
        }
    }

    @Service
    class MockConsumer implements EventServiceListener<SampleEvent> {
        private SampleEvent event;

        public EventServiceEvent getEvent() {
            return event;
        }

        @Override
        public String getId() { return this.getClass().getCanonicalName(); }

        @Override
        public String getEventType() {
            return event.getObjectClass();
        }

        @Override
        public EventServiceListener getInstance() {
            return this;
        }

        @Override
        public UUID getInstanceId() {
            return null;
        }

        @Override
        public void setEventService(EventService eventService) { }

        @Override
        public Date getDetectedTimestamp() {
            return null;
        }

        @Override
        public void accept(Event<SampleEvent> event) {
            this.event = event.getData();
            synchronized (this) {
                notifyAll();
            }
        }

    }



}