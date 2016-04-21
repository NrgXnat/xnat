package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.schedule.TriggerTaskProxy;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.initialization.InitializerSiteConfiguration;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.security.ResetEmailRequests;
import org.nrg.xnat.security.ResetFailedLogins;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Bean
    public TriggerTaskProxy disableInactiveUsers() throws SiteConfigurationException {
        return new TriggerTaskProxy(new DisableInactiveUsers(_preferences.getInactivityBeforeLockout()), _preferences.getInactivityBeforeLockoutSchedule());
    }

    @Bean
    public TriggerTaskProxy resetFailedLogins() throws SiteConfigurationException {
        return new TriggerTaskProxy(new ResetFailedLogins(_dataSource), _preferences.getMaxFailedLoginsLockoutDuration());
    }

    @Bean
    public TriggerTaskProxy resetEmailRequests() {
        return new TriggerTaskProxy(new ResetEmailRequests(_emailRequestLogService), 900000);
    }

    @Bean
    public TriggerTaskProxy clearExpiredAliasTokens() throws SiteConfigurationException {
        return new TriggerTaskProxy(new ClearExpiredAliasTokens(_dataSource, _preferences.getAliasTokenTimeout()), 3600000);
    }

    @Bean
    public TriggerTaskProxy rebuildSessionéXmls() throws SiteConfigurationException {
        return new TriggerTaskProxy(new SessionXMLRebuilder(_provider, _preferences.getSessionXmlRebuilderInterval(), _jmsTemplate), _preferences.getSessionXmlRebuilderRepeat(), 60000);
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
        for (final TriggerTaskProxy triggerTask : _triggerTasks) {
            taskRegistrar.addTriggerTask(triggerTask);
        }
    }

    @Inject
    private DataSource _dataSource;

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
    private List<TriggerTaskProxy> _triggerTasks;
}