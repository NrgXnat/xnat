package org.nrg.xnat.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.config.TestHibernateEntityEventListenersConfig;
import org.nrg.xnat.hibernate.entities.Entity1;
import org.nrg.xnat.hibernate.entities.Entity2;
import org.nrg.xnat.hibernate.listeners.methods.HibernateEntityEventHandlerMethod.Action;
import org.nrg.xnat.hibernate.services.Entity1Service;
import org.nrg.xnat.hibernate.services.Entity2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestHibernateEntityEventListenersConfig.class)
@Slf4j
public class TestHibernateEntityEventListeners {
    private static final String ENTITY1_NAME = "Entity 1";
    private static final String ENTITY2_NAME = "Entity 2";

    @Autowired
    private Entity1Service   entity1Service;
    @Autowired
    private Entity2Service   entity2Service;
    @Autowired
    private TestEventTracker eventTracker;

    @Test
    @DirtiesContext
    public void testEventHandlerMethods() {
        final Entity1 entity1Created = entity1Service.create(Entity1.builder().name(ENTITY1_NAME).description("A great entity 1").oneness("Is completely one").build());
        assertThat(entity1Created).isNotNull().hasFieldOrProperty("id");
        final Triple<Action, Class<?>, Long> entity1CreatedEvent = eventTracker.getLastEvent();
        assertThat(entity1CreatedEvent).extracting(Triple::getLeft).isEqualTo(Action.INSERT);
        assertThat(entity1CreatedEvent).extracting(Triple::getMiddle).isEqualTo(Entity1.class);
        assertThat(entity1CreatedEvent).extracting(Triple::getRight).isEqualTo(entity1Created.getId());
        entity1Created.setDescription("Something different");
        entity1Created.setOneness("Something else different");
        entity1Service.update(entity1Created);
        final Entity1 entity1Updated = entity1Service.getByName(ENTITY1_NAME);
        assertThat(entity1Updated).isNotNull().hasFieldOrPropertyWithValue("id", entity1Created.getId()).hasFieldOrPropertyWithValue("name", ENTITY1_NAME).hasFieldOrPropertyWithValue("description", "Something different").hasFieldOrPropertyWithValue("oneness", "Something else different");
        final Triple<Action, Class<?>, Long> entity1UpdatedEvent = eventTracker.getLastEvent();
        assertThat(entity1UpdatedEvent).extracting(Triple::getLeft).isEqualTo(Action.UPDATE);
        assertThat(entity1UpdatedEvent).extracting(Triple::getMiddle).isEqualTo(Entity1.class);
        assertThat(entity1UpdatedEvent).extracting(Triple::getRight).isEqualTo(entity1Created.getId());
        entity1Service.delete(entity1Updated.getId());
        final Triple<Action, Class<?>, Long> entity1DeletedEvent = eventTracker.getLastEvent();
        assertThat(entity1DeletedEvent).extracting(Triple::getLeft).isEqualTo(Action.DELETE);
        assertThat(entity1DeletedEvent).extracting(Triple::getMiddle).isEqualTo(Entity1.class);
        assertThat(entity1DeletedEvent).extracting(Triple::getRight).isEqualTo(entity1Created.getId());
        final Entity2 entity2Created = entity2Service.create(Entity2.builder().name(ENTITY2_NAME).description("A great entity 2").twoness("Is completely two").build());
        assertThat(entity2Created).isNotNull().hasFieldOrProperty("id");
        final Triple<Action, Class<?>, Long> entity2CreatedEvent = eventTracker.getLastEvent();
        assertThat(entity2CreatedEvent).extracting(Triple::getLeft).isEqualTo(Action.INSERT);
        assertThat(entity2CreatedEvent).extracting(Triple::getMiddle).isEqualTo(Entity2.class);
        assertThat(entity2CreatedEvent).extracting(Triple::getRight).isEqualTo(entity2Created.getId());
        entity2Created.setDescription("Something different");
        entity2Created.setTwoness("Something else different");
        entity2Service.update(entity2Created);
        final Entity2 entity2Updated = entity2Service.getByName(ENTITY2_NAME);
        assertThat(entity2Updated).isNotNull().hasFieldOrPropertyWithValue("id", entity2Created.getId()).hasFieldOrPropertyWithValue("name", ENTITY2_NAME).hasFieldOrPropertyWithValue("description", "Something different").hasFieldOrPropertyWithValue("twoness", "Something else different");
        final Triple<Action, Class<?>, Long> entity2UpdatedEvent = eventTracker.getLastEvent();
        assertThat(entity2UpdatedEvent).extracting(Triple::getLeft).isEqualTo(Action.UPDATE);
        assertThat(entity2UpdatedEvent).extracting(Triple::getMiddle).isEqualTo(Entity2.class);
        assertThat(entity2UpdatedEvent).extracting(Triple::getRight).isEqualTo(entity2Created.getId());
        entity2Service.delete(entity2Updated.getId());
        final Triple<Action, Class<?>, Long> entity2DeletedEvent = eventTracker.getLastEvent();
        assertThat(entity2DeletedEvent).extracting(Triple::getLeft).isEqualTo(Action.DELETE);
        assertThat(entity2DeletedEvent).extracting(Triple::getMiddle).isEqualTo(Entity2.class);
        assertThat(entity2DeletedEvent).extracting(Triple::getRight).isEqualTo(entity2Created.getId());
    }
}
