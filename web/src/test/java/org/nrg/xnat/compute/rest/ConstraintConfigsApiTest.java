package org.nrg.xnat.compute.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.compute.config.ConstraintConfigsApiTestConfig;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConstraintConfigsApiTestConfig.class})
public class ConstraintConfigsApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private ConstraintConfigService mockConstraintConfigService;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private ConstraintConfig constraintConfig;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Mock the user
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");
        when(mockUser.getEmail()).thenReturn("mockUser@mockuser.com");
        when(mockUser.getPassword()).thenReturn("mockUserPassword");
        when(mockUser.getID()).thenReturn(1);
        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(true);
        mockAuthentication = new TestingAuthenticationToken(mockUser, mockUser.getPassword());

        // Set up a PlacementConstraintConfig
        constraintConfig = new ConstraintConfig();
        constraintConfig.setId(1L);
    }

    @After
    public void after() {
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockConstraintConfigService
        );
    }

    @Test
    public void testGetPlacementConstraintConfig() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/constraints/1")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        // Set up the mocks
        when(mockConstraintConfigService.retrieve(1L)).thenReturn(Optional.of(constraintConfig));

        // Make the call
        mockMvc.perform(request).andExpect(status().isOk());

        // Verify the mocks
        verify(mockConstraintConfigService).retrieve(1L);
    }

    @Test
    public void testGetPlacementConstraintConfigNotFound() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/constraints/1")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        // Set up the mocks
        when(mockConstraintConfigService.retrieve(1L)).thenReturn(Optional.empty());

        // Make the call
        mockMvc.perform(request).andExpect(status().isNotFound());

        // Verify the mocks
        verify(mockConstraintConfigService).retrieve(1L);
    }

    @Test
    public void testGetAllPlacementConstraintConfigs() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/constraints")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());
        // Make the call
        mockMvc.perform(request).andExpect(status().isOk());

        // Verify the mocks
        verify(mockConstraintConfigService).getAll();
    }

    @Test
    public void testCreatePlacementConstraintConfig() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/compute/constraints")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext())
                .content(mapper.writeValueAsString(constraintConfig));

        // Make the call
        mockMvc.perform(request).andExpect(status().isCreated());

        // Verify the mocks
        verify(mockConstraintConfigService).create(constraintConfig);
    }

    @Test
    public void testUpdatePlacementConstraintConfig() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/constraints/1")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext())
                .content(mapper.writeValueAsString(constraintConfig));

        // Make the call
        mockMvc.perform(request).andExpect(status().isOk());

        // Verify the mocks
        verify(mockConstraintConfigService).update(constraintConfig);
    }

    @Test(expected = NestedServletException.class)
    public void testUpdatePlacementConstraintConfig_IdMismatch() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/constraints/2")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext())
                .content(mapper.writeValueAsString(constraintConfig));

        // Make the call
        mockMvc.perform(request).andExpect(status().isOk());

        // Verify the mocks
        verify(mockConstraintConfigService, never()).update(constraintConfig);
    }

    @Test
    public void testDeletePlacementConstraintConfig() throws Exception {
        // Set up the request
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/compute/constraints/1")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        // Make the call
        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify the mocks
        verify(mockConstraintConfigService).delete(1L);
    }

}