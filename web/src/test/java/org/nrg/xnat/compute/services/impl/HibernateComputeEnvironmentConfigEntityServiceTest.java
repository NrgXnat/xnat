package org.nrg.xnat.compute.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.HibernateComputeEnvironmentConfigEntityServiceTestConfig;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.repositories.ComputeEnvironmentConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.CONTAINER_SERVICE;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.JUPYTERHUB;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateComputeEnvironmentConfigEntityServiceTestConfig.class)
public class HibernateComputeEnvironmentConfigEntityServiceTest {

    @Autowired private HibernateComputeEnvironmentConfigEntityService hibernateComputeEnvironmentConfigEntityService;
    @Autowired @Qualifier("hardwareConfigDaoImpl") private HardwareConfigDao hardwareConfigDaoImpl;
    @Autowired @Qualifier("computeEnvironmentConfigDaoImpl") private ComputeEnvironmentConfigDao computeEnvironmentConfigDaoImpl;

    private ComputeEnvironmentConfig computeEnvironmentConfig1;
    private ComputeEnvironmentConfig computeEnvironmentConfig2;
    private ComputeEnvironmentConfig computeEnvironmentConfig3;

    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;

    @Before
    public void before() {
        hibernateComputeEnvironmentConfigEntityService.setDao(computeEnvironmentConfigDaoImpl);
        createDummyConfigsAndEntities();
    }

    @After
    public void after() {
        Mockito.reset();
    }

    @Test
    public void test() {
        assertNotNull(hibernateComputeEnvironmentConfigEntityService);
        assertNotNull(hardwareConfigDaoImpl);
        assertNotNull(computeEnvironmentConfigDaoImpl);
    }

    @Test
    @DirtiesContext
    public void testCreate() {
        // Setup
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);

        // Execute
        ComputeEnvironmentConfigEntity created = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        commitTransaction();
        ComputeEnvironmentConfigEntity retrieved = hibernateComputeEnvironmentConfigEntityService.retrieve(created.getId());

        // Verify
        assertNotNull(retrieved);
        assertThat(retrieved.toPojo(), is(created.toPojo()));
    }

    @Test
    @DirtiesContext
    public void testAddHardwareConfigEntity() {
        // Setup
        HardwareConfigEntity hardwareConfigEntity1 = HardwareConfigEntity.fromPojo(hardwareConfig1);
        HardwareConfigEntity hardwareConfigEntity2 = HardwareConfigEntity.fromPojo(hardwareConfig2);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity2 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig2);

        // Commit hardware configs
        Long hardwareConfigEntity1_id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity1);
        Long hardwareConfigEntity2_Id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity2);

        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity2);

        commitTransaction();

        // Verify
        assertNotNull(hardwareConfigEntity1_id);
        assertNotNull(hardwareConfigEntity2_Id);
        assertNotNull(createdComputeEnvironmentConfigEntity1);
        assertNotNull(createdComputeEnvironmentConfigEntity2);

        // Execute
        // Add hardware configs to compute environment configs
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity2_Id);
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity2.getId());

        assertNotNull(retrievedComputeEnvironmentConfigEntity1);
        assertNotNull(retrievedComputeEnvironmentConfigEntity2);
        assertEquals(2, retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(1, retrievedComputeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
        assertTrue(retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
        assertTrue(retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity2_Id)));
        assertTrue(retrievedComputeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
    }

    @Test
    @DirtiesContext
    public void testRemoveHardwareConfigEntity() {
        // Setup
        HardwareConfigEntity hardwareConfigEntity1 = HardwareConfigEntity.fromPojo(hardwareConfig1);
        HardwareConfigEntity hardwareConfigEntity2 = HardwareConfigEntity.fromPojo(hardwareConfig2);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity2 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig2);

        // Commit hardware configs
        Long hardwareConfigEntity1_id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity1);
        Long hardwareConfigEntity2_Id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity2);

        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity2);

        commitTransaction();

        // Verify
        assertNotNull(hardwareConfigEntity1_id);
        assertNotNull(hardwareConfigEntity2_Id);
        assertNotNull(createdComputeEnvironmentConfigEntity1);
        assertNotNull(createdComputeEnvironmentConfigEntity2);

        // Execute
        // Add hardware configs to compute environment configs
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity2_Id);
        hibernateComputeEnvironmentConfigEntityService.addHardwareConfigEntity(createdComputeEnvironmentConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity2.getId());

        assertNotNull(retrievedComputeEnvironmentConfigEntity1);
        assertNotNull(retrievedComputeEnvironmentConfigEntity2);
        assertEquals(2, retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(1, retrievedComputeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
        assertTrue(retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
        assertTrue(retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity2_Id)));
        assertTrue(retrievedComputeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));

        // Remove hardware configs from compute environment configs
        hibernateComputeEnvironmentConfigEntityService.removeHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeEnvironmentConfigEntityService.removeHardwareConfigEntity(createdComputeEnvironmentConfigEntity1.getId(), hardwareConfigEntity2_Id);

        commitTransaction();

        hibernateComputeEnvironmentConfigEntityService.removeHardwareConfigEntity(createdComputeEnvironmentConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        retrievedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());
        retrievedComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity2.getId());

        assertNotNull(retrievedComputeEnvironmentConfigEntity1);
        assertNotNull(retrievedComputeEnvironmentConfigEntity2);
        assertEquals(0, retrievedComputeEnvironmentConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(0, retrievedComputeEnvironmentConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
    }

    @Test
    @DirtiesContext
    public void testFindByType() {
        // Setup
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity2 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig2);
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity3 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig3);

        // Commit compute environment configs
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity2 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity2);
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity3 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity3);

        commitTransaction();

        // Verify
        assertNotNull(createdComputeEnvironmentConfigEntity1);
        assertNotNull(createdComputeEnvironmentConfigEntity2);
        assertNotNull(createdComputeEnvironmentConfigEntity3);

        // Execute
        List<ComputeEnvironmentConfigEntity> retrievedComputeEnvironmentConfigEntities = hibernateComputeEnvironmentConfigEntityService.findByType(JUPYTERHUB);

        // Verify
        assertNotNull(retrievedComputeEnvironmentConfigEntities);
        assertEquals(2, retrievedComputeEnvironmentConfigEntities.size());
        assertEquals(createdComputeEnvironmentConfigEntity1.getId(), retrievedComputeEnvironmentConfigEntities.get(0).getId());
        assertEquals(createdComputeEnvironmentConfigEntity3.getId(), retrievedComputeEnvironmentConfigEntities.get(1).getId());

        // Execute
        retrievedComputeEnvironmentConfigEntities = hibernateComputeEnvironmentConfigEntityService.findByType(CONTAINER_SERVICE);

        // Verify
        assertNotNull(retrievedComputeEnvironmentConfigEntities);
        assertEquals(2, retrievedComputeEnvironmentConfigEntities.size());
        assertEquals(createdComputeEnvironmentConfigEntity2.getId(), retrievedComputeEnvironmentConfigEntities.get(0).getId());
        assertEquals(createdComputeEnvironmentConfigEntity3.getId(), retrievedComputeEnvironmentConfigEntities.get(1).getId());
    }

    @Test
    @DirtiesContext
    public void testAddScopeToComputeEnvironment() {
        // Create compute environment config entity
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);

        // Commit compute environment configs
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        commitTransaction();

        // Retrieve compute environment config entity
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());

        // Verify
        assertNotNull(retrievedComputeEnvironmentConfigEntity1);

        // Update the pojo with the id
        computeEnvironmentConfig1.setId(retrievedComputeEnvironmentConfigEntity1.getId());

        // Add scope to compute environment
        Map<Scope, ComputeEnvironmentScope> scopes1 = computeEnvironmentConfig1.getScopes();

        ComputeEnvironmentScope dataTypeScope = ComputeEnvironmentScope.builder()
                .scope(Scope.DataType)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("xnat:petSessionData")))
                .build();

        scopes1.put(Scope.DataType, dataTypeScope);

        // Update and commit entity
        retrievedComputeEnvironmentConfigEntity1.update(computeEnvironmentConfig1);
        hibernateComputeEnvironmentConfigEntityService.update(retrievedComputeEnvironmentConfigEntity1);
        commitTransaction();

        // Retrieve updated compute environment config entity
        ComputeEnvironmentConfigEntity updatedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());

        // Verify
        assertNotNull(updatedComputeEnvironmentConfigEntity1);
        assertEquals(computeEnvironmentConfig1, updatedComputeEnvironmentConfigEntity1.toPojo());
        assertThat(updatedComputeEnvironmentConfigEntity1.getScopes().keySet(), hasItems(Scope.Site, Scope.Project, Scope.User, Scope.DataType));
        assertThat(updatedComputeEnvironmentConfigEntity1.getScopes().get(Scope.DataType).getIds(), hasItems("xnat:petSessionData"));
        assertThat(updatedComputeEnvironmentConfigEntity1.getScopes().keySet().size(), is(4));
    }

    @Test
    @DirtiesContext
    public void testRemoveScopeFromComputeEnvironment() {
        // Setup
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity1 = ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig1);

        // Commit compute environment configs
        ComputeEnvironmentConfigEntity createdComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.create(computeEnvironmentConfigEntity1);
        commitTransaction();

        // Verify
        assertNotNull(createdComputeEnvironmentConfigEntity1);

        // Update the pojo with the id
        computeEnvironmentConfig1.setId(createdComputeEnvironmentConfigEntity1.getId());

        // Remove scope from compute environment
        Map<Scope, ComputeEnvironmentScope> scopes1 = computeEnvironmentConfig1.getScopes();
        scopes1.remove(Project);

        // Update and commit entity
        createdComputeEnvironmentConfigEntity1.update(computeEnvironmentConfig1);
        hibernateComputeEnvironmentConfigEntityService.update(createdComputeEnvironmentConfigEntity1);
        commitTransaction();

        // Verify
        ComputeEnvironmentConfigEntity retrievedComputeEnvironmentConfigEntity1 = hibernateComputeEnvironmentConfigEntityService.retrieve(createdComputeEnvironmentConfigEntity1.getId());
        assertNotNull(retrievedComputeEnvironmentConfigEntity1);
        assertEquals(computeEnvironmentConfig1, retrievedComputeEnvironmentConfigEntity1.toPojo());
        assertThat(retrievedComputeEnvironmentConfigEntity1.getScopes().keySet(), hasItems(Scope.Site, Scope.User));
        assertThat(retrievedComputeEnvironmentConfigEntity1.getScopes().keySet(), not(hasItems(Scope.Project)));
        assertThat(retrievedComputeEnvironmentConfigEntity1.getScopes().keySet().size(), is(2));
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

        // Setup first compute environment
        // Create mount first
        Mount mount1 = Mount.builder()
                .localPath("/home/jovyan/work")
                .containerPath("/home/jovyan/work")
                .readOnly(false)
                .build();

        Mount mount2 = Mount.builder()
                .localPath("/tools/MATLAB/R2019b")
                .containerPath("/tools/MATLAB/R2019b")
                .readOnly(true)
                .build();

        ComputeEnvironment computeEnvironment1 = ComputeEnvironment.builder()
                .name("Jupyter Datascience Notebook")
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .environmentVariables(Collections.singletonList(new EnvironmentVariable("JUPYTER_ENABLE_LAB", "yes")))
                .mounts(Arrays.asList(mount1, mount2))
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
                .hardwareConfigs(new HashSet<>())
                .build();

        computeEnvironmentConfig1 = ComputeEnvironmentConfig.builder()
                .configTypes(new HashSet<>(Collections.singletonList(JUPYTERHUB)))
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
                .hardwareConfigs(new HashSet<>())
                .build();

        computeEnvironmentConfig2 = ComputeEnvironmentConfig.builder()
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
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope3 = ComputeEnvironmentScope.builder()
                .scope(Project)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("Project1")))
                .build();

        ComputeEnvironmentScope computeEnvironmentUserScope3 = ComputeEnvironmentScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("User1")))
                .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes3 = new HashMap<>();
        computeEnvironmentScopes2.put(Site, computeEnvironmentSiteScope3);
        computeEnvironmentScopes2.put(Project, computeEnvironmentProjectScope3);
        computeEnvironmentScopes2.put(User, computeEnvironmentUserScope3);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions3 = ComputeEnvironmentHardwareOptions.builder()
                .allowAllHardware(false)
                .hardwareConfigs(new HashSet<>())
                .build();

        computeEnvironmentConfig3 = ComputeEnvironmentConfig.builder()
                .configTypes(new HashSet<>(Arrays.asList(CONTAINER_SERVICE, JUPYTERHUB)))
                .computeEnvironment(computeEnvironment3)
                .scopes(computeEnvironmentScopes3)
                .hardwareOptions(computeEnvironmentHardwareOptions3)
                .build();
    }

}