package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferenceEvent;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    }

    @Override
    public void handlePreference(final String preference, final String value) {

    }

    private void updateAliasTokenTimeout(SiteConfigPreferenceEvent e){
        try {
            XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
            Iterator<Runnable> iter = XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().getQueue().iterator();

            for(ScheduledFuture temp: scheduledAliasTokenTimeouts){
                temp.cancel(false);
            }

            scheduledAliasTokenTimeouts.add(XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").schedule(new ClearExpiredAliasTokens(_template), new CronTrigger(XDAT.getSiteConfigPreferences().getAliasTokenTimeoutSchedule())));

        } catch (Exception e1) {
            _log.error("", e1);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(AliasTokenPreferenceHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("aliasTokenTimeout", "aliasTokenTimeoutSchedule"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    private              ArrayList<ScheduledFuture> scheduledAliasTokenTimeouts = new ArrayList<>();
}
