/*
 * web: org.nrg.xnat.event.listeners.methods.AliasTokenPreferenceHandlerMethod
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
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AliasTokenPreferenceHandlerMethod extends AbstractScheduledXnatPreferenceHandlerMethod {
    @Autowired
    public AliasTokenPreferenceHandlerMethod(final AliasTokenService service, final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler) {
        super(scheduler, SCHEDULE);

        _service = service;
        _siteConfigPreferences = preferences;

        setAliasTokenTimeoutSchedule(preferences.getAliasTokenTimeoutSchedule());
    }

    @Override
    protected AbstractXnatRunnable getTask() {
        return new ClearExpiredAliasTokens(getService(), getSiteConfigPreferences());
    }

    @Override
    protected Trigger getTrigger() {
        return new CronTrigger(getAliasTokenTimeoutSchedule());
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
        if (!SCHEDULE.equals(preference)) {
            return;
        }
        setAliasTokenTimeoutSchedule(value);
    }

    public static final String SCHEDULE = "aliasTokenTimeoutSchedule";

    private final AliasTokenService _service;
    private final SiteConfigPreferences _siteConfigPreferences;
    private String _aliasTokenTimeoutSchedule;
}
