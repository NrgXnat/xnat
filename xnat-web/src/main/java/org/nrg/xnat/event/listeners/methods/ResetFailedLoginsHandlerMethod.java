/*
 * web: org.nrg.xnat.event.listeners.methods.ResetFailedLoginsHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.ResetFailedLogins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class ResetFailedLoginsHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public ResetFailedLoginsHandlerMethod(final SiteConfigPreferences preferences, final JdbcTemplate template, final ThreadPoolTaskScheduler scheduler) {
        _preferences = preferences;
        _template = template;
        _scheduler = scheduler;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateResetFailedLogins();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateResetFailedLogins();
        }
    }

    private void updateResetFailedLogins() {
        _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
        _scheduler.getScheduledThreadPoolExecutor().getQueue().iterator();
        for (ScheduledFuture temp : scheduledResetFailedLogins) {
            temp.cancel(false);
        }
        scheduledResetFailedLogins.clear();
        scheduledResetFailedLogins.add(_scheduler.schedule(new ResetFailedLogins(_template, _preferences), new CronTrigger(_preferences.getResetFailedLoginsSchedule())));
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("maxFailedLoginsLockoutDuration", "resetFailedLoginsSchedule"));

    private final ArrayList<ScheduledFuture> scheduledResetFailedLogins = new ArrayList<>();

    private final SiteConfigPreferences   _preferences;
    private final JdbcTemplate            _template;
    private final ThreadPoolTaskScheduler _scheduler;
}
