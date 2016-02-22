package org.nrg.xnat.configuration;

import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.security.ResetEmailRequests;
import org.nrg.xnat.security.ResetFailedLogins;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    @Scheduled(cron = "0 0 1 * * ?")
    public void disableInactiveUsers() {
        _log.debug("Now running the disable inactive users process.");
        final DisableInactiveUsers disableInactiveUsers = new DisableInactiveUsers(_inactivityBeforeLockout);
        disableInactiveUsers.call();
    }

    @Scheduled(fixedRateString = "${security.max_failed_logins_lockout_duration:86400000}")
    public void resetFailedLogins() {
        _log.debug("Now running the reset failed logins process.");
        final ResetFailedLogins resetFailedLogins = new ResetFailedLogins(_dataSource);
        resetFailedLogins.call();
    }

    @Scheduled(fixedRate = 900000)
    public void resetEmailRequests() {
        _log.debug("Now running the reset email requests process.");
        final ResetEmailRequests resetEmailRequests = new ResetEmailRequests(_emailRequestLogService);
        resetEmailRequests.call();
    }

    @Scheduled(fixedRate = 3600000)
    public void clearExpiredAliasTokens() {
        _log.debug("Now running the clear expired alias tokens process.");
        final ClearExpiredAliasTokens clearExpiredAliasTokens = new ClearExpiredAliasTokens(_dataSource, _tokenTimeout);
        clearExpiredAliasTokens.call();
    }

    @Scheduled(fixedRateString = "${services.rebuilder.repeat:60000}", initialDelay = 60000)
    public void rebuildSessionXmls() {
        _log.debug("Now running the session rebuild process.");
        final SessionXMLRebuilder sessionXMLRebuilder = new SessionXMLRebuilder(_provider, _interval, _jmsTemplate);
        sessionXMLRebuilder.call();
    }

    private static final Logger _log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Inject
    private DataSource _dataSource;

    @Inject
    private EmailRequestLogService _emailRequestLogService;

    @Inject
    private XnatUserProvider _provider;

    @Inject
    private JmsTemplate _jmsTemplate;

    @Value("${security.inactivity_before_lockout:31556926}")
    private int _inactivityBeforeLockout;

    @Value("${security.token_timeout:2 days}")
    private String _tokenTimeout;

    @Value("${services.rebuilder.interval:5}")
    private int _interval;
}