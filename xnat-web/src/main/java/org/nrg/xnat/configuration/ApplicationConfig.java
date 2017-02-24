/*
 * web: org.nrg.xnat.configuration.ApplicationConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.XDATUserMgmtServiceImpl;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.impl.ThemeServiceImpl;
import org.nrg.xnat.initialization.InitializingTask;
import org.nrg.xnat.initialization.InitializingTasksExecutor;
import org.nrg.xnat.preferences.AutomationPreferences;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
import org.nrg.xnat.restlet.XnatRestletExtensions;
import org.nrg.xnat.restlet.XnatRestletExtensionsBean;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.TaskScheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@ComponentScan({"org.nrg.automation.daos", "org.nrg.automation.repositories", "org.nrg.config.daos", "org.nrg.dcm.xnat",
                "org.nrg.dicomtools.filters", "org.nrg.framework.datacache.impl.hibernate",
                "org.nrg.framework.services.impl", "org.nrg.notify.daos", "org.nrg.prefs.repositories",
                "org.nrg.xdat.daos", "org.nrg.xdat.security.validators", "org.nrg.xdat.services.impl.hibernate",
                "org.nrg.xft.daos", "org.nrg.xft.event.listeners", "org.nrg.xft.services",
                "org.nrg.xnat.configuration", "org.nrg.xnat.daos", "org.nrg.xnat.event.listeners",
                "org.nrg.xnat.helpers.merge", "org.nrg.xnat.initialization.tasks",
                "org.nrg.xnat.node", "org.nrg.xnat.task"})
@Import({FeaturesConfig.class, ReactorConfig.class})
@ImportResource("WEB-INF/conf/mq-context.xml")
public class ApplicationConfig {
    @Bean
    public ThemeService themeService() {
        return new ThemeServiceImpl();
    }

    @Bean
    public InitializingTasksExecutor initializingTasksExecutor(final TaskScheduler scheduler, final List<InitializingTask> tasks) {
        return new InitializingTasksExecutor(scheduler, tasks);
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        return new SiteConfigPreferences(preferenceService, eventService, configFolderPaths, initPrefs);
    }

    @Bean
    public NotificationsPreferences notificationsPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        return new NotificationsPreferences(preferenceService, eventService, configFolderPaths, initPrefs);
    }

    @Bean
    public AutomationPreferences automationPreferences(final NrgPreferenceService preferenceService, final NrgEventService service, final ConfigPaths configFolderPaths, final OrderedProperties initPrefs) {
        return new AutomationPreferences(preferenceService, service, configFolderPaths, initPrefs);
    }

    @Bean
    public PluginOpenUrlsPreference pluginOpenUrlsPreference(final NrgPreferenceService preferenceService) {
        return new PluginOpenUrlsPreference(preferenceService);
    }

    @Bean
    public PETTracerUtils petTracerUtils(final ConfigService configService) throws Exception {
        return new PETTracerUtils(configService);
    }

    @Bean
    public UserManagementServiceI userManagementService() {
        // TODO: This should be made to use a preference setting.
        return new XDATUserMgmtServiceImpl();
    }

    // MIGRATION: I'm not even sure this is used, but we need to do away with it in favor of prefs.
    @Bean
    public List<String> propertiesRepositories() {
        return Collections.singletonList("WEB-INF/conf/properties");
    }

    @Bean
    public XnatUserProvider primaryAdminUserProvider(final SiteConfigPreferences preferences) throws SiteConfigurationException {
        return new XnatUserProvider(preferences, "primaryAdminUsername");
    }

    @Bean
    public XnatUserProvider receivedFileUserProvider(final SiteConfigPreferences preferences) throws SiteConfigurationException {
        return new XnatUserProvider(preferences, "receivedFileUser");
    }

    @Bean
    public XnatRestletExtensionsBean xnatRestletExtensionsBean(final List<XnatRestletExtensions> extensions) {
        return new XnatRestletExtensionsBean(extensions);
    }

    @Bean
    public XnatRestletExtensions defaultXnatRestletExtensions() {
        return new XnatRestletExtensions(new HashSet<>(Arrays.asList(new String[] {"org.nrg.xnat.restlet.extensions"})));
    }

    @Bean
    public XnatRestletExtensions extraXnatRestletExtensions() {
        return new XnatRestletExtensions(new HashSet<>(Arrays.asList(new String[] {"org.nrg.xnat.restlet.actions"})));
    }

    @Bean
    public ImporterHandlerPackages importerHandlerPackages() {
        return new ImporterHandlerPackages(new HashSet<>(Arrays.asList(new String[] {"org.nrg.xnat.restlet.actions", "org.nrg.xnat.archive"})));
    }
}
