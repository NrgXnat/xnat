/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractNotificationsPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.prefs.events.AbstractPreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractNotificationsPreferenceHandlerMethod extends AbstractPreferenceHandlerMethod {
    @Override
    public List<String> getToolIds() {
        return new ArrayList<>(Collections.singletonList(NotificationsPreferences.NOTIFICATIONS_TOOL_ID));
    }
}
