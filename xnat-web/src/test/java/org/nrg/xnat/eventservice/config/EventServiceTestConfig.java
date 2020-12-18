package org.nrg.xnat.eventservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.services.ContextService;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.nrg.prefs.services.impl.DefaultNrgPreferenceService;
import org.nrg.prefs.services.impl.hibernate.HibernatePreferenceService;
import org.nrg.prefs.services.impl.hibernate.HibernateToolService;
import org.nrg.prefs.transformers.PreferenceTransformer;
import org.nrg.test.utils.TestBeans;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.eventservice.actions.EventServiceLoggingAction;
import org.nrg.xnat.eventservice.actions.TestAction;
import org.nrg.xnat.eventservice.daos.EventSubscriptionEntityDao;
import org.nrg.xnat.eventservice.daos.SubscriptionDeliveryEntityDao;
import org.nrg.xnat.eventservice.entities.*;
import org.nrg.xnat.eventservice.events.*;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.listeners.TestDefaultEventServiceListener;
import org.nrg.xnat.eventservice.services.*;
import org.nrg.xnat.eventservice.services.impl.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;
import reactor.bus.EventBus;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@Import({HibernateConfig.class, ObjectMapperConfig.class, SerializerConfig.class})
@ComponentScan({"org.nrg.prefs.repositories", "org.nrg.prefs.transformers"})
public class EventServiceTestConfig {
    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver(preferenceService(), TestBeans.getDefaultTestSiteMap());
    }

    @Bean
    public ToolService toolService() {
        return new HibernateToolService();
    }

    @Bean
    public PreferenceService preferenceService() {
        return new HibernatePreferenceService(toolService());
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public NrgPreferenceService nrgPreferenceService(final List<PreferenceTransformer> transformers, final Map<String, PreferenceEntityResolver> resolvers) {
        return new DefaultNrgPreferenceService(toolService(), preferenceService(), transformers, resolvers);
    }

    @Bean
    public EventService eventService(EventSubscriptionEntityService subscriptionService, EventBus eventBus,
                                     EventServiceComponentManager componentManager,
                                     ActionManager actionManager,
                                     SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                                     UserManagementServiceI userManagementService,
                                     EventPropertyService eventPropertyService,
                                     ObjectMapper mapper,
                                     EventServicePrefsBean mockEventServicePrefsBean) {
        return new EventServiceImpl(subscriptionService, eventBus, componentManager, actionManager, subscriptionDeliveryEntityService, userManagementService, eventPropertyService, mapper, mockEventServicePrefsBean);
    }

    @Bean
    public EventService mockEventService(EventSubscriptionEntityService subscriptionService, EventBus eventBus,
                                         EventServiceComponentManager componentManager,
                                         ActionManager actionManager,
                                         SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService,
                                         UserManagementServiceI userManagementService,
                                         EventPropertyService eventPropertyService,
                                         ObjectMapper mapper,
                                         EventServicePrefsBean mockEventServicePrefsBean) {
        return new EventServiceImpl(subscriptionService, eventBus, componentManager, actionManager, mockSubscriptionDeliveryEntityService, userManagementService, eventPropertyService, mapper, mockEventServicePrefsBean);
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
    public EventServicePrefsBean mockEventServicePrefsBean() {
        return Mockito.mock(EventServicePrefsBean.class);
    }


    @Bean
    public SubscriptionDeliveryEntityService subscriptionDeliveryEntityService(final @Lazy EventService eventService, final @Lazy EventSubscriptionEntityService eventSubscriptionService) {
        return new SubscriptionDeliveryEntityServiceImpl(eventService, eventSubscriptionService);
    }

    @Bean
    public SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService() {
        return Mockito.mock(SubscriptionDeliveryEntityService.class);
    }

    @Bean
    public TestDefaultEventServiceListener testListener() {
        return new TestDefaultEventServiceListener();
    }

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
    public EventServiceActionProvider eventServiceLoggingAction() {
        return new EventServiceLoggingAction();
    }

    @Bean
    public EventServiceLoggingAction mockEventServiceLoggingAction() {
        return Mockito.mock(EventServiceLoggingAction.class);
    }

    @Bean
    public ActionManager actionManager(EventServiceComponentManager componentManager,
                                       SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                                       EventPropertyService eventPropertyService) {
        return new ActionManagerImpl(componentManager, subscriptionDeliveryEntityService, eventPropertyService);
    }

    @Bean
    public ActionManager mockActionManager(EventServiceComponentManager mockComponentManager,
                                           SubscriptionDeliveryEntityService mockSubscriptionDeliveryEntityService,
                                           EventPropertyService eventPropertyService) {
        return new ActionManagerImpl(mockComponentManager, mockSubscriptionDeliveryEntityService, eventPropertyService);
    }

    @Bean
    public SubscriptionDeliveryEntityDao subscriptionDeliveryEntityDao() {
        return new SubscriptionDeliveryEntityDao();
    }

    @Bean
    public List<EventServiceActionProvider> actionProviders() {
        return null;
    }

    @Bean
    public EventBus eventBus() {
        return EventBus.create();
    }

    @Bean
    public EventSubscriptionEntityDao eventSubscriptionDao() {
        return new EventSubscriptionEntityDao();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource, @Qualifier("hibernateProperties") final Properties properties) {
        final LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setAnnotatedClasses(
                SubscriptionEntity.class,
                EventServiceFilterEntity.class,
                SubscriptionDeliveryEntity.class,
                TimedEventStatusEntity.class,
                EventServicePayloadEntity.class,
                TriggeringEventEntity.class);
        return bean;
    }

    @Bean
    public ResourceTransactionManager transactionManager(final SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public EventServiceComponentManager mockComponentManager() {
        return Mockito.mock(EventServiceComponentManager.class);
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public EventServiceComponentManager componentManager(final List<EventServiceListener> eventListeners,
                                                         final List<EventServiceActionProvider> actionProviders) {
        return new EventServiceComponentManagerImpl(eventListeners, actionProviders);
    }

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }

    @Bean
    public EventPropertyService eventPropertyService(EventServiceComponentManager componentManager, ObjectMapper mapper) {
        return new EventPropertyServiceImpl(componentManager, mapper);
    }

    //** Events **//
    @Bean
    public ProjectEvent projectCreatedEvent() {
        return new ProjectEvent();
    }

    @Bean
    public SubjectEvent subjectCreatedEvent() {
        return new SubjectEvent();
    }

    @Bean
    public ScanEvent scanArchiveEvent() {
        return new ScanEvent();
    }

    @Bean
    public SessionEvent sessionArchiveEvent() {
        return new SessionEvent();
    }

    @Bean
    public WorkflowStatusChangeEvent workflowStatusChangeEvent() {
        return new WorkflowStatusChangeEvent();
    }

    @Bean
    public ImageAssessorEvent imageAssessorEvent() { return new ImageAssessorEvent();}

    @Bean
    public SampleEvent sampleEvent() { return new SampleEvent();}

}
