/*
 * web: org.nrg.xnat.event.listeners.methods.InactivityBeforeLockoutHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PROTECTED;
import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToIntSeconds;

@Component
@Slf4j
@Getter(PROTECTED)
@Setter(AccessLevel.PRIVATE)
@Accessors(prefix = "_")
public class InactivityBeforeLockoutHandlerMethod extends AbstractScheduledXnatPreferenceHandlerMethod {
    @Autowired
    public InactivityBeforeLockoutHandlerMethod(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate template, final ThreadPoolTaskScheduler scheduler) {
        super(scheduler, INACTIVITY_BEFORE_LOCKOUT, LOCKOUT_DURATION, INACTIVITY_BEFORE_LOCKOUT_SCHEDULE);

        _template = template;

        final String inactivityBeforeLockout         = preferences.getInactivityBeforeLockout();
        final String maxFailedLoginsLockoutDuration  = preferences.getMaxFailedLoginsLockoutDuration();
        final String inactivityBeforeLockoutSchedule = preferences.getInactivityBeforeLockoutSchedule();

        log.debug("Initializing InactivityBeforeLockoutHandlerMethod with inactivityBeforeLockout '{}', lockout duration '{}', and scheduled to run '{}'", inactivityBeforeLockout, maxFailedLoginsLockoutDuration, inactivityBeforeLockoutSchedule);

        setInactivityBeforeLockout(convertPGIntervalToIntSeconds(inactivityBeforeLockout));
        setMaxFailedLoginsLockoutDuration(convertPGIntervalToIntSeconds(maxFailedLoginsLockoutDuration));
        setInactivityBeforeLockoutSchedule(inactivityBeforeLockoutSchedule);
    }

    @Override
    protected AbstractXnatRunnable getTask() {
        return new DisableInactiveUsers(getTemplate(), getInactivityBeforeLockout(), getMaxFailedLoginsLockoutDuration());
    }

    @Override
    protected Trigger getTrigger() {
        return new CronTrigger(getInactivityBeforeLockoutSchedule());
    }

    /**
     * Updates the value for the specified preference according to the preference type.
     *
     * @param preference The preference to set.
     * @param value      The value to set.
     */
    protected void handlePreferenceImpl(final String preference, final String value) {
        log.debug("Found preference {} that this handler can handle, setting value to {}", preference, value);
        switch (preference) {
            case INACTIVITY_BEFORE_LOCKOUT:
                setInactivityBeforeLockout(convertPGIntervalToIntSeconds(value));
                break;

            case LOCKOUT_DURATION:
                setMaxFailedLoginsLockoutDuration(convertPGIntervalToIntSeconds(value));
                break;

            case INACTIVITY_BEFORE_LOCKOUT_SCHEDULE:
                setInactivityBeforeLockoutSchedule(value);
                break;
        }
    }

    private static final String INACTIVITY_BEFORE_LOCKOUT          = "inactivityBeforeLockout";
    private static final String LOCKOUT_DURATION                   = "maxFailedLoginsLockoutDuration";
    private static final String INACTIVITY_BEFORE_LOCKOUT_SCHEDULE = "inactivityBeforeLockoutSchedule";

    private final NamedParameterJdbcTemplate _template;

    private int    _inactivityBeforeLockout;
    private int    _maxFailedLoginsLockoutDuration;
    private String _inactivityBeforeLockoutSchedule;
}
