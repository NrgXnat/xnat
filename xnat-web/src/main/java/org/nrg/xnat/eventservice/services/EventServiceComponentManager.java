package org.nrg.xnat.eventservice.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;

import java.util.List;

public interface EventServiceComponentManager {

    List<EventServiceEvent> getInstalledEvents();
    EventServiceEvent getEvent(String id);

    List<EventServiceListener> getInstalledListeners();
    EventServiceListener getListener(String listenerClassName);

    List<EventServiceActionProvider> getActionProviders();

    XnatModelObject getModelObject(Object object, UserI user);
}
