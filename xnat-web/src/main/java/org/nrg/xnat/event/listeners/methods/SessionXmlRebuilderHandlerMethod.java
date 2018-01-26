/*
 * web: org.nrg.xnat.event.listeners.methods.SessionXmlRebuilderHandlerMethod
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
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Component
@Slf4j
@Getter(PROTECTED)
@Setter(PRIVATE)
@Accessors(prefix = "_")
public class SessionXmlRebuilderHandlerMethod extends AbstractScheduledXnatPreferenceHandlerMethod {
    @Autowired
    public SessionXmlRebuilderHandlerMethod(final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler, final XnatTaskService taskService, final JmsTemplate jmsTemplate, final XnatUserProvider primaryAdminUserProvider, final XnatAppInfo appInfo, final JdbcTemplate jdbcTemplate) {
        super(scheduler, primaryAdminUserProvider, REPEAT, INTERVAL);

        _taskService = taskService;
        _jmsTemplate = jmsTemplate;
        _jdbcTemplate = jdbcTemplate;
        _appInfo = appInfo;

        setSessionXmlRebuilderInterval(preferences.getSessionXmlRebuilderInterval());
        setSessionXmlRebuilderRepeat(preferences.getSessionXmlRebuilderRepeat());
    }

    @NotNull
    protected AbstractXnatRunnable getTask() {
        return new SessionXMLRebuilder(getUserProvider(), getTaskService(), getAppInfo(), getJmsTemplate(), getJdbcTemplate(), getSessionXmlRebuilderInterval());
    }

    @NotNull
    protected Trigger getTrigger() {
        return new PeriodicTrigger(getSessionXmlRebuilderRepeat());
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
            case REPEAT:
                setSessionXmlRebuilderRepeat(Long.parseLong(value));
                break;

            case INTERVAL:
                setSessionXmlRebuilderInterval(Integer.parseInt(value));
                break;
        }
    }

    private static final String REPEAT   = "sessionXmlRebuilderRepeat";
    private static final String INTERVAL = "sessionXmlRebuilderInterval";

    private final XnatTaskService _taskService;
    private final JmsTemplate     _jmsTemplate;
    private final JdbcTemplate    _jdbcTemplate;
    private final XnatAppInfo     _appInfo;

    private int  _sessionXmlRebuilderInterval;
    private long _sessionXmlRebuilderRepeat;
}
