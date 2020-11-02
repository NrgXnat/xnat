package org.nrg.xnat.eventservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.actions.EventServiceLoggingAction;
import org.nrg.xnat.eventservice.actions.SingleActionProvider;
import org.nrg.xnat.eventservice.actions.TestAction;
import org.nrg.xnat.eventservice.config.EventServiceTestConfig;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.events.ProjectEvent;
import org.nrg.xnat.eventservice.events.SampleEvent;
import org.nrg.xnat.eventservice.events.ScanEvent;
import org.nrg.xnat.eventservice.events.SessionEvent;
import org.nrg.xnat.eventservice.events.SubjectEvent;
import org.nrg.xnat.eventservice.events.TestCombinedEvent;
import org.nrg.xnat.eventservice.events.WorkflowStatusChangeEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.listeners.TestListener;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.ActionProvider;
import org.nrg.xnat.eventservice.model.EventFilter;
import org.nrg.xnat.eventservice.model.EventFilterCreator;
import org.nrg.xnat.eventservice.model.EventSignature;
import org.nrg.xnat.eventservice.model.ProjectEventFilterCreator;
import org.nrg.xnat.eventservice.model.ProjectSubscriptionCreator;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionCreator;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.TimedEventStatus;
import org.nrg.xnat.eventservice.model.xnat.Scan;
import org.nrg.xnat.eventservice.model.xnat.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static reactor.bus.selector.Selectors.type;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class EventServiceIntegrationTest {

    private static final String EVENT_RESOURCE_PATTERN = "classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";

    private UserI mockUser;

    private final String  FAKE_USER    = "mockUser";
    private final Integer FAKE_USER_ID = 1234;

    @Autowired
    private EventBus                          eventBus;
    @Autowired
    private TestListener                      testListener;
    @Autowired
    private EventServiceActionProvider        testAction;
    @Autowired
    @Lazy
    private EventService                      eventService;
    @Autowired
    private EventService                      mockEventService;
    @Autowired
    private EventSubscriptionEntityService    eventSubscriptionEntityService;
    @Autowired
    private ContextService                    contextService;
    @Autowired
    private EventServiceComponentManager      componentManager;
    @Autowired
    private EventServiceComponentManager      mockComponentManager;
    @Autowired
    private ActionManager                     actionManager;
    @Autowired
    private ActionManager                     mockActionManager;
    @Autowired
    private ObjectMapper                      objectMapper;
    @Autowired
    private EventServiceLoggingAction         mockEventServiceLoggingAction;
    @Autowired
    private UserManagementServiceI            mockUserManagementServiceI;
    @Autowired
    private SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService;
    @Autowired
    private EventServicePrefsBean             mockEventServicePrefsBean;

    private SubscriptionCreator project1CreatedSubscription;
    private EventFilterCreator  project1EventFilterCreator;
    private SubscriptionCreator project2CreatedSubscription;
    private EventFilterCreator  project2EventFilterCreator;
    private Scan                mrScan1 = new Scan();
    private Scan                mrScan2 = new Scan();
    private Scan                ctScan1 = new Scan();

    private Session mrSession = new Session();
    private Session ctSession = new Session();

    //private Project project1 = new Project("PROJECTID-1", mockUser);
    //private Project project2 = new Project("PROJECTID-2", mockUser);
    //private Subject subject1 = new Subject("SUBJECTID-1", mockUser);
    //private Subject subject2 = new Subject("SUBJECTID-2", mockUser);

    @Before
    public void setUp() throws Exception {
        project1EventFilterCreator = EventFilterCreator.builder()
                                                       .projectIds(Arrays.asList("PROJECTID-1"))
                                                       .eventType("org.nrg.xnat.eventservice.events.ProjectEvent")
                                                       .status("CREATED")
                                                       .build();
        project1CreatedSubscription = SubscriptionCreator.builder()
                                                         .name("TestSubscription")
                                                         .active(true)
                                                         .customListenerId("org.nrg.xnat.eventservice.listeners.TestListener")
                                                         .actionKey("org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction")
                                                         .eventFilter(project1EventFilterCreator)
                                                         .actAsEventUser(false)
                                                         .build();

        project2EventFilterCreator = EventFilterCreator.builder()
                                                       .projectIds(Arrays.asList("PROJECTID-2"))
                                                       .eventType("org.nrg.xnat.eventservice.events.ProjectEvent")
                                                       .status("CREATED")
                                                       .build();
        project2CreatedSubscription = SubscriptionCreator.builder()
                                                         .name("TestSubscription2")
                                                         .active(true)
                                                         .customListenerId("org.nrg.xnat.eventservice.listeners.TestListener")
                                                         .actionKey("org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction")
                                                         .eventFilter(project2EventFilterCreator)
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
        when(mockEventServiceLoggingAction.getActions(Matchers.any(String.class), Matchers.any(List.class), Matchers.any(UserI.class))).thenReturn(null);

        // Mock prefs bean
        when(mockEventServicePrefsBean.getEnabled()).thenReturn(true);


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
    public void getInstalledEvents() throws Exception {
        List<SimpleEvent> events                 = eventService.getEvents();
        Integer           eventPropertyFileCount = BasicXnatResourceLocator.getResources(EVENT_RESOURCE_PATTERN).size();

        assert (events != null && events.size() == eventPropertyFileCount);
    }

    @Test
    public void getInstalledActionProviders() throws Exception {
        assertThat("componentManager.getActionProviders() should not be null.", componentManager.getActionProviders(), notNullValue());
        assertThat("componentManager.getActionProviders() should not be empty.", componentManager.getActionProviders().size(), not(equalTo(0)));
    }

    @Test
    public void getInstalledActions() throws Exception {
        List<Action> actions = eventService.getAllActions();
        assert (actions != null && actions.size() > 0);
    }

    @Test
    public void getInstalledListeners() throws Exception {
        List<EventServiceListener> listeners = componentManager.getInstalledListeners();
        assertThat(listeners, notNullValue());
        assertThat(listeners, not(is(empty())));
    }

    @Test
    public void checkXsiTypeToObjectMapping() throws Exception {

        List<String> projectXsiTypes = componentManager.getXsiTypes(XnatProjectdataI.class);
        assertThat(projectXsiTypes, is(notNullValue()));

        List<String> subjectXsiTypes = componentManager.getXsiTypes(XnatSubjectdataI.class);
        assertThat(subjectXsiTypes, is(notNullValue()));

        List<String> sessionXsiTypes = componentManager.getXsiTypes(XnatImagesessiondataI.class);
        assertThat(sessionXsiTypes, is(notNullValue()));

        List<String> scanXsiTypes = componentManager.getXsiTypes(XnatImagescandataI.class);
        assertThat(scanXsiTypes, is(notNullValue()));

        List<String> assessorXsiTypes = componentManager.getXsiTypes(XnatImageassessordataI.class);
        assertThat(assessorXsiTypes, is(notNullValue()));

    }

    @Test
    @DirtiesContext
    public void createSubscription() throws Exception {
        List<SimpleEvent> events = mockEventService.getEvents();
        assertThat("eventService.getEvents() should not return a null list", events, notNullValue());
        assertThat("eventService.getEvents() should not return an empty list", events, is(not(empty())));

        List<Action> actions = mockEventService.getAllActions();
        assertThat("eventService.getAllActions() should not return a null list", actions, notNullValue());
        assertThat("eventService.getAllActions() should not return an empty list", actions, is(not(empty())));

        List<EventServiceListener> listeners = mockComponentManager.getInstalledListeners();
        assertThat("componentManager.getInstalledListeners() should not return a null list", listeners, notNullValue());
        assertThat("componentManager.getInstalledListeners() should not return an empty list", listeners, is(not(empty())));

        String eventId      = events.get(0).id();
        String actionId     = actions.get(0).id();
        String actionKey    = actions.get(0).actionKey();
        String listenerType = listeners.get(0).getClass().getCanonicalName();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder().name("Test Subscription")
                                                                     .active(true)
                                                                     .customListenerId(listenerType)
                                                                     .actionKey(actionKey)
                                                                     .actAsEventUser(false)
                                                                     .eventFilter(EventFilterCreator.builder().eventType(eventId).build())
                                                                     .build();

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Created subscription should not be null", subscription, notNullValue());

        subscription = eventService.validateSubscription(subscription);

        assertThat("Validated subscription should not be null", subscription, notNullValue());

        Subscription savedSubscription = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() should not return null", savedSubscription, notNullValue());
        assertThat("subscription id should not be null", savedSubscription.id(), notNullValue());
        assertThat("subscription id should not be zero", savedSubscription.id(), not(0));
        assertThat("subscription registration key should not be null", eventSubscriptionEntityService.getListenerId(savedSubscription.id()), notNullValue());

        subscriptionCreator = SubscriptionCreator.builder().name("Test 2 Subscription")
                                                 .active(true)
                                                 .eventFilter(EventFilterCreator.builder().eventType(eventId).build())
                                                 .customListenerId(listenerType)
                                                 .actionKey(actionKey)
                                                 .actAsEventUser(false)
                                                 .build();

        subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());

        eventService.validateSubscription(subscription);

        Subscription secondSavedSubscription = eventService.createSubscription(subscription);

        assertThat("Subscriptions should have unique listener IDs", eventSubscriptionEntityService.getListenerId(savedSubscription.id()), not(eventSubscriptionEntityService.getListenerId(secondSavedSubscription.id())));
        assertThat("Subscriptions should have unique IDs", savedSubscription.id(), not(secondSavedSubscription.id()));
    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
    @Test
    @DirtiesContext
    public void createSubscriptionWithBlankName() throws Exception {
        EventServiceEvent testCombinedEvent = componentManager.getEvent("org.nrg.xnat.eventservice.events.TestCombinedEvent");
        assertThat("Could not load TestCombinedEvent from componentManager", testCombinedEvent, notNullValue());

        String eventType = "org.nrg.xnat.eventservice.events.TestCombinedEvent";
        String projectId = "PROJECTID-1";
        EventFilterCreator eventServiceFilterWithJson = EventFilterCreator.builder()
                                                                          .eventType(eventType)
                                                                          .projectIds(Arrays.asList(projectId))
                                                                          .jsonPathFilter("(\"MR\" in @.modalities-in-study)")
                                                                          .build();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("")
                                                                     .active(true)
                                                                     .actionKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction")
                                                                     .eventFilter(eventServiceFilterWithJson)
                                                                     .actAsEventUser(false)
                                                                     .build();
        assertThat("Json Filtered SubscriptionCreator builder failed :(", subscriptionCreator, notNullValue());

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Json Filtered Subscription creation failed :(", subscription, notNullValue());

        List<String> names = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            Subscription createdSubsciption = eventService.createSubscription(subscription);
            assertThat("eventService.createSubscription() returned a null value", createdSubsciption, not(nullValue()));
            assertThat("Expected subscription to have auto-generated name", createdSubsciption.name(), notNullValue());
            assertThat("Expected subscription to have auto-generated name", createdSubsciption.name(), not(""));
            assertThat("Expected subscription name to be unique", names, not(contains(createdSubsciption.name())));
            names.add(createdSubsciption.name());
        }

    }

    @Ignore("Fails with error message: No qualifying bean of type 'org.nrg.xnat.eventservice.events.ImageAssessorEvent' available")
    @Test
    public void listSubscriptions() throws Exception {
        createSubscription();
        assertThat("No subscriptions found.", eventService.getSubscriptions(), is(not(empty())));
        assertThat("Two subscriptions expected.", eventService.getSubscriptions().size(), is(2));
        for (Subscription subscription : eventService.getSubscriptions()) {
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
        Subscription subscription          = Subscription.create(project1CreatedSubscription, mockUser.getLogin());
        Subscription validatedSubscription = eventSubscriptionEntityService.validate(subscription);
        assertThat("Sample subscription validation failed.", validatedSubscription, notNullValue());
    }

    @Test
    @DirtiesContext
    public void activateAndSaveSubscriptions() throws Exception {
        Subscription subscription1 = eventSubscriptionEntityService.createSubscription(Subscription.create(project1CreatedSubscription, mockUser.getLogin()));
        assertThat(subscription1, not(nullValue()));
        assertThat("Expected an active subscription entry with subscription id = " + Long.toString(subscription1.id()), eventSubscriptionEntityService.getActiveRegistrationSubscriptionIds().iterator().next(), equalTo(subscription1.id()));

        Subscription subscription2 = eventSubscriptionEntityService.createSubscription(Subscription.create(project2CreatedSubscription, mockUser.getLogin()));
        assertThat("Subscription 2 needs a non-null ID", subscription2.id(), not(nullValue()));
        assertThat("Subscription 1 and 2 need unique IDs", subscription2.id(), not(is(subscription1.id())));
        assertThat("Subscription 1 and 2 should have unique registration keys.",
                eventSubscriptionEntityService.getListenerId(subscription1.id()).toString(),
                not(containsString(eventSubscriptionEntityService.getListenerId(subscription2.id()).toString())));
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

    @Test
    public void testJsonPathFilterConfiguration() throws Throwable {
        final Configuration jaywayConf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

        String scanJson = objectMapper.writeValueAsString(mrScan1);

        List singleQuoteMatch = JsonPath.using(jaywayConf).parse(scanJson).read("$[?(@.xsiType in ['xnat:Scan'])]");
        assertThat(singleQuoteMatch, notNullValue());
        assertThat(singleQuoteMatch.size(), is(not(0)));

        List doubleQuoteMatch = JsonPath.using(jaywayConf).parse(scanJson).read("$[?((@.xsiType in [\"xnat:Scan\"]))]");
        assertThat(doubleQuoteMatch, notNullValue());
        assertThat(doubleQuoteMatch.size(), is(not(0)));


    }

    @Test
    public void testJsonPathFilterSelector() throws Throwable {
        String       eventId        = "some.test.EventId";
        List<String> filterProjects = Arrays.asList("ProjectId1", "ProjectId2");
        String       eventProject   = "ProjectId1";
        String       status         = "CREATED";

        String signatureJson = objectMapper.writeValueAsString(EventSignature.builder().eventType(eventId).projectId(eventProject).status(status).build());
        Filter filter        = EventFilter.builder().eventType(eventId).projectIds(filterProjects).status(status).build().buildReactorFilter();

        List match = JsonPath.read(signatureJson, "$.[?]", filter);

        assertThat(match, notNullValue());
        assertThat(match.size(), is(not(0)));

        String noProjectSignature = objectMapper.writeValueAsString(
                EventSignature.builder().eventType(eventId).status(status).build());
        Filter noProjectEventFilter = EventFilter.builder().eventType(eventId).status(status).build().buildReactorFilter();

        match = JsonPath.read(noProjectSignature, "$.[?]", noProjectEventFilter);

        assertThat(match, notNullValue());
        assertThat(match.size(), is(not(0)));

        match = JsonPath.read(signatureJson, "$.[?]", noProjectEventFilter);

        assertThat(match, notNullValue());
        assertThat(match.size(), is(not(0)));

        match = JsonPath.read(noProjectSignature, "$.[?]", filter);

        assertThat(match, notNullValue());
        assertThat("JsonPath filter match should be empty. Event signature contains no project, but projects are specified on filter.", match.size(), is(0));

        String otherProjectSignature = objectMapper.writeValueAsString(
                EventSignature.builder().eventType(eventId).projectId("ProjectId").status(status).build());
        Filter otherProjectEventFilter = EventFilter.builder().eventType(eventId).projectIds(Arrays.asList("SomethingElse", "ADifferentOne")).status(status).build().buildReactorFilter();

        match = JsonPath.read(otherProjectSignature, "$.[?]", filter);

        assertThat(match, notNullValue());
        assertThat("JsonPath filter match should be empty. Event signature contains projectId not contained in filter.", match.size(), is(0));

        match = JsonPath.read(signatureJson, "$.[?]", otherProjectEventFilter);

        assertThat(match, notNullValue());
        assertThat("JsonPath filter match should be empty. Event signature contains projectId not contained in filter.", match.size(), is(0));

        Filter otherStatusEventFilter = EventFilter.builder().eventType(eventId).projectIds(filterProjects).status("DIFFERENT_STATUS").build().buildReactorFilter();
        match = JsonPath.read(signatureJson, "$.[?]", otherStatusEventFilter);

        assertThat(match, notNullValue());
        assertThat("JsonPath filter match should be empty. Event signature and filter contain different status.", match.size(), is(0));

        Filter otherEventIdFilter = EventFilter.builder().eventType("SOME_OTHER_EVENT").projectIds(filterProjects).status(status).build().buildReactorFilter();
        match = JsonPath.read(signatureJson, "$.[?]", otherEventIdFilter);

        assertThat(match, notNullValue());
        assertThat("JsonPath filter match should be empty. Event signature and filter contain different eventIds.", match.size(), is(0));
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

        assertThat("Time-out waiting for eventType", consumer.getEvent(), is(notNullValue()));
    }

    @Ignore("Fails with error message: Could not compile jsonPath filter. null")
    @Test
    @DirtiesContext
    public void catchSubscribedEvent() throws Exception {
        EventServiceEvent event         = new SampleEvent();
        String            testActionKey = testAction.getAllActions().get(0).actionKey();

        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(event.getType()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder().name("Test Subscription")
                                                                     .active(true)
                                                                     .eventFilter(filterCreator)
                                                                     .customListenerId(testListener.getType())
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
        assertThat("List of detected events should not be null.", action.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.", action.getDetectedEvents().size(), not(0));
    }

    @Ignore("Fails with error message: Could not compile jsonPath filter. null")
    @Test
    @DirtiesContext
    public void checkSubscriptionDeliveryEntry() throws Exception {
        catchSubscribedEvent();
        List<SubscriptionDelivery> subscriptionDeliveries = eventService.getSubscriptionDeliveries(null, null, null);
        assertThat("subscriptionDeliveries table is null. Expected one entry.", subscriptionDeliveries, notNullValue());
        assertThat("subscriptionDeliveries table is empty. Expected one entry.", subscriptionDeliveries.size(), is(1));

        List<TimedEventStatus> eventStatuses = subscriptionDeliveries.get(0).timedEventStatuses();
        assertThat("TimedEventStatus table is null. Expected entries.", eventStatuses, notNullValue());
        assertThat("", eventStatuses.get(eventStatuses.size() - 1).status(), is("ACTION_COMPLETE"));
    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
    @Test
    @DirtiesContext
    public void registerMrSessionSubscription() throws Exception {
        EventServiceEvent testCombinedEvent = componentManager.getEvent("org.nrg.xnat.eventservice.events.TestCombinedEvent");
        assertThat("Could not load TestCombinedEvent from componentManager", testCombinedEvent, notNullValue());

        String projectId = "PROJECTID-1";
        String eventType = "org.nrg.xnat.eventservice.events.TestCombinedEvent";
        EventFilterCreator eventServiceFilterWithJson = EventFilterCreator.builder()
                                                                          .eventType(eventType)
                                                                          .projectIds(Arrays.asList(projectId))
                                                                          .jsonPathFilter("( \"MR\" in @.modalities-in-study)")
                                                                          .build();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("FilterTestSubscription")
                                                                     .active(true)
                                                                     .actionKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction")
                                                                     .eventFilter(eventServiceFilterWithJson)
                                                                     .actAsEventUser(false)
                                                                     .build();
        assertThat("Json Filtered SubscriptionCreator builder failed :(", subscriptionCreator, notNullValue());

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Json Filtered Subscription creation failed :(", subscription, notNullValue());

        Subscription createdSubsciption = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() returned a null value", createdSubsciption, not(nullValue()));
        assertThat("Created subscription is missing listener registration key.", eventSubscriptionEntityService.getListenerId(createdSubsciption.id()), not(nullValue()));
        assertThat("Created subscription is missing DB id.", createdSubsciption.id(), not(nullValue()));

    }

    @Test
    @DirtiesContext
    public void registerFilterablePayloadWorkflowStatusChangeSubscription() throws Exception {
        EventServiceEvent event = componentManager.getEvent("org.nrg.xnat.eventservice.events.WorkflowStatusChangeEvent");
        assertThat("Could not load WorkflowStatusChangeEvent from componentManager", event, notNullValue());

        String projectId = "PROJECTID-1";
        String eventType = event.getType();
        EventFilterCreator eventServiceFilterWithJson = EventFilterCreator.builder()
                                                                          .eventType(eventType)
                                                                          .projectIds(Arrays.asList(projectId))
                                                                          .jsonPathFilter("(@.status == \"In Progress\")")
                                                                          .build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("PayloadFilterTestSubscription")
                                                                     .active(true)
                                                                     .actionKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction")
                                                                     .eventFilter(eventServiceFilterWithJson)
                                                                     .actAsEventUser(false)
                                                                     .build();
        assertThat("Json Filtered PayloadSubscriptionCreator builder failed :(", subscriptionCreator, notNullValue());

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Json Filtered Payload Subscription creation failed :(", subscription, notNullValue());

        Subscription createdSubsciption = eventService.createSubscription(subscription);
        assertThat("eventService.createSubscription() returned a null value", createdSubsciption, not(nullValue()));
        assertThat("Created subscription is missing listener registration key.", eventSubscriptionEntityService.getActiveRegistrationSubscriptionIds().size(), is(1));
        assertThat("Created subscription is missing DB id.", createdSubsciption.id(), not(nullValue()));
    }

    @Test
    @DirtiesContext
    public void tryToBreakReactorWithStringEventKey() throws Exception {
        String finished = null;
        try {
            registerFilterablePayloadWorkflowStatusChangeSubscription();
            eventBus.notify("org.this.could.cause.problems", Event.wrap("MisterBug"));
            eventBus.notify(Event.wrap("MrsBug"));
            finished = "yay";
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        assertThat("Exception raised when attempting to handle string event key.", finished, notNullValue());
    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
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

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin(), TestCombinedEvent.Status.CREATED, "PROJECTID-1");

        eventService.triggerEvent(combinedEvent);

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.", actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.", actionProvider.getDetectedEvents().size(), not(0));
    }

    @Test
    @DirtiesContext
    public void matchWorkflowStatusChangeEvent() throws Exception {
        registerFilterablePayloadWorkflowStatusChangeSubscription();

        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());

        String              projectId = "PROJECTID-1";
        WorkflowStatusEvent workflow  = new WorkflowStatusEvent();
        workflow.setStatus("In Progress");
        workflow.setJustification("Unit Test");
        workflow.setEventSpecificFields(Sets.newHashSet());
        WorkflowStatusChangeEvent workflowStatusChangeEvent = new WorkflowStatusChangeEvent(workflow, mockUser.getLogin(), WorkflowStatusChangeEvent.Status.CHANGED, projectId, "wrk:workflowData");

        eventService.triggerEvent(workflowStatusChangeEvent);

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.", actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should not be empty.", actionProvider.getDetectedEvents().size(), not(0));
    }

    @Test
    @DirtiesContext
    public void mismatchWorkflowStatusChangeEvent() throws Exception {
        registerFilterablePayloadWorkflowStatusChangeSubscription();

        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);
        assertThat("Could not load TestAction from actionManager", testAction, notNullValue());

        String              projectId = "PROJECTID-1";
        WorkflowStatusEvent workflow  = new WorkflowStatusEvent();
        workflow.setStatus("Complete");
        workflow.setJustification("Unit Test");
        workflow.setEventSpecificFields(Sets.newHashSet());
        WorkflowStatusChangeEvent workflowStatusChangeEvent = new WorkflowStatusChangeEvent(workflow, mockUser.getLogin(), WorkflowStatusChangeEvent.Status.CHANGED, projectId, "wrk:workflowData");

        eventService.triggerEvent(workflowStatusChangeEvent);

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.", actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should be empty.", actionProvider.getDetectedEvents().size(), is(0));
    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
    @Test
    @DirtiesContext
    public void mismatchProjectIdMrSubscriptionToMrSession() throws Exception {
        registerMrSessionSubscription();

        Action testAction = actionManager.getActionByKey("org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction", mockUser);

        XnatImagesessiondataI session = new XnatImagesessiondataBean();
        session.setModality("MR");
        session.setProject("PROJECTID-2");
        session.setSessionType("xnat:imageSessionData");

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin(), TestCombinedEvent.Status.CREATED, "PROJECTID-2");

        eventService.triggerEvent(combinedEvent);

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should be empty (Mis-matched Project IDs.", actionProvider.getDetectedEvents(), is(empty()));
    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
    @Test
    @DirtiesContext
    public void testReactivateAllActive() throws Exception {
        // Create a working subscription
        matchMrSubscriptionToMrSession();

        List<Subscription> allSubscriptions1 = eventSubscriptionEntityService.getAllSubscriptions();
        assertThat("Expected one subscription to be created.", allSubscriptions1.size(), is(1));

        final Subscription subscription1 = allSubscriptions1.get(0);
        String             regKey1       = eventSubscriptionEntityService.getListenerId(subscription1.id()).toString();

        eventService.reactivateAllSubscriptions();

        List<Subscription> allSubscriptions2 = eventSubscriptionEntityService.getAllSubscriptions();
        assertThat("Expected only a single subscription after reactivation", allSubscriptions2.size(), is(1));
        final Subscription subscription2 = allSubscriptions2.get(0);
        String             regKey2       = eventSubscriptionEntityService.getListenerId(subscription2.id()).toString();

        assertThat("Expected reactivated subscription to have unique registration key.", regKey1, is(not(regKey2)));


    }

    @Ignore("Fails with error message: Could not load TestCombinedEvent from componentManager Expected: not null but: was null")
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

        TestCombinedEvent combinedEvent = new TestCombinedEvent(session, mockUser.getLogin(), TestCombinedEvent.Status.CREATED, session.getProject());
        eventService.triggerEvent(combinedEvent);

        // wait for async action (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }

        TestAction actionProvider = (TestAction) testAction.provider();
        assertThat("List of detected events should not be null.", actionProvider.getDetectedEvents(), notNullValue());
        assertThat("List of detected events should be empty.", actionProvider.getDetectedEvents().size(), is(0));
    }


    @Test
    @DirtiesContext
    public void catchSubjectEventWithProjectSubscription() throws Exception {
        // Create a user
        String          projectId1 = "PROJECTID_1";
        String          subjectID  = "TestSubject";
        XnatSubjectdata subject    = new XnatSubjectdata(mockUser);
        subject.setProject(projectId1);
        subject.setId(subjectID);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new SubjectEvent().getType()).projectIds(Arrays.asList(projectId1)).status(SubjectEvent.Status.CREATED.name()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .active(true)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .eventFilter(filterCreator)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger Subject Created Event
        eventService.triggerEvent(new SubjectEvent(subject, mockUser.getLogin(), SubjectEvent.Status.CREATED, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SubjectEvent", action.getDetectedEvents().get(0).getType(), containsString("SubjectEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }

    @Test
    @DirtiesContext
    public void catchSubjectEventWithSiteSubscription() throws Exception {
        // Create a user
        String          projectId1 = "";
        String          subjectID  = "TestSubject";
        XnatSubjectdata subject    = new XnatSubjectdata(mockUser);
        subject.setProject(projectId1);
        subject.setId(subjectID);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new SubjectEvent().getType()).projectIds(Arrays.asList(projectId1)).status(SubjectEvent.Status.CREATED.name()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .eventFilter(filterCreator)
                                                                     .active(true)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger Subject Created Event
        eventService.triggerEvent(new SubjectEvent(subject, mockUser.getLogin(), SubjectEvent.Status.CREATED, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SubjectEvent", action.getDetectedEvents().get(0).getType(), containsString("SubjectEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));

    }

    @Test
    @DirtiesContext
    public void missSubjectEventWithDifferentProjectSubscription() throws Exception {
        // Create a user
        String          projectId1 = "PROJECTID_1";
        String          projectId2 = "PROJECTID-2";
        String          subjectID  = "TestSubject";
        XnatSubjectdata subject    = new XnatSubjectdata(mockUser);
        subject.setProject(projectId1);
        subject.setId(subjectID);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new SubjectEvent().getType()).projectIds(Arrays.asList(projectId1)).status(SubjectEvent.Status.CREATED.name()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .active(true)
                                                                     .eventFilter(filterCreator)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger Subject Created Event
        eventService.triggerEvent(new SubjectEvent(subject, mockUser.getLogin(), SubjectEvent.Status.CREATED, projectId2));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected zero detected events.", action.getDetectedEvents().size(), is(0));
    }

    @Test
    @DirtiesContext
    public void catchSessionArchiveEventWithProjectId() throws Exception {
        String projectId1 = "PROJECTID_1";

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId1);
        session.setSessionType("xnat:imageSessionData");

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new SessionEvent().getType()).projectIds(Arrays.asList(projectId1)).status(SubjectEvent.Status.CREATED.name()).build();

        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .active(true)
                                                                     .eventFilter(filterCreator)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger SessionEvent
        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }

    @Test
    @DirtiesContext
    public void catchScanArchiveEventWithProjectId() throws Exception {
        String projectId1 = "PROJECTID_1";

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId1);
        session.setSessionType("xnat:imageSessionData");
        session.setId("SESSION_ID");

        XnatImagescandata scan = new XnatImagescandata();
        scan.setImageSessionData(session);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new ScanEvent().getType()).projectIds(Arrays.asList(projectId1)).status(SubjectEvent.Status.CREATED.name()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .active(true)
                                                                     .eventFilter(filterCreator)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger ScanArchiveEvent
        eventService.triggerEvent(new ScanEvent(scan, mockUser.getLogin(), ScanEvent.Status.CREATED, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type ScanEvent", action.getDetectedEvents().get(0).getType(), containsString("ScanEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }


    @Test
    @DirtiesContext
    public void create1000SubscriptionsCatchOneWithDifferentEventType() throws Exception {
        StopWatch sw1 = new StopWatch();
        sw1.start("CreateEvents");
        String projectId = "Test";

        for (Integer i = 0; i < 999; i++) {
            String name = projectId + i.toString();
            createScanSubscription(name, projectId, null);

        }
        createSessionSubscription("SomethingDifferent", projectId, null);
        sw1.stop();
        System.out.print("\n" + Integer.toString(eventService.getSubscriptions().size()) + " Subscriptions created in : " + sw1.getTotalTimeSeconds() + "seconds\n");

        XnatImagesessiondataI session = new XnatImagesessiondata(mockUser);
        session.setModality("MR");
        session.setProject(projectId);
        session.setSessionType("xnat:imageSessionData");
        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId));

        StopWatch sw2 = new StopWatch();
        sw2.start("eventTriggerToAction");
        synchronized (testAction) {
            testAction.wait(100);
        }
        sw2.stop();
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
        System.out.print("Event caught in : " + sw2.getTotalTimeSeconds() + "seconds\n");

    }

    //@Ignore("Fails with error message: Expected two detected events. Expected: is <2> but: was <1>")
    @Test
    @DirtiesContext
    public void create1000SubscriptionsCatchTwoWithDifferentProjectId() throws Exception {
        StopWatch sw1 = new StopWatch();
        sw1.start("CreateSubscriptions");
        String projectId = "Test";

        for (Integer i = 0; i < 1000; i++) {
            String name = projectId + i.toString();
            createSessionSubscription(name, name, null);

        }
        sw1.stop();
        System.out.print("\n" + Integer.toString(eventService.getSubscriptions().size()) + " Subscriptions created in : " + sw1.getTotalTimeSeconds() + "seconds\n");

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId);
        session.setSessionType("xnat:imageSessionData");
        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId + "500"));

        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId + "600"));

        StopWatch sw2 = new StopWatch();
        sw2.start("eventTriggerToAction");
        synchronized (testAction) {
            testAction.wait(100);
        }
        sw2.stop();
        TestAction action = (TestAction) testAction;
        assertThat("Expected two detected events.", action.getDetectedEvents().size(), is(2));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(1).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
        System.out.print("Two event caught in : " + sw2.getTotalTimeSeconds() + "seconds\n");

    }

    //@Ignore("Fails with error message: Expected 100 detected events. Expected: is <1000> but: was <1>")
    @Test
    @DirtiesContext
    public void createManySubscriptionsTriggerManyEventsCatch1000() throws Exception {
        String               projectIdToIgnore = "ProjectIdToIgnore";
        XnatImagesessiondata sessionToIgnore   = new XnatImagesessiondata();
        sessionToIgnore.setModality("MR");
        sessionToIgnore.setProject(projectIdToIgnore);
        sessionToIgnore.setSessionType("xnat:imageSessionData");

        String               projectIdToCatch = "ProjectIdToCatch";
        XnatImagesessiondata sessionToCatch   = new XnatImagesessiondata();
        sessionToCatch.setModality("MR");
        sessionToCatch.setProject(projectIdToCatch);
        sessionToCatch.setSessionType("xnat:imageSessionData");

        StopWatch sw1 = new StopWatch();
        sw1.start("Subscriptions");
        String projectId = "Test";

        for (Integer i = 0; i < 1000; i++) {
            String name = projectId + i.toString();
            assertThat(createSessionSubscription(name + "_1", projectId + "_1", null), notNullValue());
            assertThat(createSessionSubscription(name + "_2", projectId + "_2", null), notNullValue());
            assertThat(createScanSubscription(name + "_3", projectId + "_3", null), notNullValue());
            assertThat(createScanSubscription(name + "_4", projectId + "_4", null), notNullValue());
        }
        for (Integer i = 0; i < 10; i++) {
            String name = projectIdToCatch + i.toString();
            assertThat(createSessionSubscription(name, projectIdToCatch, null), notNullValue());
        }
        sw1.stop();
        System.out.print("\n" + Integer.toString(eventService.getSubscriptions().size()) + " Subscriptions created in : " + sw1.getTotalTimeSeconds() + "seconds\n");

        StopWatch sw2 = new StopWatch();
        sw2.start("eventTriggersToActions");
        for (Integer i = 0; i < 10000; i++) {
            eventService.triggerEvent(new SessionEvent(sessionToIgnore, mockUser.getLogin(), SessionEvent.Status.CREATED, null));
        }
        sw2.stop();
        System.out.print("Triggered 10000 ignored events in : " + sw2.getTotalTimeSeconds() + "seconds\n");

        StopWatch sw3 = new StopWatch();
        sw3.start("eventTriggersToActions");
        for (Integer i = 0; i < 100; i++) {
            eventService.triggerEvent(new SessionEvent(sessionToCatch, mockUser.getLogin(), SessionEvent.Status.CREATED, projectIdToCatch));
        }
        synchronized (testAction) {
            testAction.wait(100);
        }
        sw3.stop();
        TestAction action = (TestAction) testAction;
        System.out.print("Triggered/Caught " + Integer.toString(action.getDetectedEvents().size()) + " detected events in : " + sw3.getTotalTimeSeconds() + "seconds\n");

        List<EventServiceEvent> detectedEvents = action.getDetectedEvents();
        assertThat("Expected 100 detected events.", action.getDetectedEvents().size(), is(1000));

    }

    @Test
    @DirtiesContext
    public void testDisabledSubscriptionHandlingSpeed() throws Exception {
        String               projectIdToCatch = "ProjectIdToCatch";
        XnatImagesessiondata sessionToCatch   = new XnatImagesessiondata();
        sessionToCatch.setModality("MR");
        sessionToCatch.setProject(projectIdToCatch);
        sessionToCatch.setSessionType("xnat:imageSessionData");

        StopWatch sw1 = new StopWatch();
        sw1.start("Subscriptions");

        Integer i;
        for (i = 0; i < 1000; i++) {
            String name = projectIdToCatch + i.toString();
            Long   id   = createSessionSubscription(name + "_1", projectIdToCatch, null).id();
            eventService.deactivateSubscription(id);
        }
        createSessionSubscription(projectIdToCatch, projectIdToCatch, null);

        // time reaction to 1000 disabled subscriptions and 1 enabled
        StopWatch sw3 = new StopWatch();
        sw3.start("disabledEventTriggersToActions");
        eventService.triggerEvent(new SessionEvent(sessionToCatch, mockUser.getLogin(), SessionEvent.Status.CREATED, projectIdToCatch));

        synchronized (testAction) {
            testAction.wait(100);
        }
        sw3.stop();
        TestAction action = (TestAction) testAction;
        System.out.print("Triggered " + Integer.toString(action.getDetectedEvents().size()) + " enabled event and " + i.toString() + " disabled events  in : " + sw3.getTotalTimeSeconds() + "seconds\n");


    }

    @Ignore("Fails with error message: null id in org.nrg.xnat.eventservice.entities.TimedEventStatusEntity entry (don't flush the Session after an exception occurs)")
    @Test
    @DirtiesContext
    public void testSubscriptionDeliveryCreation() throws Exception {
        String               projectIdToCatch = "ProjectIdToCatch";
        XnatImagesessiondata sessionToCatch   = new XnatImagesessiondata();
        sessionToCatch.setModality("MR");
        sessionToCatch.setProject(projectIdToCatch);
        sessionToCatch.setSessionType("xnat:mrSessionData");

        String               projectIdToIgnore = "ProjectIdToIgnore";
        XnatImagesessiondata sessionToIgnore   = new XnatImagesessiondata();
        sessionToCatch.setModality("MR");
        sessionToCatch.setProject(projectIdToIgnore);
        sessionToCatch.setSessionType("xnat:mrSessionData");

        assertThat(createSessionSubscription("SubscriptionOfInterest", projectIdToCatch, null), notNullValue());
        StopWatch sw3 = new StopWatch();
        sw3.start("eventTriggersToActions");
        for (Integer i = 0; i < 10; i++) {
            eventService.triggerEvent(new SessionEvent(sessionToCatch, mockUser.getLogin(), SessionEvent.Status.CREATED, projectIdToCatch));
        }
        synchronized (testAction) {
            testAction.wait(100);
        }
        sw3.stop();

        List<SubscriptionDelivery> deliveriesWithProjectId = eventService.getSubscriptionDeliveries(projectIdToCatch, null, false);
        assertThat("Expected 10 deliveries.", deliveriesWithProjectId.size(), is(10));

        List<SubscriptionDelivery> deliveriesWithSubscriptionId = eventService.getSubscriptionDeliveries(null, 1L, true);
        assertThat("Expected 10 deliveries.", deliveriesWithSubscriptionId.size(), is(10));

        List<SubscriptionDelivery> deliveriesWithSubscriptionIdAndProjectId = eventService.getSubscriptionDeliveries(projectIdToCatch, 1l, false);
        assertThat("Expected 10 deliveries.", deliveriesWithSubscriptionIdAndProjectId.size(), is(10));

        List<SubscriptionDelivery> deliveries = eventService.getSubscriptionDeliveries(null, null, true);
        assertThat("Expected 10 deliveries.", deliveries.size(), is(10));

        // Add some other things to the history table
        assertThat(createSessionSubscription("SubscriptionToIgnore", projectIdToIgnore, null), notNullValue());
        for (Integer i = 0; i < 10; i++) {
            eventService.triggerEvent(new SessionEvent(sessionToIgnore, mockUser.getLogin(), SessionEvent.Status.CREATED, projectIdToIgnore));
        }
        synchronized (testAction) {
            testAction.wait(100);
        }

        deliveriesWithProjectId = eventService.getSubscriptionDeliveries(projectIdToCatch, null, false);
        assertThat("Expected 10 deliveries.", deliveriesWithProjectId.size(), is(10));
        Integer deliveriesWithProjectIdCount = eventService.getSubscriptionDeliveriesCount(projectIdToCatch, null, false);
        assertThat("Expected 10 deliveries counted.", deliveriesWithProjectIdCount, is(10));


        deliveriesWithSubscriptionId = eventService.getSubscriptionDeliveries(null, 1L, true);
        assertThat("Expected 10 deliveries.", deliveriesWithSubscriptionId.size(), is(10));
        Integer deliveriesWithSubscriptionIdCount = eventService.getSubscriptionDeliveriesCount(null, 1L, true);
        assertThat("Expected 10 deliveries counted.", deliveriesWithSubscriptionIdCount, is(10));

        deliveriesWithSubscriptionIdAndProjectId = eventService.getSubscriptionDeliveries(projectIdToCatch, 1l, false);
        assertThat("Expected 10 deliveries.", deliveriesWithSubscriptionIdAndProjectId.size(), is(10));
        Integer deliveriesWithSubscriptionIdAndProjectIdCount = eventService.getSubscriptionDeliveriesCount(projectIdToCatch, 1L, false);
        assertThat("Expected 10 deliveries counted.", deliveriesWithSubscriptionIdAndProjectIdCount, is(10));

        deliveries = eventService.getSubscriptionDeliveries(null, null, true);
        assertThat("Expected 20 deliveries.", deliveries.size(), is(20));
        Integer deliveriesCount = eventService.getSubscriptionDeliveriesCount(null, null, true);
        assertThat("Expected 20 deliveries counted.", deliveriesCount, is(20));

    }

    @Test
    @DirtiesContext
    public void testCreateAndDelete() throws Exception {

        String projectId = "ProjectId";


        List<Subscription> subscriptionsToDelete = new ArrayList<>();
        for (Integer i = 0; i < 50; i++) {
            String name = projectId + i.toString();
            subscriptionsToDelete.add(createSessionSubscription(name, projectId, null));
        }

        Subscription subToCatch = createSessionSubscription("Name to Catch 1", projectId, null);

        for (Integer i = 50; i < 100; i++) {
            String name = projectId + i.toString();
            subscriptionsToDelete.add(createSessionSubscription(name, projectId, null));
        }

        for (Subscription subscription : subscriptionsToDelete) {
            eventService.deleteSubscription(subscription.id());
        }

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId);
        session.setSessionType("xnat:mrSessionData");

        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId));

        synchronized (testAction) {
            testAction.wait(100);
        }

        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }


    @Test
    @DirtiesContext
    public void testCreateAndDeactivate() throws Exception {

        String projectId = "ProjectId";

        List<Subscription> subscriptionsToDeactivate = new ArrayList<>();
        for (Integer i = 0; i < 10; i++) {
            String name = projectId + i.toString();
            subscriptionsToDeactivate.add(createSessionSubscription(name, projectId, null));
        }

        Subscription subToCatch = createSessionSubscription("Name to Catch 1", projectId, null);

        for (Integer i = 20; i < 30; i++) {
            String name = projectId + i.toString();
            subscriptionsToDeactivate.add(createSessionSubscription(name, projectId, null));
        }

        for (Subscription subscription : subscriptionsToDeactivate) {
            eventService.deactivateSubscription(subscription.id());
        }

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId);
        session.setSessionType("xnat:mrSessionData");

        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId));

        synchronized (testAction) {
            testAction.wait(100);
        }

        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }

    @Test
    @DirtiesContext
    public void testCreateAndDeactivateAndActivate() throws Exception {

        String projectId = "ProjectId";

        List<Subscription> subscriptionsToDeactivate = new ArrayList<>();
        for (Integer i = 0; i < 5; i++) {
            String name = projectId + i.toString();
            subscriptionsToDeactivate.add(createSessionSubscription(name, projectId, null));
        }

        Subscription subscriptionToCatch = createSessionSubscription("Name to Catch 1", projectId, null);
        subscriptionsToDeactivate.add(subscriptionToCatch);

        for (Integer i = 6; i < 10; i++) {
            String name = projectId + i.toString();
            subscriptionsToDeactivate.add(createSessionSubscription(name, projectId, null));
        }

        for (Subscription subscription : subscriptionsToDeactivate) {
            eventService.deactivateSubscription(subscription.id());
        }

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId);
        session.setSessionType("xnat:mrSessionData");

        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId));

        synchronized (testAction) {
            testAction.wait(100);
        }

        TestAction action = (TestAction) testAction;
        assertThat("Expected zero detected events.", action.getDetectedEvents().size(), is(0));

        eventService.activateSubscription(subscriptionToCatch.id());

        eventService.triggerEvent(new SessionEvent(session, mockUser.getLogin(), SessionEvent.Status.CREATED, projectId));

        synchronized (testAction) {
            testAction.wait(100);
        }

        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type SessionEvent", action.getDetectedEvents().get(0).getType(), containsString("SessionEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }


    @Test
    @DirtiesContext
    public void catchSpecificEventWithOpenFilter() throws Exception {
        String projectId1 = "PROJECTID_1";

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId1);
        session.setSessionType("xnat:mrSessionData");

        XnatImagescandata scan = new XnatImagescandata();
        scan.setImageSessionData(session);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        EventFilterCreator filterCreator = EventFilterCreator.builder().eventType(new ScanEvent().getType()).build();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name("Test Subscription")
                                                                     .active(true)
                                                                     .eventFilter(filterCreator)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger ScanArchiveEvent
        eventService.triggerEvent(new ScanEvent(scan, mockUser.getLogin(), ScanEvent.Status.CREATED, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected one detected event.", action.getDetectedEvents().size(), is(1));
        assertThat("Expected detected event to be of type ScanEvent", action.getDetectedEvents().get(0).getType(), containsString("ScanEvent"));
        assertThat("Expected Action User to be subscription creator", action.getActionUser(), is(mockUser.getLogin()));
    }


    @Test
    @DirtiesContext
    public void missOpenEventWithSpecificSubscription() throws Exception {
        String projectId1 = "PROJECTID_1";
        String eventType  = new ScanEvent().getType();
        Enum   status     = ScanEvent.Status.CREATED;

        XnatImagesessiondata session = new XnatImagesessiondata();
        session.setModality("MR");
        session.setProject(projectId1);
        session.setSessionType("xnat:mrSessionData");

        XnatImagescandata scan = new XnatImagescandata();
        scan.setImageSessionData(session);

        // Subscribe to event
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        ProjectEventFilterCreator filter = ProjectEventFilterCreator.builder().projectIds(Arrays.asList(projectId1)).eventType(eventType).status(status.name()).build();
        ProjectSubscriptionCreator subscriptionCreator = ProjectSubscriptionCreator.builder()
                                                                                   .name("Test Subscription")
                                                                                   .eventFilter(filter)
                                                                                   .active(true)
                                                                                   .actionKey(testActionKey)
                                                                                   .build();
        Subscription subscription = Subscription.createOnProject(subscriptionCreator, mockUser.getLogin());
        assertThat("Subscription failed validation.", eventService.validateSubscription(subscription), notNullValue());
        assertThat("Subscription failed creation.", eventService.createSubscription(subscription), notNullValue());

        // Trigger ScanArchiveEvent
        eventService.triggerEvent(new ScanEvent(scan, mockUser.getLogin(), null, projectId1));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        TestAction action = (TestAction) testAction;
        assertThat("Expected zero detected events.", action.getDetectedEvents().size(), is(0));


        // Trigger ScanArchiveEvent
        eventService.triggerEvent(new ScanEvent(scan, mockUser.getLogin(), ScanEvent.Status.CREATED, null));

        // wait for listener (max 1 sec.)
        synchronized (testAction) {
            testAction.wait(1000);
        }
        action = (TestAction) testAction;
        assertThat("Expected zero detected events.", action.getDetectedEvents().size(), is(0));
    }


    public Subscription createSessionSubscription(String name, String projectId, EventFilterCreator filter) throws Exception {
        String eventType = new SessionEvent().getType();
        Enum   status    = SessionEvent.Status.CREATED;
        if (filter == null) {
            filter = EventFilterCreator.builder().projectIds(Arrays.asList(projectId)).eventType(eventType).status(status.name()).build();
        }

        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name(name)
                                                                     .active(true)
                                                                     .eventFilter(filter)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        subscription = eventService.validateSubscription(subscription);
        return eventService.createSubscription(subscription);
    }

    public Subscription createScanSubscription(String name, String projectId, EventFilterCreator filter) throws Exception {
        String eventType = new ScanEvent().getType();
        Enum   status    = ScanEvent.Status.CREATED;
        if (filter == null) {
            filter = EventFilterCreator.builder().projectIds(Arrays.asList(projectId)).eventType(eventType).status(status.name()).build();
        }
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name(name)
                                                                     .active(true)
                                                                     .eventFilter(filter)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();

        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        subscription = eventService.validateSubscription(subscription);
        return eventService.createSubscription(subscription);
    }

    public Subscription createProjectCreatedSubscription(String name, String projectId, EventFilterCreator filter) throws Exception {
        String eventType = new ProjectEvent().getType();
        Enum   status    = ProjectEvent.Status.CREATED;
        if (filter == null) {
            filter = EventFilterCreator.builder().projectIds(Arrays.asList(projectId)).eventType(eventType).status(status.name()).build();
        }
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name(name)
                                                                     .active(true)
                                                                     .eventFilter(filter)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        subscription = eventService.validateSubscription(subscription);
        return eventService.createSubscription(subscription);
    }

    public Subscription createSubjectCreatedSubscription(String name, String projectId, EventFilterCreator filter) throws Exception {
        String eventType = new SubjectEvent().getType();
        Enum   status    = SubjectEvent.Status.CREATED;
        if (filter == null) {
            filter = EventFilterCreator.builder().projectIds(Arrays.asList(projectId)).eventType(eventType).status(status.name()).build();
        }
        String testActionKey = testAction.getAllActions().get(0).actionKey();
        eventService.getAllActions();
        SubscriptionCreator subscriptionCreator = SubscriptionCreator.builder()
                                                                     .name(name)
                                                                     .active(true)
                                                                     .eventFilter(filter)
                                                                     .actionKey(testActionKey)
                                                                     .actAsEventUser(false)
                                                                     .build();
        Subscription subscription = Subscription.create(subscriptionCreator, mockUser.getLogin());
        subscription = eventService.validateSubscription(subscription);
        return eventService.createSubscription(subscription);
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
        public void processEvent(EventServiceEvent event, Subscription subscription, UserI user,
                                 Long deliveryId) {

        }

        @Override
        public Map<String, ActionAttributeConfiguration> getAttributes(String projectId, UserI user) {
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
        public String getType() {
            return this.getClass().getCanonicalName();
        }

        @Override
        public String getEventType() {
            return SampleEvent.class.getName();
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
        public void setEventService(EventService eventService) {
        }

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