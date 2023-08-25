package org.nrg.xnat.compute.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.compute.config.ComputeEnvironmentConfigsApiConfig;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ComputeEnvironmentConfigsApiConfig.class})
public class ComputeEnvironmentConfigsApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private ComputeEnvironmentConfigService mockComputeEnvironmentConfigService;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private ComputeEnvironmentConfig computeEnvironmentConfig;

    @Before
    public void before() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Mock the user
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");
        when(mockUser.getEmail()).thenReturn("mockUser@mockuser.com");
        when(mockUser.getPassword()).thenReturn("mockUserPassword");
        when(mockUser.getID()).thenReturn(1);
        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(true);
        mockAuthentication = new TestingAuthenticationToken(mockUser, mockUser.getPassword());

        // Setup the compute environment config
        computeEnvironmentConfig = new ComputeEnvironmentConfig();
        computeEnvironmentConfig.setId(1L);
    }

    @After
    public void after() throws Exception {
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockComputeEnvironmentConfigService,
                mockUser
        );
    }

    @Test
    public void testGetAllComputeEnvironmentConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/environments")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).getAll();
        verify(mockComputeEnvironmentConfigService, never()).getByType(any());
    }

    @Test
    public void testGetComputeEnvironmentConfigsByType() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/environments")
                .param("type", "JUPYTERHUB")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, never()).getAll();
        verify(mockComputeEnvironmentConfigService, times(1)).getByType(any());
    }

    @Test
    public void testGetComputeEnvironmentConfigById() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/environments/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).retrieve(any());
    }

    @Test
    public void testGetComputeEnvironmentConfigByIdNotFound() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/environments/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.empty());

        mockMvc.perform(request).andExpect(status().isNotFound());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).retrieve(any());
    }

    @Test
    public void testCreateComputeEnvironmentConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/compute/environments")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ComputeEnvironmentConfig()))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isCreated());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).create(any());
    }

    @Test
    public void testUpdateComputeEnvironmentConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/environments/1")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(computeEnvironmentConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).update(eq(computeEnvironmentConfig));
    }

    @Test(expected = NestedServletException.class)
    public void testUpdateComputeEnvironmentConfig_IdMismatch() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/environments/2")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(computeEnvironmentConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        // Throws NestedServletException in response to IllegalArgumentException
        mockMvc.perform(request).andExpect(status().isBadRequest());

        // Verify that the service was not called
        verify(mockComputeEnvironmentConfigService, never()).update(any());
    }

    @Test
    public void testDeleteComputeEnvironmentConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/compute/environments/1")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify that the service was called
        verify(mockComputeEnvironmentConfigService, times(1)).delete(eq(1L));
    }

    @Test
    public void testGetAvailableComputeEnvironmentConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/environments/available")
                .param("user", mockUser.getLogin())
                .param("prj", "projectId")
                .param("datatype", "xnat:mrSessionData")
                .param("type", "JUPYTERHUB")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        Map<Scope, String> expectedScopeMap = new HashMap<>();
        expectedScopeMap.put(Scope.User, mockUser.getLogin());
        expectedScopeMap.put(Scope.Project, "projectId");
        expectedScopeMap.put(Scope.DataType, "xnat:mrSessionData");
        verify(mockComputeEnvironmentConfigService, times(1)).getAvailable(eq(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB), eq(expectedScopeMap));
    }

}