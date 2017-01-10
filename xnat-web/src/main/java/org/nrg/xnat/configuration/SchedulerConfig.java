/*
 * web: org.nrg.xnat.configuration.SchedulerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.framework.services.NrgEventService;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.ResetEmailRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Bean
    public TriggerTask resetEmailRequests(final EmailRequestLogService service) {
        return new TriggerTask(new ResetEmailRequests(service), new PeriodicTrigger(900000));
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Autowired
    public void setNrgEventService(final NrgEventService service) {
        _service = service;
    }

    @Autowired
    public void setSiteConfigPreferences(final SiteConfigPreferences siteConfigPreferences) {
        _siteConfigPreferences = siteConfigPreferences;
    }

    @Autowired
    public void setNotificationsPreferences(final NotificationsPreferences notificationsPreferences) {
        _notificationsPreferences = notificationsPreferences;
    }

    @Autowired
    public void setTriggerTasks(final List<TriggerTask> tasks) {
        _tasks = tasks;
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());

        _service.triggerEvent(new PreferenceEvent("sessionXmlRebuilderRepeat", String.valueOf(_siteConfigPreferences.getSessionXmlRebuilderRepeat())));
        _service.triggerEvent(new PreferenceEvent("aliasTokenTimeout", String.valueOf(_siteConfigPreferences.getAliasTokenTimeout())));
        _service.triggerEvent(new PreferenceEvent("inactivityBeforeLockout", String.valueOf(_siteConfigPreferences.getInactivityBeforeLockout())));
        _service.triggerEvent(new PreferenceEvent("maxFailedLoginsLockoutDuration", String.valueOf(_siteConfigPreferences.getMaxFailedLoginsLockoutDuration())));
        _service.triggerEvent(new PreferenceEvent("emailPrefix", String.valueOf(_notificationsPreferences.getEmailPrefix())));
        _service.triggerEvent(new PreferenceEvent("smtpHostname", String.valueOf(_notificationsPreferences.getSmtpHostname())));
        _service.triggerEvent(new PreferenceEvent("requireLogin", String.valueOf(_siteConfigPreferences.getRequireLogin())));
        _service.triggerEvent(new PreferenceEvent("securityChannel", String.valueOf(_siteConfigPreferences.getSecurityChannel())));
        _service.triggerEvent(new PreferenceEvent("passwordExpirationType", String.valueOf(_siteConfigPreferences.getPasswordExpirationType())));
        _service.triggerEvent(new PreferenceEvent("archivePath", String.valueOf(_siteConfigPreferences.getArchivePath())));
        _service.triggerEvent(new PreferenceEvent("roleService", String.valueOf(_siteConfigPreferences.getRoleService())));
        _service.triggerEvent(new PreferenceEvent("checksums", String.valueOf(_siteConfigPreferences.getChecksums())));
        _service.triggerEvent(new PreferenceEvent("sitewidePetTracers", String.valueOf(_siteConfigPreferences.getSitewidePetTracers())));

        for (final TriggerTask triggerTask : _tasks) {
            taskRegistrar.addTriggerTask(triggerTask);
        }
    }

    private SiteConfigPreferences    _siteConfigPreferences;
    private NotificationsPreferences _notificationsPreferences;
    private List<TriggerTask>        _tasks;
    private NrgEventService          _service;
}
