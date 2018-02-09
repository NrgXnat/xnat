package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Properties;

// ** Extend this class to implement a Reactor Event and Listener in one class ** //
@Service
public abstract class SimpleEventServiceEvent<EventObjectT>
        implements EventServiceEvent<EventObjectT>{

    UserI eventUser;
    EventObjectT object;

    @Autowired
    EventService eventService;

    public SimpleEventServiceEvent(){};

    public SimpleEventServiceEvent(final EventObjectT object, final UserI eventUser) {
        this.object = object;
        this.eventUser = eventUser;
    }

    @Override
    public String getId() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public  EventObjectT getObject() {
        return object;
    }

    @Override
    public String getObjectClass() {
        return getObject() == null ? null : getObject().getClass().getCanonicalName();
    }

    public static SimpleEventServiceEvent createFromResource(Resource resource)
            throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        SimpleEventServiceEvent event = null;
        final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
        Class<?> clazz = Class.forName(properties.get(XnatEventServiceEvent.EVENT_CLASS).toString());
        if (SimpleEventServiceEvent.class.isAssignableFrom(clazz) &&
                !clazz.isInterface() &&
                !Modifier.isAbstract(clazz.getModifiers())) {
            try {
                event = (SimpleEventServiceEvent) clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException e){
              throw new NoSuchMethodException("Can't find default constructor for " + clazz.getName());
            }
        }
        return event;
    }

}
