package org.nrg.xnat.compute.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.HibernateHardwareConfigEntityServiceTestConfig;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.Hardware;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnat.compute.models.HardwareScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateHardwareConfigEntityServiceTestConfig.class)
public class HibernateHardwareConfigEntityServiceTest {

    @Autowired private HibernateHardwareConfigEntityService hibernateHardwareConfigEntityService;

    @Test
    public void test() {
        assertNotNull(hibernateHardwareConfigEntityService);
    }

    @Test
    @DirtiesContext
    public void testCreateAndUpdateHardwareConfig() {
        // Create hardware config
        HardwareConfig hardwareConfigPojo = createHardwareConfig();

        // Save hardware config entity
        HardwareConfigEntity hardwareConfigEntity = hibernateHardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfigPojo));
        commitTransaction();

        // Retrieve hardware config entity
        HardwareConfigEntity retrievedHardwareConfigEntity = hibernateHardwareConfigEntityService.retrieve(hardwareConfigEntity.getId());

        // Verify hardware config entity
        assertNotNull(retrievedHardwareConfigEntity);

        // Update the pojo with the id
        hardwareConfigPojo.setId(retrievedHardwareConfigEntity.getId());

        // Check that the retrieved hardware config entity matches the original
        assertNotNull(retrievedHardwareConfigEntity);
        assertEquals(hardwareConfigEntity.toPojo(), retrievedHardwareConfigEntity.toPojo());

        // Add a scope to the hardware config
        HardwareScope hardwareDataTypeScope = HardwareScope.builder()
                .scope(DataType)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        hardwareConfigPojo.getScopes().put(DataType, hardwareDataTypeScope);

        // Updated hardware config
        retrievedHardwareConfigEntity.update(hardwareConfigPojo);
        hibernateHardwareConfigEntityService.update(retrievedHardwareConfigEntity);
        commitTransaction();

        // Retrieve updated hardware config
        HardwareConfigEntity updatedHardwareConfigEntity = hibernateHardwareConfigEntityService.retrieve(hardwareConfigEntity.getId());

        // Verify updated hardware config
        assertNotNull(updatedHardwareConfigEntity);
        assertEquals(hardwareConfigPojo, updatedHardwareConfigEntity.toPojo());
        assertEquals(retrievedHardwareConfigEntity.toPojo(), updatedHardwareConfigEntity.toPojo());
        assertThat(updatedHardwareConfigEntity.getScopes().keySet(), hasItems(Scope.Site, Scope.Project, Scope.User, Scope.DataType));
        assertThat(updatedHardwareConfigEntity.getScopes().keySet().size(), is(4));

        // Remove a scope from the hardware config
        hardwareConfigPojo.getScopes().remove(Project);

        // Updated hardware config
        updatedHardwareConfigEntity.update(hardwareConfigPojo);

        hibernateHardwareConfigEntityService.update(updatedHardwareConfigEntity);
        commitTransaction();

        // Retrieve updated hardware config
        HardwareConfigEntity updatedHardwareConfigEntity2 = hibernateHardwareConfigEntityService.retrieve(hardwareConfigEntity.getId());

        // Verify updated hardware config
        assertEquals(updatedHardwareConfigEntity.toPojo(), updatedHardwareConfigEntity2.toPojo());
        assertEquals(updatedHardwareConfigEntity.toPojo(), updatedHardwareConfigEntity2.toPojo());
        assertThat(updatedHardwareConfigEntity2.getScopes().keySet(), hasItems(Scope.Site, Scope.User, Scope.DataType));
        assertThat(updatedHardwareConfigEntity2.getScopes().keySet(), not(hasItems(Scope.Project)));
        assertThat(updatedHardwareConfigEntity2.getScopes().keySet().size(), is(3));
    }

    private HardwareConfig createHardwareConfig() {
        // Setup hardware
        Hardware hardware = Hardware.builder()
                .name("Small")
                .cpuReservation(2.0)
                .cpuLimit(4.0)
                .memoryReservation("4G")
                .memoryLimit("8G")
                .constraints(Collections.emptyList())
                .environmentVariables(Collections.emptyList())
                .genericResources(Collections.emptyList())
                .build();

        // Setup hardware scopes
        HardwareScope hardwareSiteScope = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope = HardwareScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope userHardwareScope = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes1 = new HashMap<>();
        hardwareScopes1.put(Site, hardwareSiteScope);
        hardwareScopes1.put(Project, hardwareProjectScope);
        hardwareScopes1.put(User, userHardwareScope);

        // Create hardware config model
        return HardwareConfig.builder()
                .hardware(hardware)
                .scopes(hardwareScopes1)
                .build();
    }

}