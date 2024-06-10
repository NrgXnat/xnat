package org.nrg.xnat.compute.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.DefaultJobTemplateServiceTestConfig;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultJobTemplateServiceTestConfig.class)
public class DefaultJobTemplateServiceTest {

    @Autowired private DefaultJobTemplateService jobTemplateService;
    @Autowired private ComputeEnvironmentConfigService mockComputeEnvironmentConfigService;
    @Autowired private HardwareConfigService mockHardwareConfigService;
    @Autowired private ConstraintConfigService mockConstraintConfigService;

    @Before
    public void before() {
    }

    @After
    public void after() {
        Mockito.reset(
                mockComputeEnvironmentConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService
        );
    }

    @Test
    public void testIsAvailable_ComputeEnvironmentNotAvailable() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(false);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(true);

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Site, "site");
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");
        executionScope.put(Scope.DataType, "xnat:petSessionData");
        executionScope.put(Scope.Experiment, "XNAT_E00001");

        // Run
        boolean isAvailable = jobTemplateService.isAvailable(1L, 1L, executionScope);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_HardwareNotAvailable() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(false);

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");

        // Run
        boolean isAvailable = jobTemplateService.isAvailable(1L, 1L, executionScope);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeEnvironmentAndHardwareNotAvailable() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(false);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(false);

        // Run
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");
        boolean isAvailable = jobTemplateService.isAvailable(1L, 1L, executionScope);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeEnvironmentConfigAllHardwareIsAvailable() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(true);

        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().build();
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(true);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");

        // Run
        boolean isAvailable = jobTemplateService.isAvailable(1L, 1L, executionScope);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeEnvironmentConfigSpecificHardwareAllowed() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(true);

        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().id(1L).build();
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));
        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");
        executionScope.put(Scope.DataType, "xnat:petSessionData");
        executionScope.put(Scope.Experiment, "XNAT_E00001");

        // Run
        boolean isAvailable = jobTemplateService.isAvailable(1L, 1L, executionScope);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeEnvironmentConfigSpecificHardwareNotAllowed() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(true);

        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().id(1L).build();
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));
        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");
        executionScope.put(Scope.DataType, "xnat:petSessionData");
        executionScope.put(Scope.Experiment, "XNAT_E00002");

        // Run
        boolean isAvailable = jobTemplateService.isAvailable(1L, 2L, executionScope);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testResolve() {
        // Setup
        when(mockComputeEnvironmentConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any())).thenReturn(true);

        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().id(1L).build();
        ComputeEnvironment computeEnvironment = ComputeEnvironment.builder()
                .name("JupyterHub Data Science Notebook")
                .image("jupyter/datascience-notebook:latest")
                .build();
        computeEnvironmentConfig.setComputeEnvironment(computeEnvironment);
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        Hardware hardware = Hardware.builder()
                .name("Standard")
                .cpuLimit(1.0)
                .memoryLimit("4G")
                .build();
        hardwareConfig.setHardware(hardware);
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));

        Constraint constraint = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("worker")))
                .build();

        ConstraintConfig constraintConfig = ConstraintConfig.builder()
                .id(1L)
                .constraint(constraint)
                .build();

        when(mockComputeEnvironmentConfigService.retrieve(any())).thenReturn(Optional.of(computeEnvironmentConfig));
        when(mockHardwareConfigService.retrieve(any())).thenReturn(Optional.of(hardwareConfig));
        when(mockConstraintConfigService.getAvailable(any())).thenReturn(Collections.singletonList(constraintConfig));

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, "project");
        executionScope.put(Scope.User, "user");

        // Run
        JobTemplate jobTemplate = jobTemplateService.resolve(1L, 1L, executionScope);

        // Verify
        assertNotNull(jobTemplate);
        assertEquals(computeEnvironmentConfig.getComputeEnvironment(), jobTemplate.getComputeEnvironment());
        assertEquals(hardware, jobTemplate.getHardware());
        assertEquals(Collections.singletonList(constraint), jobTemplate.getConstraints());
    }

}