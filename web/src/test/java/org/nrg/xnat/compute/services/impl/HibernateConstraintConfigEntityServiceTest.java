package org.nrg.xnat.compute.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.HibernateConstraintConfigEntityServiceTestConfig;
import org.nrg.xnat.compute.entities.ConstraintConfigEntity;
import org.nrg.xnat.compute.models.Constraint;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.models.ConstraintScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateConstraintConfigEntityServiceTestConfig.class)
public class HibernateConstraintConfigEntityServiceTest {

    @Autowired private HibernateConstraintConfigEntityService hibernateConstraintConfigEntityService;

    @Test
    public void test() {
        assertNotNull(hibernateConstraintConfigEntityService);
    }

    @Test
    @DirtiesContext
    public void testCreateAndUpdateConstraintConfig() {
        // Create constraint config
        ConstraintConfig constraintConfig = createConstraintConfig();

        // Save the constraint config
        ConstraintConfigEntity created = hibernateConstraintConfigEntityService.create(ConstraintConfigEntity.fromPojo(constraintConfig));

        commitTransaction();

        // Retrieve the constraint config
        ConstraintConfigEntity retrieved = hibernateConstraintConfigEntityService.retrieve(created.getId());

        // Update the pojo with the id
        constraintConfig.setId(retrieved.getId());

        // Check that the retrieved constraint config matches the original
        assertEquals(created, retrieved);

        // Add a scope to the constraint config
        ConstraintScope constraintScopeDataType = ConstraintScope.builder()
                .scope(Scope.DataType)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        constraintConfig.getScopes().put(Scope.DataType, constraintScopeDataType);

        // Save the updated constraint config
        retrieved.update(constraintConfig);
        hibernateConstraintConfigEntityService.update(retrieved);

        commitTransaction();

        // Retrieve the updated constraint config
        ConstraintConfigEntity updated = hibernateConstraintConfigEntityService.retrieve(created.getId());

        // Verify updated constraint config
        assertNotNull(updated);
        assertEquals(constraintConfig, updated.toPojo());
        assertEquals(retrieved.toPojo(), updated.toPojo());
        assertThat(updated.getScopes().keySet(), hasItems(Scope.Site, Scope.Project, Scope.User, Scope.DataType));
        assertThat(updated.getScopes().keySet().size(), is(4));

        // Remove a scope from the constraint config
        constraintConfig.getScopes().remove(Scope.Project);

        // Save the updated constraint config
        updated.update(constraintConfig);
        hibernateConstraintConfigEntityService.update(updated);

        commitTransaction();

        // Retrieve the updated constraint config
        ConstraintConfigEntity updated2 = hibernateConstraintConfigEntityService.retrieve(created.getId());

        // Verify updated constraint config
        assertNotNull(updated2);
        assertEquals(updated.toPojo(), updated2.toPojo());
        assertThat(updated2.getScopes().keySet(), hasItems(Scope.Site, Scope.User, Scope.DataType));
        assertThat(updated2.getScopes().keySet(), not(hasItems(Scope.Project)));
        assertThat(updated2.getScopes().keySet().size(), is(3));
    }

    private ConstraintConfig createConstraintConfig() {
        Constraint constraint = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("worker")))
                .build();

        ConstraintScope constraintScopeSite = ConstraintScope.builder()
                .scope(Scope.Site)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ConstraintScope constraintScopeProject = ConstraintScope.builder()
                .scope(Scope.Project)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ConstraintScope constraintScopeUser = ConstraintScope.builder()
                .scope(Scope.User)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        Map<Scope, ConstraintScope> scopes = new HashMap<>();
        scopes.put(Scope.Site, constraintScopeSite);
        scopes.put(Scope.Project, constraintScopeProject);
        scopes.put(Scope.User, constraintScopeUser);

        return ConstraintConfig.builder()
                .constraint(constraint)
                .scopes(scopes)
                .build();
    }

}