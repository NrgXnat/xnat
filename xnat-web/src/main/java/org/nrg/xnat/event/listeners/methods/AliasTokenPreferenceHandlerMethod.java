/*
 * web: org.nrg.xnat.event.listeners.methods.AliasTokenPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class AliasTokenPreferenceHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public AliasTokenPreferenceHandlerMethod(final AliasTokenService service, final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler) {
        _service = service;
        _preferences = preferences;
        _scheduler = scheduler;
    }

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
        log.debug("Updating alias token timeout to preference value {}", _preferences.getAliasTokenTimeout());
        _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
        for (final ScheduledFuture future : _timeouts) {
            future.cancel(false);
        }
        _timeouts.clear();
        _timeouts.add(_scheduler.schedule(new ClearExpiredAliasTokens(_service, _preferences), new CronTrigger(_preferences.getAliasTokenTimeoutSchedule())));
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("aliasTokenTimeout", "aliasTokenTimeoutSchedule"));

    private final List<ScheduledFuture> _timeouts = new ArrayList<>();

    private final AliasTokenService       _service;
    private final SiteConfigPreferences   _preferences;
    private final ThreadPoolTaskScheduler _scheduler;
}
