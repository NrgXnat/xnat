/*
 * web: org.nrg.xnat.event.listeners.methods.MailHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MailHandlerMethod extends AbstractSiteConfigNotificationsPreferenceHandlerMethod {
    @Autowired
    public MailHandlerMethod(final SiteConfigPreferences siteConfigPreferences, final NotificationsPreferences notificationsPreferences, final ChannelRenderer mailRenderer) {
        _siteConfigPreferences = siteConfigPreferences;
        _notificationsPreferences = notificationsPreferences;
        _mailRenderer = mailRenderer;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateMail();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateMail();
        }
    }

    private void updateMail(){
		try {
            ((NrgMailChannelRenderer)_mailRenderer).setFromAddress(_siteConfigPreferences.getAdminEmail());
            ((NrgMailChannelRenderer)_mailRenderer).setSubjectPrefix(_notificationsPreferences.getEmailPrefix());
		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(MailHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("emailPrefix", "adminEmail"));

    private final SiteConfigPreferences _siteConfigPreferences;
    private final NotificationsPreferences _notificationsPreferences;
    private final ChannelRenderer _mailRenderer;
}
