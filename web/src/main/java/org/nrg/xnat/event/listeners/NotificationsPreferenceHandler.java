/*
 * web: org.nrg.xnat.event.listeners.NotificationsPreferenceHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.prefs.events.AbstractPreferenceHandler;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NotificationsPreferenceHandler extends AbstractPreferenceHandler<PreferenceEvent> {
    @Inject
    public NotificationsPreferenceHandler(final EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public String getToolId() {
        return NotificationsPreferences.NOTIFICATIONS_TOOL_ID;
    }

    @Override
    public void setToolId(String toolId) {
        log.info("Someone tried to set my tool ID to {}, but the tool ID can't be changed.");
    }

    @Override
    public List<PreferenceHandlerMethod> getMethods() {
        return _methods;
    }

    @Override
    public void addMethod(PreferenceHandlerMethod method) {
        _methods.add(method);
    }

    private final List<PreferenceHandlerMethod> _methods = new ArrayList<>();
}
