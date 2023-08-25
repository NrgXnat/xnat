package org.nrg.xnat.compute.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.config.DefaultComputeEnvironmentConfigServiceTestConfig;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.CONTAINER_SERVICE;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.JUPYTERHUB;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = DefaultComputeEnvironmentConfigServiceTestConfig.class)
public class DefaultComputeEnvironmentConfigServiceTest {

    @Autowired private DefaultComputeEnvironmentConfigService defaultComputeEnvironmentConfigService;
    @Autowired private HardwareConfigEntityService hardwareConfigEntityService;

    private ComputeEnvironmentConfig computeEnvironmentConfig1;
    private ComputeEnvironmentConfig computeEnvironmentConfig2;
    private ComputeEnvironmentConfig computeEnvironmentConfig3;
    private ComputeEnvironmentConfig computeEnvironmentConfigInvalid;

    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;
    private HardwareConfig hardwareConfig3;

    @Before
    public void before() {
        createDummyConfigs();
    }

    @Test
    @DirtiesContext
    public void testExists() {
        // Test
        ComputeEnvironmentConfig created = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        boolean exists = defaultComputeEnvironmentConfigService.exists(created.getId());

        // Verify
        assertTrue(exists);
    }

    @Test
    @DirtiesContext
    public void testDoesNotExist() {
        // Test
        ComputeEnvironmentConfig created = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        boolean exists = defaultComputeEnvironmentConfigService.exists(created.getId() + 1);

        // Verify
        assertFalse(exists);
    }

    @Test
    @DirtiesContext
    public void testGetDoesNotExist() {
        // Test
        Optional<ComputeEnvironmentConfig> computeEnvironmentConfig = defaultComputeEnvironmentConfigService.retrieve(1L);

        // Verify
        assertFalse(computeEnvironmentConfig.isPresent());
    }

    @Test
    @DirtiesContext
    public void testGet() {
        // Test
        ComputeEnvironmentConfig created = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        Optional<ComputeEnvironmentConfig> computeEnvironmentConfig = defaultComputeEnvironmentConfigService.retrieve(created.getId());

        // Verify
        assertTrue(computeEnvironmentConfig.isPresent());
        assertEquals(created, computeEnvironmentConfig.get());

        assertEquals(computeEnvironmentConfig1.getComputeEnvironment(), computeEnvironmentConfig.get().getComputeEnvironment());
        assertEquals(computeEnvironmentConfig1.getConfigTypes(), computeEnvironmentConfig.get().getConfigTypes());
        assertEquals(computeEnvironmentConfig1.getScopes(), computeEnvironmentConfig.get().getScopes());
    }

    @Test
    @DirtiesContext
    public void testGetAll() {
        // Test
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getAll();

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(3));
        assertThat(computeEnvironmentConfigs, hasItems(created1, created2, created3));
    }

    @Test
    @DirtiesContext
    public void testGetByType() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getByType(CONTAINER_SERVICE);

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(2));
        assertThat(computeEnvironmentConfigs, hasItems(created2, created3));

        // Test
        computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getByType(JUPYTERHUB);

        // Verify
        assertEquals(2, computeEnvironmentConfigs.size());
        assertThat(computeEnvironmentConfigs, containsInAnyOrder(created1, created3));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_WrongUser() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User2");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getAvailable(null, executionScope);

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(1));
        assertThat(computeEnvironmentConfigs, hasItems(created1));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_WrongProject() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project2");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getAvailable(executionScope);

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(1));
        assertThat(computeEnvironmentConfigs, hasItem(created1));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable() {
        // Create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        HardwareConfigEntity hardwareConfigEntity3 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig3));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());
        hardwareConfig3.setId(hardwareConfigEntity3.getId());

        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:ctSessionData");
        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getAvailable(executionScope);

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(2));
        assertThat(computeEnvironmentConfigs.get(0).getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(computeEnvironmentConfigs.get(0).getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));
        assertThat(computeEnvironmentConfigs.get(0).getHardwareOptions().getHardwareConfigs(), not(hasItems(hardwareConfig3)));
        assertThat(computeEnvironmentConfigs.get(1).getHardwareOptions().getHardwareConfigs().size(), is(1));
        assertThat(computeEnvironmentConfigs.get(1).getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig2));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_SpecificType() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        List<ComputeEnvironmentConfig> computeEnvironmentConfigs = defaultComputeEnvironmentConfigService.getAvailable(CONTAINER_SERVICE, executionScope);

        // Verify
        assertThat(computeEnvironmentConfigs.size(), is(1));
        assertThat(computeEnvironmentConfigs, hasItems(created2));
    }

    @Test
    @DirtiesContext
    public void testIsAvailable() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        boolean result = defaultComputeEnvironmentConfigService.isAvailable(created1.getId(), executionScope);

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created3.getId(), executionScope);

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testNotAvailable_WrongUser() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User2");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        boolean result = defaultComputeEnvironmentConfigService.isAvailable(created1.getId(), executionScope);

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertFalse(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created3.getId(), executionScope);

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testNotAvailable_WrongProject() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project2");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:petSessionData");
        boolean result = defaultComputeEnvironmentConfigService.isAvailable(created1.getId(), executionScope);

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertFalse(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created3.getId(), executionScope);

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testNotAvailable_WrongDataType() {
        // Create ComputeEnvironmentConfigs
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();
        ComputeEnvironmentConfig created3 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project1");
        executionScope.put(Experiment, "XNAT_E00001");
        executionScope.put(DataType, "xnat:projectData"); // Wrong data type
        boolean result = defaultComputeEnvironmentConfigService.isAvailable(created1.getId(), executionScope);

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertFalse(result);

        // Test
        result = defaultComputeEnvironmentConfigService.isAvailable(created3.getId(), executionScope);

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testCreate_AllowAllHardware() {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        HardwareConfigEntity hardwareConfigEntity3 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig3));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());
        hardwareConfig3.setId(hardwareConfigEntity3.getId());

        // Next create compute environment config
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute environment config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(3));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2, hardwareConfig3));
    }

    @Test
    @DirtiesContext
    public void testCreate_SelectHardware() {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        HardwareConfigEntity hardwareConfigEntity3 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig3));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());
        hardwareConfig3.setId(hardwareConfigEntity3.getId());

        // Next create compute environment config
        ComputeEnvironmentConfig created2 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig2);
        commitTransaction();

        // Verify that only the selected hardware config is associated with the compute environment config
        assertThat(created2.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created2.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig2, hardwareConfig3));
    }

    @Test
    @DirtiesContext
    public void testUpdate() throws NotFoundException {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute environment config
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute environment config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));

        // Update the compute environment config
        created1.getHardwareOptions().setAllowAllHardware(false);
        created1.getHardwareOptions().getHardwareConfigs().remove(hardwareConfig1);

        // Update other fields
        created1.getComputeEnvironment().setName("UpdatedName");
        created1.getComputeEnvironment().setImage("UpdatedImage");
        created1.getComputeEnvironment().getEnvironmentVariables().add(new EnvironmentVariable("UpdatedKey", "UpdatedValue"));

        // Update the compute environment config
        defaultComputeEnvironmentConfigService.update(created1);
        commitTransaction();

        // Verify that only the selected hardware config is associated with the compute environment config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(1));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItem(hardwareConfig2));

        // Verify that the other fields were updated
        assertThat(created1.getComputeEnvironment().getName(), is("UpdatedName"));
        assertThat(created1.getComputeEnvironment().getImage(), is("UpdatedImage"));
        assertThat(created1.getComputeEnvironment().getEnvironmentVariables(), hasItem(new EnvironmentVariable("UpdatedKey", "UpdatedValue")));
    }

    @Test
    @DirtiesContext
    public void testDelete() throws Exception {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute environment config
        ComputeEnvironmentConfig created1 = defaultComputeEnvironmentConfigService.create(computeEnvironmentConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute environment config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));

        hardwareConfigEntity1 = hardwareConfigEntityService.retrieve(hardwareConfigEntity1.getId());
        hardwareConfigEntity2 = hardwareConfigEntityService.retrieve(hardwareConfigEntity2.getId());

        assertThat(hardwareConfigEntity1.getComputeEnvironmentHardwareOptions().size(), is(1));
        assertThat(hardwareConfigEntity2.getComputeEnvironmentHardwareOptions().size(), is(1));

        // Delete the compute environment config
        defaultComputeEnvironmentConfigService.delete(created1.getId());
        commitTransaction();

        // Verify that the compute environment config was deleted
        assertThat(defaultComputeEnvironmentConfigService.exists(created1.getId()), is(false));

        // Verify that the hardware config entities were deleted
        hardwareConfigEntity1 = hardwareConfigEntityService.retrieve(hardwareConfigEntity1.getId());
        hardwareConfigEntity2 = hardwareConfigEntityService.retrieve(hardwareConfigEntity2.getId());

        assertThat(hardwareConfigEntity1.getComputeEnvironmentHardwareOptions().size(), is(0));
        assertThat(hardwareConfigEntity2.getComputeEnvironmentHardwareOptions().size(), is(0));
    }

    @Test
    public void testValidate() {
        try {
            defaultComputeEnvironmentConfigService.validate(computeEnvironmentConfigInvalid);
            fail("Expected exception to be thrown");
        } catch (IllegalArgumentException e) {
            // Verify that the exception message contains the expected validation errors
            // Note: the order of the validation errors is not guaranteed
            // Trying not to be too brittle here
            assertThat(e.getMessage(), containsString("name cannot be blank"));
            assertThat(e.getMessage(), containsString("image cannot be blank"));
            assertThat(e.getMessage(), containsString("must have at least one scope"));
            assertThat(e.getMessage(), containsString("hardware configs cannot be null"));
            assertThat(e.getMessage(), containsString("must have at least one config type"));
        }
    }

    public void createDummyConfigs() {
        // Setup hardware
        Hardware hardware1 = Hardware.builder()
                .name("Small")
                .cpuReservation(2.0)
                .cpuLimit(4.0)
                .memoryReservation("4G")
                .memoryLimit("8G")
                .build();

        // Setup hardware constraints
        Constraint hardwareConstraint1 = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Collections.singletonList("worker")))
                .build();

        Constraint hardwareConstraint2 = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        hardware1.setConstraints(Arrays.asList(hardwareConstraint1, hardwareConstraint2));

        // Setup hardware environment variables
        EnvironmentVariable hardwareEnvironmentVariable1 = new EnvironmentVariable("MATLAB_LICENSE_FILE", "12345@myserver");
        EnvironmentVariable hardwareEnvironmentVariable2 = new EnvironmentVariable("NVIDIA_VISIBLE_DEVICES", "all");

        hardware1.setEnvironmentVariables(Arrays.asList(hardwareEnvironmentVariable1, hardwareEnvironmentVariable2));

        // Setup hardware generic resources
        GenericResource hardwareGenericResource1 = new GenericResource("nvidia.com/gpu", "2");
        GenericResource hardwareGenericResource2 = new GenericResource("fpga.com/fpga", "1");

        hardware1.setGenericResources(Arrays.asList(hardwareGenericResource1, hardwareGenericResource2));

        // Setup hardware scopes
        HardwareScope hardwareSiteScope1 = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope1 = HardwareScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope userHardwareScope1 = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes1 = new HashMap<>();
        hardwareScopes1.put(Site, hardwareSiteScope1);
        hardwareScopes1.put(Project, hardwareProjectScope1);
        hardwareScopes1.put(User, userHardwareScope1);

        // Setup a hardware config entity
        hardwareConfig1 = HardwareConfig.builder()
                .hardware(hardware1)
                .scopes(hardwareScopes1)
                .build();

        // Setup second hardware config
        Hardware hardware2 = Hardware.builder()
                .name("Medium")
                .cpuReservation(4.0)
                .cpuLimit(4.0)
                .memoryReservation("8G")
                .memoryLimit("8G")
                .build();

        // Setup hardware constraints
        Constraint hardwareConstraint3 = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Collections.singletonList("worker")))
                .build();

        Constraint hardwareConstraint4 = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        hardware2.setConstraints(Arrays.asList(hardwareConstraint3, hardwareConstraint4));

        // Setup hardware environment variables
        EnvironmentVariable hardwareEnvironmentVariable3 = new EnvironmentVariable("MATLAB_LICENSE_FILE", "12345@myserver");
        EnvironmentVariable hardwareEnvironmentVariable4 = new EnvironmentVariable("NVIDIA_VISIBLE_DEVICES", "all");

        hardware2.setEnvironmentVariables(Arrays.asList(hardwareEnvironmentVariable3, hardwareEnvironmentVariable4));

        // Setup hardware generic resources
        GenericResource hardwareGenericResource3 = new GenericResource("nvidia.com/gpu", "2");
        GenericResource hardwareGenericResource4 = new GenericResource("fpga.com/fpga", "1");

        hardware2.setGenericResources(Arrays.asList(hardwareGenericResource3, hardwareGenericResource4));

        // Setup hardware scopes
        HardwareScope hardwareSiteScope2 = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope2 = HardwareScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope userHardwareScope2 = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes2 = new HashMap<>();
        hardwareScopes2.put(Site, hardwareSiteScope2);
        hardwareScopes2.put(Project, hardwareProjectScope2);
        hardwareScopes2.put(User, userHardwareScope2);

        // Setup second hardware config entity
        hardwareConfig2 = HardwareConfig.builder()
                .hardware(hardware2)
                .scopes(hardwareScopes2)
                .build();

        // Setup second hardware config
        Hardware hardware3 = Hardware.builder()
                .name("Large")
                .cpuReservation(8.0)
                .cpuLimit(8.0)
                .memoryReservation("16G")
                .memoryLimit("16G")
                .build();

        // No constraints, environment variables or generic resources
        hardware3.setConstraints(Collections.emptyList());
        hardware3.setEnvironmentVariables(Collections.emptyList());
        hardware3.setGenericResources(Collections.emptyList());

        // Setup hardware scopes
        HardwareScope hardwareSiteScope3 = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope3 = HardwareScope.builder()
                .scope(Project)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("ProjectABCDE")))
                .build();

        HardwareScope userHardwareScope3 = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes3 = new HashMap<>();
        hardwareScopes3.put(Site, hardwareSiteScope3);
        hardwareScopes3.put(Project, hardwareProjectScope3);
        hardwareScopes3.put(User, userHardwareScope3);

        // Setup second hardware config entity
        hardwareConfig3 = HardwareConfig.builder()
                .hardware(hardware3)
                .scopes(hardwareScopes3)
                .build();

        // Setup first compute environment
        ComputeEnvironment computeEnvironment1 = ComputeEnvironment.builder()
                .name("Jupyter Datascience Notebook")
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope1 = ComputeEnvironmentScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope1 = ComputeEnvironmentScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeEnvironmentScope computeEnvironmentUserScope1 = ComputeEnvironmentScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes1 = new HashMap<>();
        computeEnvironmentScopes1.put(Site, computeEnvironmentSiteScope1);
        computeEnvironmentScopes1.put(Project, computeEnvironmentProjectScope1);
        computeEnvironmentScopes1.put(User, computeEnvironmentUserScope1);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions1 = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig1, hardwareConfig2, hardwareConfig3)))
                .build();

        computeEnvironmentConfig1 = ComputeEnvironmentConfig.builder()
                .configTypes(new HashSet<>(Collections.singletonList(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB)))
                .computeEnvironment(computeEnvironment1)
                .scopes(computeEnvironmentScopes1)
                .hardwareOptions(computeEnvironmentHardwareOptions1)
                .build();

        // Setup second compute environment
        ComputeEnvironment computeEnvironment2 = ComputeEnvironment.builder()
                .name("XNAT Datascience Notebook")
                .image("xnat/datascience-notebook:latest")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope2 = ComputeEnvironmentScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope2 = ComputeEnvironmentScope.builder()
                .scope(Project)
                .enabled(false)
                .ids(new HashSet<>(new ArrayList<>(Arrays.asList("Project1", "Project10", "Project100"))))
                .build();

        ComputeEnvironmentScope computeEnvironmentUserScope2 = ComputeEnvironmentScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(new ArrayList<>(Arrays.asList("User1", "User10", "User100"))))
                .build();

        ComputeEnvironmentScope computeEnvironmentDatatypeScope2 = ComputeEnvironmentScope.builder()
                .scope(DataType)
                .enabled(false)
                .ids(new HashSet<>(Arrays.asList("xnat:mrSessionData", "xnat:petSessionData", "xnat:ctSessionData")))
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes2 = new HashMap<>();
        computeEnvironmentScopes2.put(Site, computeEnvironmentSiteScope2);
        computeEnvironmentScopes2.put(Project, computeEnvironmentProjectScope2);
        computeEnvironmentScopes2.put(User, computeEnvironmentUserScope2);
        computeEnvironmentScopes2.put(DataType, computeEnvironmentDatatypeScope2);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions2 = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(false)
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig2, hardwareConfig3)))
                .build();

        computeEnvironmentConfig2 = ComputeEnvironmentConfig.builder()
                .id(2L)
                .configTypes(new HashSet<>(Collections.singletonList(CONTAINER_SERVICE)))
                .computeEnvironment(computeEnvironment2)
                .scopes(computeEnvironmentScopes2)
                .hardwareOptions(computeEnvironmentHardwareOptions2)
                .build();

        // Setup third compute environment
        ComputeEnvironment computeEnvironment3 = ComputeEnvironment.builder()
                .name("MATLAB Datascience Notebook")
                .image("matlab/datascience-notebook:latest")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope3 = ComputeEnvironmentScope.builder()
                .scope(Site)
                .enabled(false)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope3 = ComputeEnvironmentScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ComputeEnvironmentScope computeEnvironmentUserScope3 = ComputeEnvironmentScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes3 = new HashMap<>();
        computeEnvironmentScopes3.put(Site, computeEnvironmentSiteScope3);
        computeEnvironmentScopes3.put(Project, computeEnvironmentProjectScope3);
        computeEnvironmentScopes3.put(User, computeEnvironmentUserScope3);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions3 = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(new HashSet<>())
                .build();

        computeEnvironmentConfig3 = ComputeEnvironmentConfig.builder()
                .configTypes(new HashSet<>(Arrays.asList(CONTAINER_SERVICE, JUPYTERHUB)))
                .computeEnvironment(computeEnvironment3)
                .scopes(computeEnvironmentScopes3)
                .hardwareOptions(computeEnvironmentHardwareOptions3)
                .build();

        // Setup invalid compute environment config
        ComputeEnvironment computeEnvironmentInvalid = ComputeEnvironment.builder()
                .name("") // invalid
                .image("") // invalid
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopesInvalid = new HashMap<>(); // invalid, no scopes

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptionsInvalid = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(null) // invalid
                .build();

        computeEnvironmentConfigInvalid = ComputeEnvironmentConfig.builder()
                .configTypes(null) // invalid
                .computeEnvironment(computeEnvironmentInvalid)
                .scopes(computeEnvironmentScopesInvalid)
                .hardwareOptions(computeEnvironmentHardwareOptionsInvalid)
                .build();
    }

}