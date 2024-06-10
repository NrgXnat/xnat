/*
 * web: org.nrg.xnat.event.listeners.methods.ResetFailedLoginsHandlerMethod
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
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.ResetFailedLogins;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Component
@Slf4j
@Getter(PROTECTED)
@Setter(PRIVATE)
@Accessors(prefix = "_")
public class ResetFailedLoginsHandlerMethod extends AbstractScheduledXnatPreferenceHandlerMethod {
    @Autowired
    public ResetFailedLoginsHandlerMethod(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate template, final ThreadPoolTaskScheduler scheduler) {
        super(scheduler, MAX_FAILED_LOGINS, LOCKOUT_DURATION, SCHEDULE);

        _template = template;

        setMaxFailedLogins(preferences.getMaxFailedLogins());
        setMaxFailedLoginsLockoutDuration(preferences.getMaxFailedLoginsLockoutDuration());
        setResetFailedLoginsSchedule(preferences.getResetFailedLoginsSchedule());
    }

    @Override
    protected AbstractXnatRunnable getTask() {
        return new ResetFailedLogins(getTemplate(), getMaxFailedLogins(), getMaxFailedLoginsLockoutDuration());
    }

    @Override
    protected Trigger getTrigger() {
        return new CronTrigger(getResetFailedLoginsSchedule());
    }

    /**
     * Updates the value for the specified preference according to the preference type.
     *
     * @param preference The preference to set.
     * @param value      The value to set.
     */
    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        log.debug("Found preference {} that this handler can handle, setting value to {}", preference, value);
        switch (preference) {
            case MAX_FAILED_LOGINS:
                setMaxFailedLogins(getMaxFailedLogins());
                break;

            case LOCKOUT_DURATION:
                setMaxFailedLoginsLockoutDuration(getMaxFailedLoginsLockoutDuration());
                break;

            case SCHEDULE:
                setResetFailedLoginsSchedule(getResetFailedLoginsSchedule());
                break;
        }
    }

    public static final String MAX_FAILED_LOGINS = "maxFailedLogins";
    public static final String LOCKOUT_DURATION  = "maxFailedLoginsLockoutDuration";
    public static final String SCHEDULE          = "resetFailedLoginsSchedule";

    private final NamedParameterJdbcTemplate _template;

    private int    _maxFailedLogins;
    private String _maxFailedLoginsLockoutDuration;
    private String _resetFailedLoginsSchedule;
}
