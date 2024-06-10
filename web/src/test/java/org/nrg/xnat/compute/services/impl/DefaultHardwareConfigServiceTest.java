package org.nrg.xnat.compute.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.DefaultHardwareConfigServiceTestConfig;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = DefaultHardwareConfigServiceTestConfig.class)
public class DefaultHardwareConfigServiceTest {

    @Autowired private DefaultHardwareConfigService defaultHardwareConfigService;
    @Autowired private ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService;

    private ComputeEnvironmentConfig computeEnvironmentConfig1;
    private ComputeEnvironmentConfig computeEnvironmentConfig2;
    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;
    private HardwareConfig hardwareConfigInvalid;

    @Before
    public void before() throws Exception {
        createDummyConfigsAndEntities();
    }

    @Test
    @DirtiesContext
    public void testExists() {
        // Test
        HardwareConfig hardwareConfig = defaultHardwareConfigService.create(hardwareConfig1);
        commitTransaction();
        boolean exists = defaultHardwareConfigService.exists(hardwareConfig.getId());

        // Verify
        assertTrue(exists);
    }

    @Test
    @DirtiesContext
    public void testDoesNotExist() {
        // Test
        boolean exists = defaultHardwareConfigService.exists(3L);

        // Verify
        assertFalse(exists);
    }

    @Test
    @DirtiesContext
    public void testRetrieve() {
        // Setup
        HardwareConfig created = defaultHardwareConfigService.create(hardwareConfig1);
        commitTransaction();
        Optional<HardwareConfig> retrieved = defaultHardwareConfigService.retrieve(created.getId());

        // Verify
        assertTrue(retrieved.isPresent());
        assertThat(retrieved.get(), is(created));

        hardwareConfig1.setId(retrieved.get().getId());
        assertThat(retrieved.get(), is(hardwareConfig1));
    }

    @Test
    @DirtiesContext
    public void testRetrieve_DoesNotExist() {
        // Setup
        Optional<HardwareConfig> retrieved = defaultHardwareConfigService.retrieve(3L);

        // Verify
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DirtiesContext
    public void testRetrieveAll() {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        HardwareConfig created2 = defaultHardwareConfigService.create(hardwareConfig2);
        commitTransaction();

        // Test
        List<HardwareConfig> retrieved = defaultHardwareConfigService.retrieveAll();

        // Verify
        assertEquals(2, retrieved.size());
        assertThat(retrieved, hasItems(created1, created2));
    }

    @Test
    @DirtiesContext
    public void testCreate() {
        // First, create the compute environment configs
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = computeEnvironmentConfigEntityService.create(ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1));
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity2 = computeEnvironmentConfigEntityService.create(ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig2));
        commitTransaction();

        // Now create a hardware config
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        commitTransaction();
        HardwareConfig created2 = defaultHardwareConfigService.create(hardwareConfig2);
        commitTransaction();

        // Then retrieve the compute environment configs
        computeEnvironmentConfigEntity1 = computeEnvironmentConfigEntityService.retrieve(computeEnvironmentConfigEntity1.getId());
        computeEnvironmentConfigEntity2 = computeEnvironmentConfigEntityService.retrieve(computeEnvironmentConfigEntity2.getId());

        // Verify that the hardware config were added only to the first compute environment config
        assertThat(computeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(computeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().size(), is(0));
        assertThat(computeEnvironmentConfigEntity1
                           .getHardwareOptions()
                           .getHardwareConfigs().stream()
                           .map(HardwareConfigEntity::toPojo)
                           .collect(Collectors.toList()),
                   hasItems(created1, created2));
    }

    @Test
    @DirtiesContext
    public void testUpdate() throws Exception {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        commitTransaction();

        // Test
        created1.getHardware().setName("Updated");
        created1.getHardware().setCpuLimit(6.5);
        created1.getHardware().setMemoryLimit("10G");

        defaultHardwareConfigService.update(created1);
        commitTransaction();

        // Verify
        HardwareConfig retrieved = defaultHardwareConfigService.retrieve(created1.getId()).get();

        assertThat(retrieved, is(created1));
        assertThat(retrieved.getHardware().getName(), is("Updated"));
        assertThat(retrieved.getHardware().getCpuLimit(), is(6.5));
        assertThat(retrieved.getHardware().getMemoryLimit(), is("10G"));
    }

    @Test
    @DirtiesContext
    public void testDelete() throws Exception {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        commitTransaction();

        // Test
        defaultHardwareConfigService.delete(created1.getId());
        commitTransaction();

        // Verify
        assertFalse(defaultHardwareConfigService.exists(created1.getId()));
    }

    @Test
    @DirtiesContext
    public void testIsAvailable() {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        HardwareConfig created2 = defaultHardwareConfigService.create(hardwareConfig2);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project1");
        boolean isAvailable1 = defaultHardwareConfigService.isAvailable(created1.getId(), executionScope);
        boolean isAvailable2 = defaultHardwareConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertTrue(isAvailable1);
        assertTrue(isAvailable2);
    }

    @Test
    @DirtiesContext
    public void testIsAvailable_WrongUser() {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        HardwareConfig created2 = defaultHardwareConfigService.create(hardwareConfig2);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User2");
        executionScope.put(Project, "Project1");
        boolean isAvailable1 = defaultHardwareConfigService.isAvailable(created1.getId(), executionScope);
        boolean isAvailable2 = defaultHardwareConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertTrue(isAvailable1);
        assertFalse(isAvailable2);
    }

    @Test
    @DirtiesContext
    public void testIsAvailable_WrongProject() {
        // Setup
        HardwareConfig created1 = defaultHardwareConfigService.create(hardwareConfig1);
        HardwareConfig created2 = defaultHardwareConfigService.create(hardwareConfig2);
        commitTransaction();

        // Test
        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(User, "User1");
        executionScope.put(Project, "Project2");
        boolean isAvailable1 = defaultHardwareConfigService.isAvailable(created1.getId(), executionScope);
        boolean isAvailable2 = defaultHardwareConfigService.isAvailable(created2.getId(), executionScope);

        // Verify
        assertTrue(isAvailable1);
        assertFalse(isAvailable2);
    }

    @Test
    public void testValidate() {
        try {
            defaultHardwareConfigService.validate(hardwareConfigInvalid);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // Verify that the exception message contains the expected validation errors
            // Note: the order of the validation errors is not guaranteed
            // Trying not to be too brittle here
            assertThat(e.getMessage(), containsString("name cannot be blank"));
            assertThat(e.getMessage(), containsString("scopes cannot be null or empty"));
        }
    }

    public void createDummyConfigsAndEntities() {
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

        // Build hardware config
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
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("Project1")))
                .build();

        HardwareScope userHardwareScope2 = HardwareScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("User1")))
                .build();

        Map<Scope, HardwareScope> hardwareScopes2 = new HashMap<>();
        hardwareScopes2.put(Site, hardwareSiteScope2);
        hardwareScopes2.put(Project, hardwareProjectScope2);
        hardwareScopes2.put(User, userHardwareScope2);

        // Build second hardware config
        hardwareConfig2 = HardwareConfig.builder()
                .hardware(hardware2)
                .scopes(hardwareScopes2)
                .build();

        // Setup invalid hardware config
        Hardware hardwareInvalid = Hardware.builder().build();
        Map<Scope, HardwareScope> hardwareScopesInvalid = new HashMap<>();
        hardwareConfigInvalid = HardwareConfig.builder()
                .hardware(hardwareInvalid)
                .scopes(hardwareScopesInvalid)
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
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig1, hardwareConfig2)))
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
                .ids(new HashSet<>(Collections.singletonList("Project1")))
                .build();

        ComputeEnvironmentScope computeEnvironmentUserScope2 = ComputeEnvironmentScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("User1")))
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes2 = new HashMap<>();
        computeEnvironmentScopes2.put(Site, computeEnvironmentSiteScope2);
        computeEnvironmentScopes2.put(Project, computeEnvironmentProjectScope2);
        computeEnvironmentScopes2.put(User, computeEnvironmentUserScope2);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions2 = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(false)
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig2)))
                .build();

        computeEnvironmentConfig2 = ComputeEnvironmentConfig.builder()
                .configTypes(new HashSet<>(Collections.singletonList(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB)))
                .computeEnvironment(computeEnvironment2)
                .scopes(computeEnvironmentScopes2)
                .hardwareOptions(computeEnvironmentHardwareOptions2)
                .build();

    }

}