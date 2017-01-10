/*
 * web: org.nrg.xnat.event.listeners.SiteConfigPreferenceHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import org.nrg.prefs.events.AbstractPreferenceHandler;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class SiteConfigPreferenceHandler extends AbstractPreferenceHandler<PreferenceEvent> {
    private       String                        _toolId  = SiteConfigPreferences.SITE_CONFIG_TOOL_ID;
    private final List<PreferenceHandlerMethod> _methods = new ArrayList<>();

    @Inject
    public SiteConfigPreferenceHandler(final EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public String getToolId() {
        return _toolId;
    }

    @Override
    public void setToolId(String toolId) {
        _toolId = toolId;
    }

    @Override
    public List<PreferenceHandlerMethod> getMethods() {
        return _methods;
    }

    @Override
    public void addMethod(PreferenceHandlerMethod method) {
        _methods.add(method);
    }
}
