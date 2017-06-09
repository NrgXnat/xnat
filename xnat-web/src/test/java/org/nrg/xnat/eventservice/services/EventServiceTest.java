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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static reactor.bus.selector.Selectors.matchAll;
import static reactor.bus.selector.Selectors.type;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class EventServiceTest {
    private static final Logger log = LoggerFactory.getLogger(EventServiceTest.class);

    private static final String EVENT_RESOURCE_PATTERN ="classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";

    private UserI mockUser;

    private final String FAKE_USER = "mockUser";
    private final Integer FAKE_USER_ID = 1234;

    @Autowired private EventBus eventBus;
    @Autowired private TestListener testListener;
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


    private SubscriptionCreator projectCreatedSubscription;
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

        EventFilter eventServiceFilter = EventFilter.builder()
                                                    .addProjectId("PROJECTID-1")
                                                    .addProjectId("PROJECTID-2")
                                                    .build();

        projectCreatedSubscription = SubscriptionCreator.builder()
                                                        .name("TestSubscription")
                                                        .active(true)
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
        mrScan2.setProjectId("PROJECTID-2");
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
        assertThat(contextService.getBean("testListener"), not(nullValue()));
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

        match = JsonPath.parse(jsonMrScan2).read(mrCtProj2Filter);
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
        System.out.println("\nFound " + events.size() + " Event classes:");
        for (SimpleEvent event : events) {
            System.out.println(event.toString());
        }
        assert(events != null && events.size() == eventPropertyFileCount);
    }

    @Test
    public void getInstalledActionProviders() throws Exception {
        System.out.println("Installed Action Providers\n");
        assertThat("componentManager.getActionProviders() should not be null.", componentManager.getActionProviders(), notNullValue());
        assertThat("componentManager.getActionProviders() should not be empty.", componentManager.getActionProviders().size(), not(equalTo(0)));
        for(EventServiceActionProvider provider:componentManager.getActionProviders()) {
            System.out.println(provider.toString());
        }

    }

    @Test
    public void getInstalledActions() throws Exception {
        List<Action> actions = eventService.getAllActions(null);
        System.out.println("\nFound " + actions.size() + " Actions:");
        for (Action action : actions) {
            System.out.println(action.toString());
        }
        assert(actions != null && actions.size() > 0);
    }

    @Test
    public void createSubscription() throws Exception {
        List<SimpleEvent> events = mockEventService.getEvents();
        assertThat("eventService.getEvents() should not return a null list", events, notNullValue());
        assertThat("eventService.getEvents() should not return an empty list", events, is(not(empty())));

        List<Action> actions = mockEventService.getAllActions(null);
        assertThat("eventService.getAllActions() should not return a null list", actions, notNullValue());
        assertThat("eventService.getAllActions() should not return an empty list", actions, is(not(empty())));

        List<EventServiceListener> listeners = mockComponentManager.getInstalledListeners();
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

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getID());
        assertThat("Created subscription should not be null", subscription, notNullValue());

        eventService.validateSubscription(subscription);

        Subscription savedSubscription = mockEventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() should not return null", savedSubscription, notNullValue());
        assertThat("subscription id should not be null", savedSubscription.id(), notNullValue());
        assertThat("subscription id should not be zero", savedSubscription.id(), not(0));
        assertThat("subscription registration key should not be null", savedSubscription.listenerRegistrationKey(), notNullValue());
        assertThat("subscription registration key should not be empty", savedSubscription.listenerRegistrationKey(), not(""));

    }

    @Test
    public void listSubscriptions() throws Exception {
        createSubscription();
        assertThat("No subscriptions found.", eventService.getSubscriptions(), is(not(empty())));
        for (Subscription subscription:eventService.getSubscriptions()) {
            assertThat("subscription id is null for " + subscription.name(), subscription.id(), notNullValue());
            assertThat("subscription id is zero (0) for " + subscription.name(), subscription.id(), not(0));
        }
    }

    @Test
    public void saveSubscriptionEntity() throws Exception {
        Subscription subscription = eventSubscriptionEntityService.save(Subscription.create(projectCreatedSubscription));
        assertThat("EventSubscriptionEntityService.save should not create a null entity.", subscription, not(nullValue()));
        assertThat("Saved subscription entity should have been assigned a database ID.", subscription.id(), not(nullValue()));
        assertThat("Pojo name mis-match", subscription.name(), containsString(projectCreatedSubscription.name()));
        assertThat("Pojo actionService mis-match", subscription.actionKey(), containsString(projectCreatedSubscription.actionKey()));
        assertThat("Pojo active-status mis-match", subscription.active(), is(projectCreatedSubscription.active()));
        assertThat("Pojo eventListenerFilter mis-match", subscription.eventFilter(), equalTo(projectCreatedSubscription.eventFilter()));

        SubscriptionEntity entity = eventSubscriptionEntityService.get(subscription.id());
        assertThat(entity, not(nullValue()));
    }


    @Test
    public void activateAndSaveSubscriptions() throws Exception {
        Subscription subscription1 = eventSubscriptionEntityService.createSubscription(Subscription.create(projectCreatedSubscription));
        assertThat(subscription1, not(nullValue()));
        assertThat(subscription1.listenerRegistrationKey(), not(nullValue()));

        Subscription subscription2 = eventSubscriptionEntityService.createSubscription(Subscription.create(projectCreatedSubscription));
        assertThat("Subscription 2 needs a non-null ID", subscription2.id(), not(nullValue()));
        assertThat("Subscription 1 and 2 need unique IDs", subscription2.id(), not(is(subscription1.id())));
        assertThat("Subscription 1 and 2 should have unique registration keys.", subscription2.listenerRegistrationKey().toString(), not(containsString(subscription1.listenerRegistrationKey().toString())));
    }

    @Test
    public void deleteSubscriptionEntity() throws Exception {
        Subscription subscription1 = eventSubscriptionEntityService.createSubscription(Subscription.create(projectCreatedSubscription));
        Subscription subscription2 = eventSubscriptionEntityService.createSubscription(Subscription.create(projectCreatedSubscription));
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

        List<Action> allActions = eventService.getAllActions(null);
        assertThat("eventService.getAllActions() should not return a null list", allActions, notNullValue());
        assertThat("eventService.getAllActions() should not return an empty list", allActions, is(not(empty())));


    }

    // ** Async Tests ** //

    @Test
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
    public void catchSubscribedEvent() throws Exception {
        createSubscription();

        // Trigger event
        EventServiceEvent event = new SampleEvent();
        eventBus.notify(event, Event.wrap(event));

        // wait for listener (max 1 sec.)
        synchronized (testListener) {
            testListener.wait(1000);
        }

        assertThat("List of detected events should not be null.",testListener.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.",testListener.getDetectedEvents().size(), not(0));

    }

    @Test
    public void registerMrSessionSubscription() throws Exception {
        EventServiceEvent testCombinedEvent = componentManager.getEvent("org.nrg.xnat.eventservice.events.TestCombinedEvent");
        assertThat("Could not load TestCombinedEvent from componentManager", testCombinedEvent, notNullValue());

        EventFilter eventServiceFilterWithJson = EventFilter.builder()
                                                            .addProjectId("PROJECTID-1")
                                                            .addProjectId("PROJECTID-2")
                                                            .jsonPathFilter("$[?(@.modality == \"MR\")]")
                                                            .build();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("FilterTestSubscription")
                                                                     .active(true)
                                                                     .eventId("org.nrg.xnat.eventservice.events.TestCombinedEvent")
                                                                     .actionKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction")
                                                                     .eventFilter(eventServiceFilterWithJson)
                                                                     .actAsEventUser(false)
                                                                     .build();
        assertThat("Json Filtered SubscriptionCreator builder failed :(", subscriptionCreator, notNullValue());

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getID());
        assertThat("Json Filtered Subscription creation failed :(", subscription, notNullValue());

        Subscription createdSubsciption = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() returned a null value", createdSubsciption, not(nullValue()));
        assertThat("Created subscription is missing listener registration key.", createdSubsciption.listenerRegistrationKey(), not(nullValue()));
        assertThat("Created subscription is missing DB id.", createdSubsciption.id(), not(nullValue()));

    }

    @Test
    public void matchMrSubscriptionToMrSession() throws Exception {
        registerMrSessionSubscription();

        // Test MR Project 1 session - match
        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());


        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("MR");
        session.setProject("PROJECTID-1");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser);
        String filter =  "project-id:PROJECTID-1";
        eventBus.notify(filter, Event.wrap(combinedEvent));

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.",actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.",actionProvider.getDetectedEvents().size(), not(0));
    }


    @Test
    public void testReactivateAllActive() throws Exception {
        // Create a working subscription
        matchMrSubscriptionToMrSession();

        List<Subscription> allSubscriptions = eventSubscriptionEntityService.getAllSubscriptions();
        assertThat(allSubscriptions.size(), is(1));

        final Subscription subscription = allSubscriptions.get(0);
        assertThat(subscription.useCounter(), is(1));
        // reactivate subscription
        eventService.reactivateAllSubscriptions();

    }

    @Test
    public void mismatchMrSubscriptionToCtSession() throws Exception {
        registerMrSessionSubscription();

        // Test CT Project 1 session - match
        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());


        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("CT");
        session.setProject("PROJECTID-1");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser);
        String filter =  "project-id:PROJECTID-1";
        eventBus.notify(filter, Event.wrap(combinedEvent));

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
        public List<String> getAttributeKeys() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user) {

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
        public void accept(Event<SampleEvent> event) {
            this.event = event.getData();
            synchronized (this) {
                notifyAll();
            }
        }

    }



}