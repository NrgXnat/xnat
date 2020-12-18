package org.nrg.xnat.eventservice.events;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.UUID;

// ** Extend this class to implement an Event Service Reactor class ** //
@Slf4j
public abstract class AbstractEventServiceEvent<EventObjectT>
        implements EventServiceEvent<EventObjectT> {

    protected String eventUser;
    protected Class objectClass;
    protected UUID eventUUID = UUID.randomUUID();
    protected Long eventCreatedTimestamp;
    protected Enum status = null;
    protected String projectId = null;
    protected String xsiType = null;

    public AbstractEventServiceEvent() {}

    public AbstractEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status) {
        this.objectClass = object.getClass();
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = Date.from(Instant.now()).getTime();
        this.status = status;
        this.projectId = null;
    }

    public AbstractEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status, final String projectId, final String xsiType) {
        this.objectClass = object != null ? object.getClass() : null;
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = Date.from(Instant.now()).getTime();
        this.status = status;
        this.projectId = projectId;
        this.xsiType = xsiType;
    }

    @Override
    public String getType() { return this.getClass().getCanonicalName(); }

    @Override
    public Class getObjectClass() {
        if (objectClass == null) {
            try {
                Type payloadType = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                objectClass = Class.forName(payloadType.getTypeName());
            } catch (Throwable e) {
                if(log.isDebugEnabled()) log.debug("Could not discover payload object class - Event superclass is not parametrized with generic type.");
            }
        }
        return objectClass;
    }

    @Override
    public String getPayloadXnatType() { return xsiType; }

    public void setPayloadXnatType(String xsiType) {this.xsiType = xsiType;}

    @Override
    public String getUser() {
        return eventUser;
    }

    @Override
    public Date getEventTimestamp() {
        return new Date(eventCreatedTimestamp);
    }

    @Override
    public UUID getEventUUID() {
        return eventUUID;
    }

    @Override
    public Enum getCurrentStatus() {
        return status;
    }

    @Override
    public String getProjectId(){
        return projectId;
    }

    @Override
    public Boolean filterablePayload() { return false;}

    @Override
    public Object getPayloadSignatureObject() {return null;}


    @Override
    public List<EventScope> getEventScope() { return Arrays.asList(EventScope.PROJECT, EventScope.SITE); }


    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractEventServiceEvent.class.getSimpleName() + "[", "]")
                .add("eventUser='" + eventUser + "'")
                .add("objectClass=" + objectClass)
                .add("eventUUID=" + eventUUID)
                .add("eventCreatedTimestamp=" + eventCreatedTimestamp)
                .add("status=" + status)
                .add("projectId='" + projectId + "'")
                .add("xsiType='" + xsiType + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventServiceEvent)) return false;
        AbstractEventServiceEvent<?> that = (AbstractEventServiceEvent<?>) o;
        return Objects.equals(eventUser, that.eventUser) &&
                Objects.equals(objectClass, that.objectClass) &&
                Objects.equals(eventUUID, that.eventUUID) &&
                Objects.equals(eventCreatedTimestamp, that.eventCreatedTimestamp) &&
                Objects.equals(status, that.status) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(xsiType, that.xsiType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventUser, objectClass, eventUUID, eventCreatedTimestamp, status, projectId, xsiType);
    }

    public static EventServiceEvent createFromResource(org.springframework.core.io.Resource resource)
            throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        EventServiceEvent event = null;
        final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
        Class<?> clazz = Class.forName(properties.get(XnatEventServiceEvent.EVENT_CLASS).toString());
        if (EventServiceEvent.class.isAssignableFrom(clazz) &&
                !clazz.isInterface() &&
                !Modifier.isAbstract(clazz.getModifiers())) {
            try {
                event = (EventServiceEvent) clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException e){
              throw new NoSuchMethodException("Can't find default constructor for " + clazz.getName());
            }
        }
        return event;
    }

}
