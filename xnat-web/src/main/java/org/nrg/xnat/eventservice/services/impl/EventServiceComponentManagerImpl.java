package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xdat.model.XnatAbstractprojectassetI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.CombinedEventServiceEvent;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.xnat.Assessor;
import org.nrg.xnat.eventservice.model.xnat.Project;
import org.nrg.xnat.eventservice.model.xnat.ProjectAsset;
import org.nrg.xnat.eventservice.model.xnat.Scan;
import org.nrg.xnat.eventservice.model.xnat.Session;
import org.nrg.xnat.eventservice.model.xnat.Subject;
import org.nrg.xnat.eventservice.model.xnat.SubjectAssessor;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class EventServiceComponentManagerImpl implements EventServiceComponentManager {
    private static final String EVENT_RESOURCE_PATTERN ="classpath*:META-INF/xnat/event/*-xnateventserviceevent.properties";

    private List<EventServiceListener> installedListeners;
    private List<EventServiceActionProvider> actionProviders;
    private List<EventServiceEvent> installedEvents;
    private Map<Class<?>, List<String>> classToXsiTypesMap = new HashMap<>();

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
            if(event != null && eventId.matches(event.getType())) {
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
            if(el.getType().contains(name)) {
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
                EventServiceEvent event = CombinedEventServiceEvent.createFromResource(resource);
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

        if(object == null){
            return null;
        }

        if(XnatImageassessordataI.class.isAssignableFrom(object.getClass())) {
            String rootPath = null;
            try {
                rootPath = ((XnatImageassessordata) object).getArchiveRootPath();
            } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
                e.printStackTrace();
            }
            return new Assessor((XnatImageassessordataI) object, null, rootPath);
        }
        else if(XnatProjectdata.class.isAssignableFrom(object.getClass())) {
            return new Project((XnatProjectdata) object);
        }
        else if(XnatAbstractprojectassetI.class.isAssignableFrom(object.getClass())){
            return new ProjectAsset(((XnatAbstractprojectassetI) object).getId(), user);
        }
        else if(XnatResourcecatalog.class.isAssignableFrom(object.getClass())) {
            return new org.nrg.xnat.eventservice.model.xnat.Resource((XnatResourcecatalog) object);
        }
        else if(XnatImagescandataI.class.isAssignableFrom(object.getClass())) {
            String imageSessionId = ((XnatImagescandataI) object).getImageSessionId();
            String sessionUri = null;
            String projectId = null;
            if(imageSessionId == null) {
                log.error("User:" + (user != null ? user.getLogin() : "NULL") + " could not load Scan or parent Session:" + imageSessionId);
            } else {
                XnatImagesessiondata xnatSession = XnatImagesessiondata.getXnatImagesessiondatasById(imageSessionId, user, false);
                 sessionUri = UriParserUtils.getArchiveUri(xnatSession);
                 projectId = xnatSession != null ? xnatSession.getProject() : null;
            }
            Scan scan = new Scan((XnatImagescandataI) object, sessionUri, null);
            if(scan.getProjectId() == null) scan.setProjectId(projectId);
            return scan;

        }
        else if(XnatImagesessiondataI.class.isAssignableFrom(object.getClass())) {
            return new Session((XnatImagesessiondataI) object);
        }
        else if(XnatSubjectassessordataI.class.isAssignableFrom(object.getClass())) {
            return new SubjectAssessor((XnatSubjectassessordataI) object);
        }
        else if(XnatSubjectdataI.class.isAssignableFrom(object.getClass())) {
            return new Subject((XnatSubjectdataI) object);
        }
        return null;
    }



  @Override
    public Class<?> getModelObjectClass(Class<?> objectClass){
        if(objectClass == null){
            return null;
        }

        if(objectClass.isAssignableFrom(XnatImageassessordataI.class)) {
            return Assessor.class;
        }
        else if(objectClass.isAssignableFrom(XnatProjectdataI.class)) {
            return Project.class;
        }
        else if(objectClass.isAssignableFrom(XnatResourcecatalog.class)) {
            return org.nrg.xnat.eventservice.model.xnat.Resource.class;
        }
        else if(objectClass.isAssignableFrom(XnatImagescandataI.class)) {
            return Scan.class;
        }
        else if(objectClass.isAssignableFrom(XnatImagesessiondataI.class)) {
            return Session.class;
        }
        else if(objectClass.isAssignableFrom(XnatSubjectdataI.class)) {
            return Subject.class;
        }
        return null;
    }

    @Override
    public List<String> getXsiTypes(@Nonnull Class<?> xnatClass) {

        if (!classToXsiTypesMap.containsKey(xnatClass)) {
            try {
                final Map<String, String> classMap = ClassMappingFactory.getInstance().getElements();
                Set<String> xsiTypes = new HashSet<>();
                for (String xsiKey : classMap.keySet()) {
                    try {
                        if (xnatClass.isAssignableFrom(
                                Class.forName(StringUtils.removeEnd(classMap.get(xsiKey), "Bean").replaceAll("bean", "om")))) {
                            String xsiType = StringUtils.substringAfterLast(xsiKey, "/");
                            if (!Strings.isNullOrEmpty(xsiType)) {
                                xsiTypes.add(xsiType);
                            }
                        }
                    }catch (Throwable e){
                        log.debug("Could not load class type: " + StringUtils.removeEnd(classMap.get(xsiKey), "Bean").replaceAll("bean", "om"));
                    }
                }
                if(xsiTypes != null && !xsiTypes.isEmpty()) {
                    classToXsiTypesMap.put(xnatClass, new ArrayList<>(xsiTypes));
                }
            } catch (Throwable e) {
                log.error("Could not get xsi type for Class: " + xnatClass.getName(), e.getMessage());
            }
        }
        return classToXsiTypesMap.get(xnatClass);
    }

}
