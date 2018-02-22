package org.nrg.xnat.eventservice.rest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.config.EventServiceRestApiTestConfig;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventSubscriptionEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@ContextConfiguration(classes = EventServiceRestApiTestConfig.class)
public class EventServiceRestApiTest {


    private MockMvc mockMvc;
    private MockMvc mockSecMvc;

    private final MediaType JSON = MediaType.APPLICATION_JSON_UTF8;
    private final MediaType XML = MediaType.APPLICATION_XML;

    private final static String ADMIN_USERNAME = "admin";
    private final static String NON_ADMIN_USERNAME = "non-admin";
    private Authentication ADMIN_AUTH;
    private Authentication NONADMIN_AUTH;

    @Autowired private WebApplicationContext wac;
    @Autowired private EventService eventService;
    @Autowired private EventSubscriptionEntityService eventSubscriptionEntityService;
    @Autowired private ContextService contextService;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementServiceI;

    private String siteSubscription =
        "{\n" +
            "\"name\": \"Site Test\",\n" +
            "\"project-id\": \"\",\n" +
            "\"active\": true,\n" +
            "\"event-id\": \"org.nrg.xnat.eventservice.events.TestCombinedEvent\",\n" +
            "\"action-key\": \"org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction\",\n" +
            "\"act-as-event-user\": true\n" +
        "},"
            ;

    private String projectSubscription =
        "{\n" +
            "\"name\": \"Project Test\",\n" +
            "\"project-id\": \"SampleProject\",\n" +
            "\"active\": true,\n" +
            "\"event-id\": \"org.nrg.xnat.eventservice.events.TestCombinedEvent\",\n" +
            "\"action-key\": \"org.nrg.xnat.eventservice.actions.TestAction:org.nrg.xnat.eventservice.actions.TestAction\",\n" +
        "}"
            ;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        final String adminPassword = "admin";
        final UserI admin = mock(UserI.class);
        when(admin.getLogin()).thenReturn(ADMIN_USERNAME);
        when(admin.getPassword()).thenReturn(adminPassword);
        when(mockRoleService.isSiteAdmin(admin)).thenReturn(true);
        when(mockUserManagementServiceI.getUser(ADMIN_USERNAME)).thenReturn(admin);
        ADMIN_AUTH = new TestingAuthenticationToken(admin, adminPassword);

        final String nonAdminPassword = "non-admin-pass";
        final UserI nonAdmin = mock(UserI.class);
        when(nonAdmin.getLogin()).thenReturn(NON_ADMIN_USERNAME);
        when(nonAdmin.getPassword()).thenReturn(nonAdminPassword);
        when(mockRoleService.isSiteAdmin(nonAdmin)).thenReturn(false);
        when(mockUserManagementServiceI.getUser(NON_ADMIN_USERNAME)).thenReturn(nonAdmin);
        NONADMIN_AUTH = new TestingAuthenticationToken(nonAdmin, nonAdminPassword);
    }

    @Test
    public void getInstalledEvents() throws Exception {
        final String path = "/events/events";

        final MockHttpServletRequestBuilder request =
                get(path).with(authentication(ADMIN_AUTH))
                         .with(csrf())
                         .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                       .andExpect(status().isOk())
                       .andReturn()
                       .getResponse()
                       .getContentAsString();
        assertThat(response, not(nullValue()));
    }


    @Test
    @Ignore
    @DirtiesContext
    public void createSubscription() throws Exception {
        final String path = "/events/subscription";

        final MockHttpServletRequestBuilder request =
                post(path)
                        .content(siteSubscription)
                        .contentType(JSON)
                        .accept(XML)
                        .with(authentication(ADMIN_AUTH))
                        .with(csrf())
                        .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                       .andExpect(status().isCreated())
                       .andReturn()
                       .getResponse()
                       .getContentAsString();

        assertThat(response, is(notNullValue()));

    }

    @Test
    public void createProjectSubscription() throws Exception {

    }


    @Test
    public void getAllSubscriptions() throws Exception {

    }

    @Test
    public void retrieveSubscription() throws Exception {

    }

    @Test
    public void getInstalledListeners() throws Exception {

    }

    @Test
    public void getInstalledActions() throws Exception {

    }

}