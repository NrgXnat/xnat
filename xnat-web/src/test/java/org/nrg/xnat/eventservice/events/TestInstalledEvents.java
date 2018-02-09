package org.nrg.xnat.eventservice.events;

import org.hamcrest.text.IsEmptyString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.config.EventServiceTestConfig;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class TestInstalledEvents {
    private static final Logger log = LoggerFactory.getLogger(TestInstalledEvents.class);

    private static final String EVENT_RESOURCE_PATTERN ="classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";
    private UserI mockUser;
    private final String FAKE_USER = "mockUser";
    private final Integer FAKE_USER_ID = 1234;

    @Autowired private ContextService contextService;
    @Autowired @Lazy private EventService eventService;
    @Autowired private EventServiceComponentManager componentManager;


    @Before
    public void setUp() throws Exception {

        // Mock the userI
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn(FAKE_USER);
        when(mockUser.getID()).thenReturn(FAKE_USER_ID);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void checkContext() throws Exception {
        assertThat(contextService.getBean("eventService"), not(nullValue()));
    }

    @Test
    public void validateInstalledEvents() throws Exception {
        List<EventServiceEvent> events = componentManager.getInstalledEvents();
        Integer eventPropertyFileCount = BasicXnatResourceLocator.getResources(EVENT_RESOURCE_PATTERN).size();
        assert(events != null && events.size() == eventPropertyFileCount);

        for(EventServiceEvent event : events){
            assertThat("Null or empty event ID in " + event.getClass().getName(), event.getId(), not(IsEmptyString.isEmptyOrNullString()));
            assertThat("Null or empty event Display Name in " + event.getClass().getName(), event.getDisplayName(), not(IsEmptyString.isEmptyOrNullString()));
            assertThat("Null IsPayloadXsiType in " + event.getClass().getName(), event.isPayloadXsiType(), not(nullValue()));
            if(event.isPayloadXsiType()) {
                assertThat("Null or Empty Payload XNAT Type in" + event.getClass().getName(), event.getPayloadXnatType(), not(IsEmptyString.isEmptyOrNullString()));
            }
            assertThat("EventUser should be null until event is triggered in " + event.getClass().getName(), event.getUser(), nullValue());
            assertThat("Null EventTimestamp. Should be assigned at object creation for " + event.getClass().getName(), event.getEventTimestamp(), not(nullValue()));
            assertThat("Null EventUUID. Should be assigned at object creation for " + event.getClass().getName(), event.getEventUUID(), not(nullValue()));


        }
    }
}
