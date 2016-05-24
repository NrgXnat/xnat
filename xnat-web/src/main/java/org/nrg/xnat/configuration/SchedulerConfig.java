package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.NrgEventService;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xdat.preferences.SiteConfigPreferenceEvent;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.security.ResetEmailRequests;
import org.nrg.xnat.security.ResetFailedLogins;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
//    @Bean
//    public TriggerTask disableInactiveUsers() throws SiteConfigurationException {
//        try {
//            final DisableInactiveUsers task = new DisableInactiveUsers(_preferences.getInactivityBeforeLockout(), (int) SiteConfigPreferences.convertPGIntervalToSeconds(_preferences.getMaxFailedLoginsLockoutDuration()));
//            return new TriggerTask(task, new CronTrigger(_preferences.getInactivityBeforeLockoutSchedule()));
//        } catch (SQLException e) {
//            // This isn't a real thing: PGInterval doesn't actually access the database. But just to make everyone happy...
//            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "This really shouldn't happen.", e);
//        }
//    }

//    @Bean
//    public TriggerTask resetFailedLogins() throws SiteConfigurationException {
//        return new TriggerTask(new ResetFailedLogins(_template, _preferences.getMaxFailedLoginsLockoutDuration()), new PeriodicTrigger(900000));
//    }

    @Bean
    public TriggerTask resetEmailRequests() {
        return new TriggerTask(new ResetEmailRequests(_emailRequestLogService), new PeriodicTrigger(900000));
    }
//
//    @Bean
//    public TriggerTask clearExpiredAliasTokens() throws SiteConfigurationException {
//        return new TriggerTask(new ClearExpiredAliasTokens(_template), new Trigger() {
//            @Override public Date nextExecutionTime(TriggerContext triggerContext) {
//                Calendar nextExecutionTime =  new GregorianCalendar();
//                Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
//                nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
//                long expirationInterval = XDAT.getSiteConfigPreferences().getAliasTokenTimeout();
//                if(expirationInterval<120){//Check every minute if interval is 2 hours or less
//                    nextExecutionTime.add(Calendar.MINUTE, 1);
//                }
//                else if(expirationInterval<2880){//Check every hour if interval is 2 days or less
//                    nextExecutionTime.add(Calendar.HOUR, 1);
//                }
//                else{//Check every day
//                    nextExecutionTime.add(Calendar.DAY_OF_MONTH, 1);
//                }
//                return nextExecutionTime.getTime();
//            }
//        });
//    }

    @Bean
    public TriggerTask rebuildSessionXmls() throws SiteConfigurationException {
        final PeriodicTrigger trigger = new PeriodicTrigger(_preferences.getSessionXmlRebuilderRepeat());
        trigger.setInitialDelay(60000);
        return new TriggerTask(new SessionXMLRebuilder(_provider, _preferences.getSessionXmlRebuilderInterval(), _jmsTemplate), trigger);
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
//        taskRegistrar.addTriggerTask(resetFailedLogins());
//        taskRegistrar.addTriggerTask(disableInactiveUsers());
//        taskRegistrar.addTriggerTask(resetEmailRequests());
//        taskRegistrar.addTriggerTask(clearExpiredAliasTokens());
//        taskRegistrar.addTriggerTask(rebuildSessionXmls());
        XDAT.getContextService().getBean(NrgEventService.class).triggerEvent(new SiteConfigPreferenceEvent("aliasTokenTimeout", String.valueOf(XDAT.getSiteConfigPreferences().getAliasTokenTimeout())));
        XDAT.getContextService().getBean(NrgEventService.class).triggerEvent(new SiteConfigPreferenceEvent("inactivityBeforeLockout", String.valueOf(XDAT.getSiteConfigPreferences().getInactivityBeforeLockout())));
        XDAT.getContextService().getBean(NrgEventService.class).triggerEvent(new SiteConfigPreferenceEvent("maxFailedLoginsLockoutDuration", String.valueOf(XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration())));
        for (final TriggerTask triggerTask : _triggerTasks) {
            taskRegistrar.addTriggerTask(triggerTask);
        }
    }

    @Inject
    private EmailRequestLogService _emailRequestLogService;

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    @Inject
    private XnatUserProvider _provider;

    @Inject
    private JmsTemplate _jmsTemplate;

    @Inject
    private InitializerSiteConfiguration _preferences;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Inject
    private List<TriggerTask> _triggerTasks;
}