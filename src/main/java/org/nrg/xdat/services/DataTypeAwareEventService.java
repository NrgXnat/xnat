package org.nrg.xdat.services;

import org.nrg.framework.event.EventI;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.XFTItem;

import java.util.Map;

public interface DataTypeAwareEventService extends NrgEventServiceI {
    void triggerEvent(final EventI event);

    void triggerEvent(final String description, final EventI event, final boolean notifyClassListeners);

    void triggerEvent(final String description, final EventI event);

    void triggerEvent(final EventI event, final Object replyTo);

    void triggerXftItemEvent(final String xsiType, final String id, final String action);

    void triggerXftItemEvent(final XFTItem item, final String action);

    void triggerXftItemEvent(final BaseElement baseElement, final String action);

    void triggerXftItemEvent(final BaseElement[] baseElements, final String action);

    void triggerXftItemEvent(final String xsiType, final String id, final String action, final Map<String, ?> properties);

    void triggerUserIEvent(final String username, final String action, final Map<String, ?> properties);

    void triggerXftItemEvent(final XFTItem item, final String action, final Map<String, ?> properties);

    void triggerXftItemEvent(final BaseElement baseElement, final String action, final Map<String, ?> properties);
}
