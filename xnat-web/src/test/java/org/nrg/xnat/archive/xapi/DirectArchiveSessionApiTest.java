package org.nrg.xnat.archive.xapi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Status;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.nrg.action.ClientException;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * XNAT-7944: the delete endpoint must answer with the status the service put on its {@link ClientException} (409
 * while the session is receiving, being archived or otherwise not claimable), and pass the force flag through.
 * The shared XAPI advice only honours a status carried by an exception annotation, so the controller maps it.
 */
public class DirectArchiveSessionApiTest {
    private static final long   SESSION_ID = 42L;
    private static final String PROJECT    = "PROJ";
    private static final String TAG        = "1.2.3";
    private static final String NAME       = "SESSION_1";

    private DirectArchiveSessionService service;
    private PermissionsServiceI         permissions;
    private UserI                       user;
    private SessionData                 session;
    private MockMvc                     mockMvc;

    @Before
    public void setUp() throws Exception {
        service     = mock(DirectArchiveSessionService.class);
        permissions = mock(PermissionsServiceI.class);
        user        = mock(UserI.class);
        session     = new SessionData().setProject(PROJECT).setTag(TAG).setName(NAME);
        when(permissions.getUserEditableProjects(user)).thenReturn(List.of(PROJECT));
        when(service.findByProjectTagName(PROJECT, TAG, NAME)).thenReturn(session);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(user, "secret"));
        mockMvc = MockMvcBuilders.standaloneSetup(new DirectArchiveSessionApi(service, mock(UserManagementServiceI.class),
                                                                              permissions, mock(RoleHolder.class))).build();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void aDeleteTheServiceRefusesAnswersWithTheServiceStatus() throws Exception {
        doThrow(new ClientException(Status.CLIENT_ERROR_CONFLICT, "still receiving files")).when(service).delete(SESSION_ID, user, false);

        mockMvc.perform(delete("/direct-archive/{id}", SESSION_ID))
               .andExpect(status().isConflict())
               .andExpect(content().string(containsString("still receiving files")));
    }

    @Test
    public void forceIsPassedThroughToTheService() throws Exception {
        mockMvc.perform(delete("/direct-archive/{id}", SESSION_ID).param("force", "true"))
               .andExpect(status().isOk());

        verify(service).delete(SESSION_ID, user, true);
    }

    @Test
    public void deleteDefaultsToNotForced() throws Exception {
        mockMvc.perform(delete("/direct-archive/{id}", SESSION_ID))
               .andExpect(status().isOk());

        verify(service).delete(SESSION_ID, user, false);
    }

    @Test
    public void triggerForceIsPassedThroughToTheService() throws Exception {
        mockMvc.perform(post("/direct-archive/{project}/{tag}/{name}", PROJECT, TAG, NAME).param("force", "true"))
               .andExpect(status().isOk());

        verify(service).triggerArchive(session, user, true);
    }

    @Test
    public void triggerDefaultsToNotForced() throws Exception {
        mockMvc.perform(post("/direct-archive/{project}/{tag}/{name}", PROJECT, TAG, NAME))
               .andExpect(status().isOk());

        verify(service).triggerArchive(session, user, false);
    }

    @Test
    public void aTriggerTheServiceRefusesAnswersWithTheServiceStatus() throws Exception {
        doThrow(new ClientException(Status.CLIENT_ERROR_CONFLICT, "not in a status that can be queued")).when(service).triggerArchive(session, user, false);

        mockMvc.perform(post("/direct-archive/{project}/{tag}/{name}", PROJECT, TAG, NAME))
               .andExpect(status().isConflict())
               .andExpect(content().string(containsString("not in a status that can be queued")));
    }
}
