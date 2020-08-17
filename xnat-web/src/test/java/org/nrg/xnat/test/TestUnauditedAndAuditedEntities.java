package org.nrg.xnat.test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.config.TestUnauditedAndAuditedEntitiesConfig;
import org.nrg.xnat.test.entities.AuditedEntity;
import org.nrg.xnat.test.entities.UnauditedEntity;
import org.nrg.xnat.test.services.AuditedEntitiesService;
import org.nrg.xnat.test.services.UnauditedEntitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUnauditedAndAuditedEntitiesConfig.class)
@Slf4j
public class TestUnauditedAndAuditedEntities {
    @Test
    public void contextLoads() {
        log.debug("Loaded the context for the unaudited and audited entities tests");
        assertThat(_auditedEntitiesService).isNotNull();
        assertThat(_unauditedEntitiesService).isNotNull();
    }

    @Test
    public void testBasicUnauditedEntityTransaction() {
        final UnauditedEntity entity = new UnauditedEntity("unaudited1", "This is unaudited entity #1");
        final UnauditedEntity saved  = _unauditedEntitiesService.create(entity);
        assertThat(saved).hasFieldOrPropertyWithValue("name", entity.getName())
                         .hasFieldOrPropertyWithValue("description", entity.getDescription());

        final long id = saved.getId();

        final UnauditedEntity found = _unauditedEntitiesService.retrieve(id);
        assertThat(found).hasFieldOrPropertyWithValue("id", id)
                         .hasFieldOrPropertyWithValue("name", saved.getName())
                         .hasFieldOrPropertyWithValue("description", saved.getDescription());
        found.setDescription("This is the updated unaudited entity #1");
        _unauditedEntitiesService.update(found);
        final UnauditedEntity updated = _unauditedEntitiesService.retrieve(id);
        assertThat(updated).hasFieldOrPropertyWithValue("id", id)
                           .hasFieldOrPropertyWithValue("name", found.getName())
                           .hasFieldOrPropertyWithValue("description", found.getDescription());
        _unauditedEntitiesService.delete(id);
        final UnauditedEntity nope = _unauditedEntitiesService.retrieve(id);
        assertThat(nope).isNull();
    }


    @Test
    public void testBasicAuditedEntityTransaction() {
        final AuditedEntity entity = new AuditedEntity("audited1", "This is audited entity #1");
        final AuditedEntity saved  = _auditedEntitiesService.create(entity);
        assertThat(saved).hasFieldOrPropertyWithValue("name", entity.getName())
                         .hasFieldOrPropertyWithValue("description", entity.getDescription());

        final long id = saved.getId();

        final AuditedEntity found = _auditedEntitiesService.retrieve(id);
        assertThat(found).hasFieldOrPropertyWithValue("id", id)
                         .hasFieldOrPropertyWithValue("name", saved.getName())
                         .hasFieldOrPropertyWithValue("description", saved.getDescription());
        found.setDescription("This is the updated audited entity #1");
        _auditedEntitiesService.update(found);
        final AuditedEntity updated = _auditedEntitiesService.retrieve(id);
        assertThat(updated).hasFieldOrPropertyWithValue("id", id)
                           .hasFieldOrPropertyWithValue("name", found.getName())
                           .hasFieldOrPropertyWithValue("description", found.getDescription());
        _auditedEntitiesService.delete(id);
        final AuditedEntity nope = _auditedEntitiesService.retrieve(id);
        assertThat(nope).isNull();
    }

    @Autowired
    private AuditedEntitiesService   _auditedEntitiesService;
    @Autowired
    private UnauditedEntitiesService _unauditedEntitiesService;
}
