/*
 * web: org.nrg.xnat.event.listeners.methods.MailHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MailHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public MailHandlerMethod(final ChannelRenderer mailRenderer) {
        super("emailPrefix", "adminEmail");
        _mailRenderer = mailRenderer;
    }

    @Override
    public List<String> getToolIds() {
        return TOOL_IDS;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case ADMIN_EMAIL:
                ((NrgMailChannelRenderer) _mailRenderer).setFromAddress(value);
                break;

            case EMAIL_PREFIX:
                ((NrgMailChannelRenderer) _mailRenderer).setSubjectPrefix(value);
                break;
        }
    }

    private static final List<String> TOOL_IDS     = Arrays.asList(NotificationsPreferences.NOTIFICATIONS_TOOL_ID, SiteConfigPreferences.SITE_CONFIG_TOOL_ID);
    private static final String       EMAIL_PREFIX = "emailPrefix";
    private static final String       ADMIN_EMAIL  = "adminEmail";

    private final ChannelRenderer _mailRenderer;
}
