/*
 * web: org.nrg.xnat.event.listeners.methods.InactivityBeforeLockoutHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.security.RefreshGuestUser;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class RefreshGuestHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public RefreshGuestHandlerMethod(final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler, final XnatUserProvider primaryAdminUserProvider) {
        _preferences = preferences;
        _scheduler = scheduler;
        _userProvider = primaryAdminUserProvider;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateRefreshGuest();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateRefreshGuest();
        }
    }

    private void updateRefreshGuest() {
        _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
        for (ScheduledFuture temp : _scheduledRefreshGuest) {
            temp.cancel(false);
        }
        _scheduledRefreshGuest.clear();
        try {
            _scheduledRefreshGuest.add(_scheduler.schedule(new RefreshGuestUser(), new PeriodicTrigger(1000 * (int) SiteConfigPreferences.convertPGIntervalToSeconds(_preferences.getRefreshGuestFrequency()))));
        }catch(Exception e){
            //Ignore
        }

    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("refreshGuestFrequency"));

    private final ArrayList<ScheduledFuture> _scheduledRefreshGuest = new ArrayList<>();

    private final SiteConfigPreferences   _preferences;
    private final ThreadPoolTaskScheduler _scheduler;
    private final XnatUserProvider        _userProvider;
}
