package org.nrg.xnat.eventservice.services.impl;

import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.CombinedEventServiceEvent;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.xnat.*;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventServiceComponentManagerImpl implements EventServiceComponentManager {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private static final String EVENT_RESOURCE_PATTERN ="classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";

    private List<EventServiceListener> installedListeners;
    private List<EventServiceActionProvider> actionProviders;
    private List<EventServiceEvent> installedEvents;

    @Autowired
    public EventServiceComponentManagerImpl(@Lazy final List<EventServiceListener> installedListeners,
                                            @Lazy final List<EventServiceActionProvider> actionProviders) {
        this.installedListeners = installedListeners;
        this.actionProviders = actionProviders;

        try {
            this.installedEvents = loadInstalledEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<EventServiceEvent> getInstalledEvents() {
        if (installedEvents == null || installedEvents.isEmpty()){
            try {
                installedEvents = loadInstalledEvents();
            } catch (NoSuchMethodException|IOException e) {
                e.printStackTrace();
            }
        }
        return installedEvents;
    }

    @Override
    public org.nrg.xnat.eventservice.events.EventServiceEvent getEvent(@Nonnull String eventId) {
        for(EventServiceEvent event : getInstalledEvents()) {
            if(event != null && eventId.matches(event.getId())) {
                return event;
            }
        }
        return null;
    }

    @Override
    public List<EventServiceListener> getInstalledListeners() { return installedListeners; }

    @Override
    public EventServiceListener getListener(String name) {
        for(EventServiceListener el: installedListeners){
            if(el.getId().contains(name)) {
                return el;
            }
        }
        return null;
    }

    @Override
    public List<EventServiceActionProvider> getActionProviders() {
        return actionProviders;
    }

    public List<EventServiceEvent> loadInstalledEvents() throws IOException, NoSuchMethodException {
        List<EventServiceEvent> events = new ArrayList<>();
        for (final Resource resource : BasicXnatResourceLocator.getResources(EVENT_RESOURCE_PATTERN)) {
            try {
                CombinedEventServiceEvent event = CombinedEventServiceEvent.createFromResource(resource);
                if(event != null) { events.add(event); }
            } catch (IOException |ClassNotFoundException|IllegalAccessException|InvocationTargetException |InstantiationException e) {
                log.error("Exception loading EventClass from " + resource.toString());
                log.error("Possible missing Class Definition");
                log.error(e.getMessage());
            }
        }
        return events;
    }

    @Override
    public XnatModelObject getModelObject(Object object, UserI user) {

        if(XnatImageassessordataI.class.isAssignableFrom(object.getClass())) {
            return new Assessor((XnatImageassessordataI) object);
        }
        else if(XnatProjectdata.class.isAssignableFrom(object.getClass())) {
            return new Project((XnatProjectdata) object);
        }
        else if(XnatResourcecatalog.class.isAssignableFrom(object.getClass())) {
            return new org.nrg.xnat.eventservice.model.xnat.Resource((XnatResourcecatalog) object);
        }
        else if(XnatImagescandata.class.isAssignableFrom(object.getClass())) {
            String imageSessionId = ((XnatImagescandataI) object).getImageSessionId();
            Session session = new Session(imageSessionId, user);
            if(session == null) {
                log.error("User:" + user.getLogin() + " could not load Scan parent Session:" + imageSessionId);
            }
            String sessionUri = session.getUri();
            return new Scan((XnatImagescandataI) object, sessionUri, null);
        }
        else if(XnatImagesessiondataI.class.isAssignableFrom(object.getClass())) {
            return new Session((XnatImagesessiondataI) object);
        }
        else if(XnatSubjectdataI.class.isAssignableFrom(object.getClass())) {
            return new Subject((XnatSubjectdataI) object);
        }
        return null;
    }

    private SimpleEvent toPojo(@Nonnull EventServiceEvent event) {
        return SimpleEvent.builder()
                .id(event.getId() == null ? "" : event.getId())
                .listenerService(
                        event instanceof EventServiceListener
                                ? ((EventServiceListener) event).getClass().getName()
                                : "")
                .displayName(event.getDisplayName() == null ? "" : event.getDisplayName())
                .description(event.getDescription() == null ? "" : event.getDescription())
                .payloadClass(event.getObjectClass() == null ? "" : event.getObjectClass())
                .xnatType(event.getPayloadXnatType() == null ? "" : event.getPayloadXnatType())
                .isXsiType(event.isPayloadXsiType() == null ? false : event.isPayloadXsiType())
                .build();
    }




}
