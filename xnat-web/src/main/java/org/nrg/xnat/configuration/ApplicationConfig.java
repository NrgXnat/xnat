/*
 * web: org.nrg.xnat.configuration.ApplicationConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.XDATUserMgmtServiceImpl;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.impl.ThemeServiceImpl;
import org.nrg.xnat.helpers.prearchive.handlers.DefaultPrearchiveOperationHandlerResolver;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandlerResolver;
import org.nrg.xnat.initialization.InitializingTask;
import org.nrg.xnat.initialization.InitializingTasksExecutor;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.nrg.xnat.processor.importer.ProcessorImporterHandlerA;
import org.nrg.xnat.processor.importer.ProcessorImporterMap;
import org.nrg.xnat.restlet.XnatRestletExtensions;
import org.nrg.xnat.restlet.XnatRestletExtensionsBean;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Configuration
@ComponentScan({"org.nrg.automation.daos", "org.nrg.automation.repositories", "org.nrg.config.daos", "org.nrg.dcm.xnat",
                "org.nrg.dicomtools.filters", "org.nrg.resources", "org.nrg.framework.datacache.impl.hibernate",
                "org.nrg.framework.services.impl", "org.nrg.notify.daos", "org.nrg.prefs.repositories",
                "org.nrg.xdat.daos", "org.nrg.xdat.security.validators", "org.nrg.xdat.services.impl.hibernate", "org.nrg.xdat.services.cache.impl",
                "org.nrg.xft.daos", "org.nrg.xft.event.listeners", "org.nrg.xft.services",
                "org.nrg.xnat.configuration", "org.nrg.xnat.daos", "org.nrg.xnat.event.listeners",
                "org.nrg.xnat.helpers.merge", "org.nrg.xnat.initialization.tasks",
                "org.nrg.xnat.node", "org.nrg.xnat.task", "org.nrg.xnat.preferences", "org.nrg.xnat.processors",
                "org.nrg.xnat.processor.services.impl", "org.nrg.xnat.processor.dao", "org.nrg.xnat.processor.importer"})
@Import({FeaturesConfig.class, ReactorConfig.class})
@EnableCaching
@Getter
@Accessors(prefix = "_")
@Slf4j
public class ApplicationConfig {
    @Autowired
    public void setAsyncOperationsPreferences(final AsyncOperationsPreferences asyncOperationsPreferences) {
        _asyncOperationsPreferences = asyncOperationsPreferences;
    }

    @Autowired
    public void setXnatHome(final Path xnatHome) {
        _xnatHome = xnatHome;
    }

    @Bean
    public ThemeService themeService(final SerializerService serializer, final ServletContext context) {
        return new ThemeServiceImpl(serializer, context);
    }

    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactory().getObject());
    }

    @Bean(name = {"threadPoolExecutorFactoryBean", "executorService"})
    @DependsOn({"xnatHome", "asyncOperationsPreferences"})
    public ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean() throws IOException, InvocationTargetException, IllegalAccessException {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();

        final Path executor = getXnatHome().resolve("../executor.properties");
        if (executor.toFile().exists()) {
            try (final BufferedReader reader = Files.newBufferedReader(executor, StandardCharsets.UTF_8)) {
                final Properties properties = new Properties();
                properties.load(reader);
                final Map<String, String> converted = new HashMap<>();
                for (final String key : properties.stringPropertyNames()) {
                    converted.put(key, properties.getProperty(key));
                }
                BeanUtils.populate(bean, converted);
            }
        } else {
            final int     corePoolSize           = getAsyncOperationsPreferences().getCorePoolSize();
            final boolean allowCoreThreadTimeOut = getAsyncOperationsPreferences().getAllowCoreThreadTimeOut();
            final int     maxPoolSize            = getAsyncOperationsPreferences().getMaxPoolSize();
            final int     keepAliveSeconds       = getAsyncOperationsPreferences().getKeepAliveSeconds();

            log.info("Configuring async task executor with core pool size {}, max pool size {}, keep-alive seconds {}, and allow core thread timeout {}", corePoolSize, maxPoolSize, keepAliveSeconds, allowCoreThreadTimeOut);
            bean.setCorePoolSize(corePoolSize);
            bean.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
            bean.setMaxPoolSize(maxPoolSize);
            bean.setKeepAliveSeconds(keepAliveSeconds);
        }

        return bean;
    }

    @Bean
    public ScheduledExecutorFactoryBean scheduledExecutorFactoryBean() throws IllegalAccessException, IOException, InvocationTargetException {
        final ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
        bean.setRemoveOnCancelPolicy(true);
        bean.setContinueScheduledExecutionAfterException(true);
        bean.setWaitForTasksToCompleteOnShutdown(true);
        bean.setThreadFactory(threadPoolExecutorFactoryBean());
        return bean;
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactory() {
        return new EhCacheManagerFactoryBean() {{
            setConfigLocation(new ClassPathResource("xnat-cache.xml"));
        }};
    }

    @Bean
    public InitializingTasksExecutor initializingTasksExecutor(final TaskScheduler scheduler, final List<InitializingTask> tasks) {
        log.debug("Creating InitializingTasksExecutor bean with a scheduler of type {} and {} tasks", scheduler.getClass().getName(), tasks.size());
        return new InitializingTasksExecutor(scheduler, tasks);
    }

    @Bean(name = {"siteConfigPreferences", "siteConfig"})
    public SiteConfigPreferences siteConfigPreferences(final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        return new SiteConfigPreferences(preferenceService, eventService, configFolderPaths, initPrefs);
    }

    @Bean
    public NotificationsPreferences notificationsPreferences(final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        return new NotificationsPreferences(preferenceService, eventService, configFolderPaths, initPrefs);
    }

    @Bean
    public PETTracerUtils petTracerUtils(final ConfigService configService) {
        return new PETTracerUtils(configService);
    }

    @Bean
    public UserManagementServiceI userManagementService(final NamedParameterJdbcTemplate template) {
        // TODO: This should be made to use a preference setting.
        return new XDATUserMgmtServiceImpl(template);
    }

    // MIGRATION: I'm not even sure this is used, but we need to do away with it in favor of prefs.
    @Bean
    public List<String> propertiesRepositories() {
        return Collections.singletonList("WEB-INF/conf/properties");
    }

    @Bean
    public XnatUserProvider primaryAdminUserProvider(final SiteConfigPreferences preferences) {
        return new XnatUserProvider(preferences, "primaryAdminUsername");
    }

    @Bean
    public XnatUserProvider receivedFileUserProvider(final SiteConfigPreferences preferences) {
        return new XnatUserProvider(preferences, "receivedFileUser");
    }

    @Bean
    public XnatRestletExtensionsBean xnatRestletExtensionsBean(final List<XnatRestletExtensions> extensions) {
        return new XnatRestletExtensionsBean(extensions);
    }

    @Bean
    public XnatRestletExtensions defaultXnatRestletExtensions() {
        return new XnatRestletExtensions(new HashSet<>(Collections.singletonList("org.nrg.xnat.restlet.extensions")));
    }

    @Bean
    public XnatRestletExtensions extraXnatRestletExtensions() {
        return new XnatRestletExtensions(new HashSet<>(Collections.singletonList("org.nrg.xnat.restlet.actions")));
    }

    @Bean
    public ImporterHandlerPackages importerHandlerPackages() {
        return new ImporterHandlerPackages(new HashSet<>(Arrays.asList("org.nrg.xnat.restlet.actions", "org.nrg.xnat.archive")));
    }

    @Bean
    public PrearchiveOperationHandlerResolver prearchiveOperationHandlerResolver(final NrgEventServiceI eventService, final XnatUserProvider receivedFileUserProvider, final DicomInboxImportRequestService importRequestService) {
        return new DefaultPrearchiveOperationHandlerResolver(eventService, receivedFileUserProvider, importRequestService);
    }

    @Bean
    public ProcessorImporterMap processorImporterMap(final List<ProcessorImporterHandlerA> handlers) throws ConfigurationException, IOException, ClassNotFoundException {
        return new ProcessorImporterMap(new HashSet<>(Collections.singletonList("org.nrg.xnat.processor.importer")), handlers);
    }

    private AsyncOperationsPreferences _asyncOperationsPreferences;
    private Path                       _xnatHome;
}
