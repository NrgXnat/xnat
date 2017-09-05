/*
 * web: org.nrg.xnat.event.listeners.methods.AliasTokenPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.prefs.events.AbstractPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.nrg.dcm.scp.DicomSCPManager.TOOL_ID;

@Component
public abstract class AbstractDicomSCPPreferenceHandlerMethod extends AbstractPreferenceHandlerMethod {
    @Autowired
    public AbstractDicomSCPPreferenceHandlerMethod(final DicomSCPManager manager) {
        _manager = manager;
    }

    @Override
    public List<String> getToolIds() {
        return TOOLS_IDS;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        return new HashSet<>(preferences);
    }

    protected DicomSCPManager getManager() {
        return _manager;
    }

    private static final List<String> TOOLS_IDS   = Collections.singletonList(TOOL_ID);
    private static final List<String> PREFERENCES = TOOLS_IDS;

    private final DicomSCPManager _manager;
}
