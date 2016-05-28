package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class AliasTokenPreferenceHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateAliasTokenTimeout();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateAliasTokenTimeout();
        }
    }

    private void updateAliasTokenTimeout() {
        try {
            for (final ScheduledFuture future : _timeouts) {
                future.cancel(false);
            }
            _timeouts.clear();
            _timeouts.add(_scheduler.schedule(new ClearExpiredAliasTokens(_template), new CronTrigger(XDAT.getSiteConfigPreferences().getAliasTokenTimeoutSchedule())));
        } catch (Exception e1) {
            _log.error("", e1);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(AliasTokenPreferenceHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("aliasTokenTimeout", "aliasTokenTimeoutSchedule"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    private ArrayList<ScheduledFuture> _timeouts = new ArrayList<>();

    @Autowired
    @Qualifier("taskScheduler")
    private ThreadPoolTaskScheduler _scheduler;
}
