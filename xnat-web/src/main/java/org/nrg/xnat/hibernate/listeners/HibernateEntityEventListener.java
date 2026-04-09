package org.nrg.xnat.hibernate.listeners;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.prefs.entities.Preference;
import org.nrg.xnat.hibernate.listeners.methods.HibernateEntityEventHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class HibernateEntityEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {
    @Serial
    private static final long serialVersionUID = 851247313511152484L;

    private static final List<Class<?>> IGNORED_ENTITY_CLASSES = Arrays.asList(DefaultRevisionEntity.class, Preference.class);

    private final List<HibernateEntityEventHandlerMethod> insertHandlerMethods = new ArrayList<>();
    private final List<HibernateEntityEventHandlerMethod> updateHandlerMethods = new ArrayList<>();
    private final List<HibernateEntityEventHandlerMethod> deleteHandlerMethods = new ArrayList<>();

    @Autowired
    public HibernateEntityEventListener(final SessionFactory sessionFactory, final List<HibernateEntityEventHandlerMethod> handlerMethods) {
        if (sessionFactory instanceof SessionFactoryImplementor) {
            final EventListenerRegistry registry = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
            registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
            registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
            registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(this);
            log.debug("Registered {} as a Hibernate event listener for post-insert, post-update, and post-delete events", getClass().getName());
        } else {
            log.warn("Could not register as Hibernate event listener: sessionFactory is not a SessionFactoryImplementor");
        }
        handlerMethods.forEach(method -> {
            if (method.getHandledActions().contains(HibernateEntityEventHandlerMethod.Action.INSERT)) {
                insertHandlerMethods.add(method);
            }
            if (method.getHandledActions().contains(HibernateEntityEventHandlerMethod.Action.UPDATE)) {
                updateHandlerMethods.add(method);
            }
            if (method.getHandledActions().contains(HibernateEntityEventHandlerMethod.Action.DELETE)) {
                deleteHandlerMethods.add(method);
            }
        });
    }

    @Override
    public void onPostInsert(final PostInsertEvent event) {
        final Object entity = event.getEntity();
        if (IGNORED_ENTITY_CLASSES.contains(entity.getClass())) {
            log.debug("PostInsertEventListener onPostInsert got entity of type {} but that is an ignored class", entity.getClass().getName());
            return;
        }
        if (log.isDebugEnabled()) {
            if (entity instanceof AbstractHibernateEntity) {
                log.debug("PostInsertEventListener onPostInsert got entity of type {} ID {}", entity.getClass().getName(), ((AbstractHibernateEntity) entity).getId());
            } else {
                log.debug("PostInsertEventListener onPostInsert got entity of type {}", entity.getClass().getName());
            }
        }
        insertHandlerMethods.stream()
                            .filter(method -> method.matches(event))
                            .forEach(method -> method.handleEvent(event));
    }

    @Override
    public void onPostUpdate(final PostUpdateEvent event) {
        final Object entity = event.getEntity();
        if (IGNORED_ENTITY_CLASSES.contains(entity.getClass())) {
            log.debug("PostUpdateEventListener onPostUpdate got entity of type {} but that is an ignored class", entity.getClass().getName());
            return;
        }
        if (entity instanceof AbstractHibernateEntity) {
            log.debug("PostUpdateEventListener onPostUpdate got entity of type {} ID {}", entity.getClass().getName(), ((AbstractHibernateEntity) entity).getId());
        } else {
            log.debug("PostUpdateEventListener onPostUpdate got entity of type {}", entity.getClass().getName());
        }
        updateHandlerMethods.stream()
                            .filter(method -> method.matches(event))
                            .forEach(method -> method.handleEvent(event));
    }

    @Override
    public void onPostDelete(final PostDeleteEvent event) {
        final Object entity = event.getEntity();
        if (IGNORED_ENTITY_CLASSES.contains(entity.getClass())) {
            log.debug("PostDeleteEventListener onPostDelete got entity of type {} but that is an ignored class", entity.getClass().getName());
            return;
        }
        if (entity instanceof AbstractHibernateEntity) {
            log.debug("PostDeleteEventListener onPostDelete got entity of type {} ID {}", entity.getClass().getName(), ((AbstractHibernateEntity) entity).getId());
        } else {
            log.debug("PostDeleteEventListener onPostDelete got entity of type {}", entity.getClass().getName());
        }
        deleteHandlerMethods.stream()
                            .filter(method -> method.matches(event))
                            .forEach(method -> method.handleEvent(event));
    }

    @Override
    public boolean requiresPostCommitHanding(final EntityPersister persister) {
        return false;
    }
}
