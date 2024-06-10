package org.nrg.xnat.compute.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.config.DefaultConstraintConfigServiceTestConfig;
import org.nrg.xnat.compute.models.Constraint;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.models.ConstraintScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = DefaultConstraintConfigServiceTestConfig.class)
public class DefaultConstraintConfigServiceTest {

    @Autowired private DefaultConstraintConfigService constraintConfigService;

    private ConstraintConfig constraintConfig1;
    private ConstraintConfig constraintConfig2;
    private ConstraintConfig constraintConfig3;
    private ConstraintConfig constraintConfigInvalid;

    @Before
    public void before() {
        createDummyConstraintConfigs();
    }

    @Test
    @DirtiesContext
    public void testRetrieve() {
        // Test
        ConstraintConfig created = constraintConfigService.create(constraintConfig1);
        commitTransaction();
        Optional<ConstraintConfig> retrieved = constraintConfigService.retrieve(created.getId());

        // Verify
        assertTrue(retrieved.isPresent());
        assertEquals(created, retrieved.get());

        constraintConfig1.setId(created.getId());
        assertThat(retrieved.get(), is(constraintConfig1));
    }

    @Test
    @DirtiesContext
    public void testRetrieveDoesNotExist() {
        // Test
        Optional<ConstraintConfig> retrieved = constraintConfigService.retrieve(1L);

        // Verify
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DirtiesContext
    public void testGetAll() {
        // Test
        ConstraintConfig created1 = constraintConfigService.create(constraintConfig1);
        commitTransaction();
        ConstraintConfig created2 = constraintConfigService.create(constraintConfig2);
        commitTransaction();
        ConstraintConfig created3 = constraintConfigService.create(constraintConfig3);
        commitTransaction();

        List<ConstraintConfig> retrieved = constraintConfigService.getAll();

        // Verify
        assertEquals(3, retrieved.size());
        assertThat(retrieved, hasItems(created1, created2, created3));
    }

    @Test
    @DirtiesContext
    public void testCreate() {
        // Test
        ConstraintConfig created = constraintConfigService.create(constraintConfig1);
        commitTransaction();

        // Verify
        assertNotNull(created.getId());
        constraintConfig1.setId(created.getId());
        assertEquals(created, constraintConfig1);
    }

    @Test
    @DirtiesContext
    public void testUpdate() throws NotFoundException {
        // Test
        ConstraintConfig created = constraintConfigService.create(constraintConfig1);
        commitTransaction();
        created.getConstraint().setKey("newKey");
        ConstraintConfig updated = constraintConfigService.update(created);
        commitTransaction();

        // Verify
        assertEquals(created, updated);
    }

    @Test
    @DirtiesContext
    public void testDelete() {
        // Test
        ConstraintConfig created = constraintConfigService.create(constraintConfig1);
        commitTransaction();
        constraintConfigService.delete(created.getId());
        commitTransaction();

        // Verify
        Optional<ConstraintConfig> retrieved = constraintConfigService.retrieve(created.getId());
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DirtiesContext
    public void testGetAvailable() {
        // Setup
        ConstraintConfig created1 = constraintConfigService.create(constraintConfig1);
        commitTransaction();
        ConstraintConfig created2 = constraintConfigService.create(constraintConfig2);
        commitTransaction();
        ConstraintConfig created3 = constraintConfigService.create(constraintConfig3);
        commitTransaction();

        // Test
        Map<Scope, String> executionScopes = new HashMap<>();
        executionScopes.put(Scope.Project, "ProjectA");
        List<ConstraintConfig> retrieved = constraintConfigService.getAvailable(executionScopes);

        // Verify
        assertEquals(2, retrieved.size());
        assertThat(retrieved, hasItems(created1, created3));

        // Test
        executionScopes.put(Scope.Project, "ProjectB");
        retrieved = constraintConfigService.getAvailable(executionScopes);

        // Verify
        assertEquals(2, retrieved.size());
        assertThat(retrieved, hasItems(created1, created3));

        // Test
        executionScopes.put(Scope.Project, "ProjectC");
        retrieved = constraintConfigService.getAvailable(executionScopes);

        // Verify
        assertEquals(1, retrieved.size());
        assertThat(retrieved, hasItem(created1));
    }

    @Test
    public void testValidate() {
        try {
            constraintConfigService.validate(constraintConfigInvalid);
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("key cannot be null or blank"));
            assertThat(e.getMessage(), containsString("values cannot be null or empty"));
            assertThat(e.getMessage(), containsString("operator cannot be null"));
            assertThat(e.getMessage(), containsString("Scopes cannot be null or empty"));
        }
    }

    protected void createDummyConstraintConfigs() {
        createDummyConstraintConfig1();
        createDummyConstraintConfig2();
        createDummyConstraintConfig3();
        createDummyConstraintConfigInvalid();
    }

    protected void createDummyConstraintConfig1() {
        constraintConfig1 = new ConstraintConfig();

        Constraint constraint1 = new Constraint();
        constraint1.setKey("node.role");
        constraint1.setOperator(Constraint.Operator.IN);
        constraint1.setValues(new HashSet<>(Collections.singletonList("worker")));
        constraintConfig1.setConstraint(constraint1);

        ConstraintScope siteScope1 = ConstraintScope.builder()
                .scope(Scope.Site)
                .enabled(true)
                .ids(Collections.emptySet())
                .build();
        ConstraintScope projectScope1 = ConstraintScope.builder()
                .scope(Scope.Project)
                .enabled(true)
                .ids(Collections.emptySet())
                .build();

        Map<Scope, ConstraintScope> scopes1 = new HashMap<>();
        scopes1.put(Scope.Site, siteScope1);
        scopes1.put(Scope.Project, projectScope1);

        constraintConfig1.setScopes(scopes1);
    }

    protected void createDummyConstraintConfig2() {
        constraintConfig2 = new ConstraintConfig();

        Constraint constraint2 = new Constraint();
        constraint2.setKey("node.role");
        constraint2.setOperator(Constraint.Operator.IN);
        constraint2.setValues(new HashSet<>(Collections.singletonList("worker")));
        constraintConfig2.setConstraint(constraint2);

        ConstraintScope siteScope2 = ConstraintScope.builder()
                .scope(Scope.Site)
                .enabled(false)
                .ids(Collections.emptySet())
                .build();
        ConstraintScope projectScope2 = ConstraintScope.builder()
                .scope(Scope.Project)
                .enabled(true)
                .ids(Collections.emptySet())
                .build();

        Map<Scope, ConstraintScope> scopes2 = new HashMap<>();
        scopes2.put(Scope.Site, siteScope2);
        scopes2.put(Scope.Project, projectScope2);

        constraintConfig2.setScopes(scopes2);
    }

    protected void createDummyConstraintConfig3() {
        constraintConfig3 = new ConstraintConfig();

        Constraint constraint3 = new Constraint();
        constraint3.setKey("node.label.projects");
        constraint3.setOperator(Constraint.Operator.IN);
        constraint3.setValues(new HashSet<>(Arrays.asList("ProjectA", "ProjectB")));
        constraintConfig3.setConstraint(constraint3);

        ConstraintScope siteScope3 = ConstraintScope.builder()
                .scope(Scope.Site)
                .enabled(true)
                .ids(Collections.emptySet())
                .build();
        ConstraintScope projectScope3 = ConstraintScope.builder()
                .scope(Scope.Project)
                .enabled(false)
                .ids(new HashSet<>(Arrays.asList("ProjectA", "ProjectB")))
                .build();

        Map<Scope, ConstraintScope> scopes3 = new HashMap<>();
        scopes3.put(Scope.Site, siteScope3);
        scopes3.put(Scope.Project, projectScope3);

        constraintConfig3.setScopes(scopes3);
    }

    protected void createDummyConstraintConfigInvalid() {
        constraintConfigInvalid = new ConstraintConfig();

        Constraint constraintInvalid = new Constraint();
        constraintInvalid.setKey("");
        constraintInvalid.setOperator(null);
        constraintInvalid.setValues(new HashSet<>());
        constraintConfigInvalid.setConstraint(constraintInvalid);

        Map<Scope, ConstraintScope> scopesInvalid = new HashMap<>();

        constraintConfigInvalid.setScopes(scopesInvalid);
    }

}