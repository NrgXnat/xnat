package org.nrg.xnat.eventservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.eventservice.actions.EventServiceLoggingAction;
import org.nrg.xnat.eventservice.actions.TestAction;
import org.nrg.xnat.eventservice.daos.EventSubscriptionEntityDao;
import org.nrg.xnat.eventservice.daos.SubscriptionDeliveryEntityDao;
import org.nrg.xnat.eventservice.entities.EventServiceFilterEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.events.*;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.listeners.TestListener;
import org.nrg.xnat.eventservice.services.*;
import org.nrg.xnat.eventservice.services.impl.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;
import reactor.bus.EventBus;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
@Import({HibernateConfig.class, ObjectMapperConfig.class})
public class EventServiceTestConfig {


    @Bean
    public EventService eventService(final EventSubscriptionEntityService subscriptionService,
                                     final EventBus eventBus,
                                     final ContextService contextService,
                                     final EventServiceComponentManager componentManager,
                                     final ActionManager actionManager,
                                     final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService){
        return new EventServiceImpl(subscriptionService, eventBus, contextService, componentManager, actionManager, subscriptionDeliveryEntityService);
    }

    @Bean
    public EventService mockEventService(final EventSubscriptionEntityService subscriptionService,
                                     final EventBus eventBus,
                                     final ContextService contextService,
                                     final EventServiceComponentManager mockComponentManager,
                                     final ActionManager mockActionManager,
                                     final SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService){
        return new EventServiceImpl(subscriptionService, eventBus, contextService, mockComponentManager, mockActionManager, mockSubscriptionDeliveryEntityService);
    }

    @Bean
    public EventSubscriptionEntityService eventSubscriptionService(final @Lazy EventService eventService,
                                                                   final ObjectMapper objectMapper,
                                                                   final EventBus eventBus,
                                                                   final ContextService contextService,
                                                                   final ActionManager actionManager,
                                                                   final EventServiceComponentManager componentManager,
                                                                   final UserManagementServiceI userManagementService,
                                                                   final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        return new EventSubscriptionEntityServiceImpl(eventBus, contextService, actionManager, componentManager, eventService, objectMapper, userManagementService, subscriptionDeliveryEntityService);
    }

    @Bean
    public SubscriptionDeliveryEntityService subscriptionDeliveryEntityService(final @Lazy EventService eventService){
        return new SubscriptionDeliveryEntityServiceImpl(eventService);
    }

    @Bean
    public SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService() {return Mockito.mock(SubscriptionDeliveryEntityService.class);}

    @Bean
    public TestListener testListener() {return new TestListener(); }

    @Bean
    public TestCombinedEvent testCombinedEvent() {return new TestCombinedEvent(); }

    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }

    @Bean
    public EventServiceActionProvider testAction(SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        return new TestAction(subscriptionDeliveryEntityService);
    }

    @Bean
    public EventServiceActionProvider eventServiceLoggingAction() {return new EventServiceLoggingAction(); }

    @Bean
    public EventServiceLoggingAction mockEventServiceLoggingAction() { return Mockito.mock(EventServiceLoggingAction.class); }

    @Bean
    public ActionManager actionManager(EventServiceComponentManager componentManager,
                                       SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        return new ActionManagerImpl(componentManager, subscriptionDeliveryEntityService);
    }

    @Bean
    public ActionManager mockActionManager(EventServiceComponentManager mockComponentManager,
                                           SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService) {
        return new ActionManagerImpl(mockComponentManager, mockSubscriptionDeliveryEntityService);
    }

    @Bean
    public SubscriptionDeliveryEntityDao subscriptionDeliveryEntityDao() {return new SubscriptionDeliveryEntityDao();}

    @Bean
    public List<EventServiceActionProvider> actionProviders () {return null;}

    @Bean
    public EventBus eventBus(ContextService contextService) {return EventBus.create(); }

    @Bean
    public EventSubscriptionEntityDao eventSubscriptionDao() { return new EventSubscriptionEntityDao();  }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource, @Qualifier("hibernateProperties") final Properties properties) {
        final LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setAnnotatedClasses(
                SubscriptionEntity.class,
                EventServiceFilterEntity.class,
                SubscriptionDeliveryEntity.class,
                TimedEventStatusEntity.class);
        return bean;
    }

    @Bean
    public ResourceTransactionManager transactionManager(final SessionFactory sessionFactory) throws Exception {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public EventServiceComponentManager mockComponentManager() { return Mockito.mock(EventServiceComponentManager.class); }

    @Bean
    public EventServiceComponentManager componentManager(final List<EventServiceListener> eventListeners,
                                                         final List<EventServiceActionProvider> actionProviders) {
        return new EventServiceComponentManagerImpl(eventListeners, actionProviders);
    }

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }



    //** Combined Events/Listener **//
    @Bean
    public ProjectCreatedEvent projectCreatedEvent() {return new ProjectCreatedEvent();}
    @Bean
    public SubjectCreatedEvent subjectCreatedEvent() {return new SubjectCreatedEvent();}
    @Bean
    public ScanArchiveEvent scanArchiveEvent() {return new ScanArchiveEvent();}
    @Bean
    public SessionArchiveEvent sessionArchiveEvent() {return new SessionArchiveEvent();}
}
