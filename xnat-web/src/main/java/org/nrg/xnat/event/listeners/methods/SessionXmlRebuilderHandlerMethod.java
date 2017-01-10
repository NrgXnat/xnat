/*
 * web: org.nrg.xnat.event.listeners.methods.SessionXmlRebuilderHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class SessionXmlRebuilderHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public SessionXmlRebuilderHandlerMethod(final SiteConfigPreferences preferences, final ThreadPoolTaskScheduler scheduler, final JmsTemplate jmsTemplate, final XnatUserProvider primaryAdminUserProvider, final XnatAppInfo appInfo, final JdbcTemplate jdbcTemplate) {
        _preferences = preferences;
        _scheduler = scheduler;
        _jmsTemplate = jmsTemplate;
        _jdbcTemplate = jdbcTemplate;
        _provider = primaryAdminUserProvider;
        _appInfo = appInfo;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSessionXmlRebuilder();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateSessionXmlRebuilder();
        }
    }

    private void updateSessionXmlRebuilder() {
        _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
        for (ScheduledFuture temp : scheduledXmlRebuilder) {
            temp.cancel(false);
        }
        scheduledXmlRebuilder.clear();
        scheduledXmlRebuilder.add(_scheduler.schedule(new SessionXMLRebuilder(_provider, _appInfo, _jmsTemplate, _jdbcTemplate, _preferences.getSessionXmlRebuilderInterval()), new PeriodicTrigger(_preferences.getSessionXmlRebuilderRepeat())));
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("sessionXmlRebuilderRepeat", "sessionXmlRebuilderInterval"));

    private final ArrayList<ScheduledFuture> scheduledXmlRebuilder = new ArrayList<>();

    private final SiteConfigPreferences   _preferences;
    private final ThreadPoolTaskScheduler _scheduler;
    private final JmsTemplate             _jmsTemplate;
    private final JdbcTemplate            _jdbcTemplate;
    private final XnatUserProvider        _provider;
    private       XnatAppInfo             _appInfo;
}
