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
import org.nrg.xnat.compute.config.HardwareConfigsApiConfig;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnat.compute.services.HardwareConfigService;
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
@ContextConfiguration(classes = {HardwareConfigsApiConfig.class})
public class HardwareConfigsApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private HardwareConfigService mockHardwareConfigService;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private HardwareConfig hardwareConfig;

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

        // Set up a hardware config
        hardwareConfig = new HardwareConfig();
        hardwareConfig.setId(1L);
    }

    @After
    public void after() throws Exception {
        // Reset the mock
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockHardwareConfigService,
                mockUser
        );
    }

    @Test
    public void testGetAllHardwareConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/hardware")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).retrieveAll();
    }

    @Test
    public void testGetHardwareConfigById() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/hardware/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockHardwareConfigService.retrieve(1L)).thenReturn(Optional.of(hardwareConfig));

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).retrieve(1L);
    }

    @Test
    public void testGetHardwareConfigByIdNotFound() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/compute/hardware/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockHardwareConfigService.retrieve(1L)).thenReturn(Optional.empty());

        mockMvc.perform(request).andExpect(status().isNotFound());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).retrieve(1L);
    }

    @Test
    public void testCreateHardwareConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/compute/hardware")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(hardwareConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockHardwareConfigService.create(hardwareConfig)).thenReturn(hardwareConfig);

        mockMvc.perform(request).andExpect(status().isCreated());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).create(hardwareConfig);
    }

    @Test
    public void testUpdateHardwareConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/hardware/1")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(hardwareConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockHardwareConfigService.update(hardwareConfig)).thenReturn(hardwareConfig);

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).update(hardwareConfig);
    }

    @Test(expected = NestedServletException.class)
    public void testUpdateHardwareConfig_IdMismatch() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/compute/hardware/2")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(hardwareConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isBadRequest());

        // Verify that the service was not called
        verify(mockHardwareConfigService, never()).update(hardwareConfig);
    }

    @Test
    public void testDeleteHardwareConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/compute/hardware/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify that the service was called
        verify(mockHardwareConfigService, times(1)).delete(1L);
    }

}