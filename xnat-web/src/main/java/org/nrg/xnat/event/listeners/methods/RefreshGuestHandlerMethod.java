/*
 * web: org.nrg.xnat.event.listeners.methods.InactivityBeforeLockoutHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.RefreshGuestUser;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToSeconds;

@Component
@Slf4j
@Getter(PROTECTED)
@Setter(PRIVATE)
@Accessors(prefix = "_")
public class RefreshGuestHandlerMethod extends AbstractScheduledXnatPreferenceHandlerMethod {
    @Autowired
    public RefreshGuestHandlerMethod(final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler) {
        super(scheduler, REFRESH_GUEST_FREQUENCY);
        setRefreshGuestFrequency(convertPGIntervalToSeconds(preferences.getRefreshGuestFrequency()));
    }

    @Override
    protected AbstractXnatRunnable getTask() {
        return new RefreshGuestUser();
    }

    @Override
    protected Trigger getTrigger() {
        return new PeriodicTrigger(getRefreshGuestFrequency(), TimeUnit.SECONDS);
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals(REFRESH_GUEST_FREQUENCY, preference)) {
            setRefreshGuestFrequency(convertPGIntervalToSeconds(value));
        }
    }

    private static final String REFRESH_GUEST_FREQUENCY = "refreshGuestFrequency";

    private long _refreshGuestFrequency;
}
