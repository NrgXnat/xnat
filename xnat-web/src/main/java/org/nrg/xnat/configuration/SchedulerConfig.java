package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.services.NrgEventService;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.security.ResetEmailRequests;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.inject.Inject;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Bean
    public TriggerTask resetEmailRequests() {
        return new TriggerTask(new ResetEmailRequests(_emailRequestLogService), new PeriodicTrigger(900000));
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        _eventService.triggerEvent(new PreferenceEvent("sessionXmlRebuilderRepeat", String.valueOf(XDAT.getSiteConfigPreferences().getSessionXmlRebuilderRepeat())));
        _eventService.triggerEvent(new PreferenceEvent("aliasTokenTimeout", String.valueOf(XDAT.getSiteConfigPreferences().getAliasTokenTimeout())));
        _eventService.triggerEvent(new PreferenceEvent("inactivityBeforeLockout", String.valueOf(XDAT.getSiteConfigPreferences().getInactivityBeforeLockout())));
        _eventService.triggerEvent(new PreferenceEvent("maxFailedLoginsLockoutDuration", String.valueOf(XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration())));
        _eventService.triggerEvent(new PreferenceEvent("emailPrefix", String.valueOf(XDAT.getNotificationsPreferences().getEmailPrefix())));
        _eventService.triggerEvent(new PreferenceEvent("host", String.valueOf(XDAT.getNotificationsPreferences().getHostname())));
        _eventService.triggerEvent(new PreferenceEvent("requireLogin", String.valueOf(XDAT.getSiteConfigPreferences().getRequireLogin())));
        _eventService.triggerEvent(new PreferenceEvent("security.channel", String.valueOf(XDAT.getSiteConfigPreferences().getSecurityChannel())));
        _eventService.triggerEvent(new PreferenceEvent("passwordExpirationType", String.valueOf(XDAT.getSiteConfigPreferences().getPasswordExpirationType())));
        _eventService.triggerEvent(new PreferenceEvent("archivePath", String.valueOf(XDAT.getSiteConfigPreferences().getArchivePath())));
        for (final TriggerTask triggerTask : _triggerTasks) {
            taskRegistrar.addTriggerTask(triggerTask);
        }
    }

    @Inject
    private EmailRequestLogService _emailRequestLogService;

    @Inject
    private XnatUserProvider _provider;

    @Inject
    private JmsTemplate _jmsTemplate;

    @Inject
    private InitializerSiteConfiguration _preferences;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Inject
    private List<TriggerTask> _triggerTasks;

    @Inject
    private NrgEventService _eventService;
}