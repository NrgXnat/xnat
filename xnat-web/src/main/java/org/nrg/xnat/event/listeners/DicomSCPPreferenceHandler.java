/*
 * web: org.nrg.xnat.event.listeners.SiteConfigPreferenceHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.prefs.events.AbstractPreferenceHandler;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class DicomSCPPreferenceHandler extends AbstractPreferenceHandler<PreferenceEvent> {
    @Inject
    public DicomSCPPreferenceHandler(final EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public String getToolId() {
        return DicomSCPManager.TOOL_ID;
    }

    @Override
    public void setToolId(final String toolId) {
        // Nothing to see here...
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
